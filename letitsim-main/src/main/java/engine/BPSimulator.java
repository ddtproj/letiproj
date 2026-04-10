package engine;

import engine.exception.BPSimulatorException;
import engine.exception.ModelParseException;
import engine.exception.ProcessValidationException;
import interfaces.IProcessLogger;
import logger.ComplexLogger;
import model.*;
import model.xsd.DistributionInfo;
import parser.IProcessModelParser;
import parser.ParserFactory;
import utils.*;
import java.io.InputStream;
import java.util.*;


/**
 * Main class of Simulation engine
 */
public class BPSimulator {

    //maximum allowed elements to be processed in the simulation, 0 to disable
    private int maxAllowedCompletedElements;

    //maximum arrival period length in seconds.If 0 then unlimited
    private int maxAllowedArrivalPeriodLengthInSeconds;

    //maximum allowed cycle time in simulation
    private int maxSimulationCycleTimeInSeconds;


    private int maxAllowedResources;

    private int maxAllowedResourceInstances;

    // bpmn files to simulate
    private List<String> processFiles;

    // streams of bpmn-files
    private List<InputStream> inputStreams;

    private ProcessScheduler processScheduler;

    //used resource manager component
    private ResourceManager resourceManager;

    //Event Processor class to maintain the queue of events to complete
    private EventProcessor eventProcessor;

    private IProcessLogger processLogger;

    private PostConditionTable postConditionTable;
    private PreConditionTable preConditionTable;

    // encapsulate condition for OR-joins
    private OrJoinManager orJoinManager;

    //clock (time manager) for the simulation
    private Clock clock;

    private Environment environment;
    private SimulationStatus status;
    private Map<Integer, Activity> activities;
    private Map<Integer, TokenFlow> tokenFlows;
    private Map<Graph.Edge, Integer> edgeIndexes;
    private Map<String, Integer> edgeIdToEdgeIndex;
    private IProcessModelParser modelParser;
    private boolean[] traverseStatus;
    private Map<Integer, Activity[]> subProcessStartEventsCache;
    private boolean debug;

    //total number of process instances that the simulator created
    private int totalProcessInstances;

    private RandomGenerator randomGenerator;

    //gateway path selector for this simulation (Helper class for split-gateway path selections)
    private GatewayPathSelector gatewayPathSelector;

    private int tokenFlowCount;

    private int processInstancesCreated;
    private long startTime;
    private long endTime;
    private boolean cancelled;
    private double simulationStartTime;




    public BPSimulator() {
        this.maxAllowedCompletedElements = 0;
        this.maxAllowedArrivalPeriodLengthInSeconds = 0;
        this.maxSimulationCycleTimeInSeconds = 0;
        this.maxAllowedResources = 0;
        this.maxAllowedResourceInstances = 0;
        this.debug = true;
        this.tokenFlowCount = 1;
        this.processInstancesCreated = 0;
        this.processScheduler = new ProcessScheduler(this);
        this.resourceManager = new ResourceManager(this);
        this.eventProcessor = new EventProcessor(this);

        this.processLogger = new ComplexLogger();

        this.subProcessStartEventsCache = new HashMap();
        this.clock = new Clock();
        this.environment = new Environment(this);
        this.status = SimulationStatus.CREATED;
        this.randomGenerator = new RandomGenerator();
    }



    public BPSimulator(List<String> processFiles) {
        this();
        this.processFiles = processFiles;
    }

    public void setInputStreams(List<InputStream> inputStreams)
    {
        this.inputStreams = inputStreams;
    }


    /**
     * Entrance point of simulation process
     * @throws ModelParseException
     * @throws BPSimulatorException
     * @throws InterruptedException
     */
    public void run() throws ModelParseException, BPSimulatorException, InterruptedException {
        this.status = SimulationStatus.INITIALIZING;

        //first of all loading and parsing BPMN +BPSim files for
        try {
            this.loadFile();
        } catch (ModelParseException loadException) {
            throw new ModelParseException("Unable to load the model: " + loadException.getMessage());
        }

        //next, we validate model
        if (this.validateModel()) {
            try {
                this.initComponents();
                this.initResources();
                this.preProcessModel();

                //get starting events
                Integer[] startEventIndexes = this.modelParser.getStartEventIndexesForMainFile();

                Activity[] startEvents = new Activity[startEventIndexes.length];

                int i = 0;

                while(true) {
                    if (i >= startEvents.length) {

                        //set start events to scheduler for execution
                        this.processScheduler.setStartEvents(startEvents);

                        //set starting time
                        this.clock.setTime(this.modelParser.getStartTime());

                        this.simulationStartTime = this.modelParser.getStartTime();
                        this.startTime = System.currentTimeMillis();

                        //WTF?
                        this.setMaxSimulationCycleTimeInSeconds(94608000);

                        break;
                    }

                    startEvents[i] = this.getActivity(startEventIndexes[i]);
                    ++i;
                }
            } catch (ProcessValidationException exception) {
                throw exception;
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new ProcessValidationException("Unable to initialize simulation: " + exception.getMessage());
            }

            boolean var13 = false;

            try {
                var13 = true;
                this.initProcessInstances();
                this.status = SimulationStatus.STARTED;
                this.eventProcessor.processEvents();
                this.status = SimulationStatus.FINALIZING;
                this.endTime = System.currentTimeMillis();
                this.processLogger.finish();
                var13 = false;
            } catch (BPSimulatorException var15) {
                this.endTime = System.currentTimeMillis();
                if (this.debug) {
                    DebugLogger.println("Simulation exception: " + var15.getMessage());
                }

                throw var15;
            } catch (InterruptedException var16) {
                Thread.currentThread().interrupt();
                this.endTime = System.currentTimeMillis();
                if (this.debug) {
                    DebugLogger.println("Simulation terminated: " + var16.getMessage());
                }

                throw var16;
            } finally {
                if (var13) {
                    long time = this.endTime - this.startTime;
                    if (this.debug) {
                        DebugLogger.println("Processed " + time + "ms elements: " + this.processScheduler.getTotalProcessedEvents());
                        if (time > 0L) {
                            DebugLogger.println("Elements processed per second: " + (long)(this.processScheduler.getTotalProcessedEvents() * 1000) / time);
                        }

                        DebugLogger.println("Writing logs...");
                    }

                    this.status = SimulationStatus.FINISHED;
                    if (this.debug) {
                        DebugLogger.println("Done");
                    }

                }
            }

            long time = this.endTime - this.startTime;
            if (this.debug) {
                DebugLogger.println("Processed " + time + "ms elements: " + this.processScheduler.getTotalProcessedEvents());
                if (time > 0L) {
                    DebugLogger.println("Elements processed per second: " + (long)(this.processScheduler.getTotalProcessedEvents() * 1000) / time);
                }

                DebugLogger.println("Writing logs...");
            }

            this.status = SimulationStatus.FINISHED;
            if (this.debug) {
                DebugLogger.println("Done");
            }

        } else {
            throw new ProcessValidationException("Invalid process definition");
        }
    }





    public ComplexLogger getLogger() {
        return (ComplexLogger)this.processLogger;
    }

    private void initProcessInstances() throws BPSimulatorException, InterruptedException {
        this.totalProcessInstances = this.modelParser.getTotalProcessInstances();
        DistributionInfo arrivalDi = this.modelParser.getArrivalRateDistributionInfo();
        double completionTime = this.modelParser.getStartTime();
        double startTime = completionTime;
        this.processLogger.init();
        TimeTable startEventsTimeTable = this.modelParser.getArrivalRateTimeTable();
        if (startEventsTimeTable != null) {
            startEventsTimeTable.setClock(this.getClock());
            startEventsTimeTable.setSimulator(this);
            completionTime = startEventsTimeTable.getCompletionTime(completionTime, 0.001D) - 0.001D;
        }

        for(int i = 0; i < this.totalProcessInstances; ++i) {
            this.checkInterrupted();
            double nextArrival = this.randomGenerator.fromDistributionInfo(arrivalDi);
            if (this.getMaxAllowedArrivalPeriodLengthInSeoonds() > 0 && completionTime + nextArrival - startTime >= (double)this.getMaxAllowedArrivalPeriodLengthInSeoonds()) {
                throw new BPSimulatorException("Arrival rate too big. Process arrivals allowed over max " + this.getMaxAllowedArrivalPeriodLengthInSeoonds() / 3600 / 24 + " days");
            }

            this.processScheduler.initProcessInstance(completionTime);
            if (startEventsTimeTable != null) {
                completionTime = startEventsTimeTable.getCompletionTime(completionTime, nextArrival);
            } else {
                completionTime += nextArrival;
            }
        }

    }



    /**
     * add resources from the model to resource manager
     * @throws ProcessValidationException
     */
    private void initResources() throws ProcessValidationException {
        if (this.getMaxAllowedResources() > 0 && this.modelParser.getAllResources().size() > this.getMaxAllowedResources()) {
            throw new ProcessValidationException("Too many different resources. Maximum allowed amount is " + this.getMaxAllowedResources());
        } else {
            for(Resource r: this.modelParser.getAllResources())
            {
                TimeTable t = r.getTimeTable(); //time table for every resource
                if (t != null) {
                    r.getTimeTable().setSimulator(this);
                }

                if (this.getMaxAllowedResourceInstances() > 0 && r.getTotalAmount() > this.getMaxAllowedResourceInstances()) {
                    throw new ProcessValidationException("Too many resources has been defined. Maximum allowed amount per resource is " + this.getMaxAllowedResourceInstances());
                }

                //add resource to resource manager
                this.resourceManager.defineResource(r);
            }

            this.resourceManager.initComponent();
        }
    }


    //not implemented yet
    private boolean validateModel() {
        return true;
    }

    /**
     * Init
     */
    private void initComponents() {
        this.postConditionTable = new PostConditionTable();
        this.preConditionTable = new PreConditionTable();
        this.activities = new HashMap();
        this.tokenFlows = new HashMap();
        this.edgeIndexes = new HashMap();
        this.orJoinManager = new OrJoinManager();
        this.orJoinManager.setEdgeIndexes(this.edgeIndexes);
        this.orJoinManager.setProcessHelper(this.modelParser);
        this.orJoinManager.setSimulator(this);
        this.preConditionTable.setOrJoinManager(this.orJoinManager);
        this.edgeIdToEdgeIndex = new HashMap();
        this.processScheduler.setProcessLogger(this.processLogger);
        this.processScheduler.setResourceManager(this.resourceManager);
        this.processScheduler.setPostConditions(this.postConditionTable);
        this.processScheduler.setPreConditions(this.preConditionTable);
        this.resourceManager.setEventProcessor(this.eventProcessor);
        this.resourceManager.setProcessLogger(this.processLogger);
        this.eventProcessor.setProcessScheduler(this.processScheduler);
        this.eventProcessor.setProcessLogger(this.processLogger);
        this.environment.setEventProcessor(this.eventProcessor);
        this.gatewayPathSelector = new GatewayPathSelector(this);
    }

    private void loadFile() throws ModelParseException, ProcessValidationException {
        if (this.inputStreams != null) {
            this.modelParser = ParserFactory.getProcessModelParserForStreams(this.inputStreams);
        } else if (this.processFiles != null) {
            this.modelParser = ParserFactory.getProcessModelParserForFiles(this.processFiles);
        }

        if (this.modelParser == null) {
            throw new ModelParseException("Unable to parse BPMN file");
        } else {
            this.modelParser.parse();
        }
    }

    public TokenFlow getTokenFlow(int index) {
        return (TokenFlow)this.tokenFlows.get(index);
    }

    public Activity getActivity(int index) {
        return (Activity)this.activities.get(index);
    }

    private Activity addActivity(Activity a) {
        this.activities.put(a.getIndex(), a);
        return a;
    }


    /**
     * Prepare model for simulation
     *
     * @throws ProcessValidationException
     * @throws InterruptedException
     */
    private void preProcessModel() throws ProcessValidationException, InterruptedException {
        Graph g = this.modelParser.getGraph();
        Integer[] startEvents = this.modelParser.getAllStartEventIndexes();
        this.traverseStatus = new boolean[g.getVerticeCount() + 1];
        Integer[] var3 = startEvents;
        int var4 = startEvents.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Integer ix = var3[var5];
            this.traverseVertice(ix, -1, (String)null);
        }

        Map<Integer, List<Activity>> boundaryMap = new HashMap();
        Iterator var11 = g.getSourceNodes().iterator();

        while(true) {
            Integer ix;
            EventType et;
            do {
                if (!var11.hasNext()) {
                    Iterator var16 = boundaryMap.keySet().iterator();

                    while(var16.hasNext()) {
                        Integer parentIndex = (Integer)var16.next();
                        Activity a = this.getActivity(parentIndex);
                        Activity[] timerEvents = new Activity[((List)boundaryMap.get(parentIndex)).size()];
                        List<Activity> timerActivities = (List)boundaryMap.get(parentIndex);
                        timerActivities.toArray(timerEvents);
                        a.setBoundaryTimerEvents(timerEvents);
                    }

                    Collaboration[] allCols = this.modelParser.getCollaborations();
                    if (allCols.length > 0) {
                        Arrays.sort(allCols, new Comparator<Collaboration>() {
                            public int compare(Collaboration o1, Collaboration o2) {
                                return o1.getSourceActivityIndex() - o2.getSourceActivityIndex();
                            }
                        });
                        ArrayList<Collaboration> elementCols = new ArrayList();

                        for(int i = 0; i <= allCols.length; ++i) {
                            if (i < allCols.length) {
                                allCols[i].setSourceActivity(this.getActivity(allCols[i].getSourceActivityIndex()));
                                allCols[i].setTargetActivity(this.getActivity(allCols[i].getTargetActivityIndex()));
                                this.getActivity(allCols[i].getTargetActivityIndex()).setHasIncomingMessageFlow(true);
                            }

                            if (i > 0 && (i == allCols.length || allCols[i].getSourceActivityIndex() != allCols[i - 1].getSourceActivityIndex())) {
                                Collaboration[] elementColsArray = new Collaboration[elementCols.size()];
                                elementCols.toArray(elementColsArray);
                                allCols[i - 1].getSourceActivity().setCollaborations(elementColsArray);
                                elementCols.clear();
                            }

                            if (i != allCols.length) {
                                elementCols.add(allCols[i]);
                            }
                        }
                    }

                    this.orJoinManager.init();
                    return;
                }

                ix = (Integer)var11.next();
                et = this.modelParser.getEventType(ix);
            } while(et != EventType.BOUNDARY && et != EventType.START);

            this.traverseVertice(ix, -1, (String)null);
            if (et == EventType.BOUNDARY) {
                Activity a = this.getActivity(ix);
                Integer parentIndex = this.modelParser.getParentBoundaryActivity(ix);
                if (a.getEventAction() == EventAction.TIMER) {
                    if (!boundaryMap.containsKey(parentIndex)) {
                        boundaryMap.put(parentIndex, new ArrayList());
                    }

                    ((List)boundaryMap.get(parentIndex)).add(this.getActivity(ix));
                }
            }
        }
    }





    private void traverseVertice(Integer verticeIndex, Integer fromVertice, String edgeId) throws ProcessValidationException, InterruptedException {
        this.checkInterrupted();
        boolean hasProcessed = this.traverseStatus[verticeIndex];
        TokenFlow flow = new TokenFlow(verticeIndex, this.tokenFlowCount++);
        this.tokenFlows.put(flow.getIndex(), flow);
        if (edgeId != null) {
            this.edgeIdToEdgeIndex.put(edgeId, flow.getIndex());
            double prob = this.modelParser.getEdgeProbability(edgeId);
            if (prob < 0.0D || prob > 1.0D) {
                throw new ProcessValidationException("Invalid flow probability '" + prob + "' for a flow " + edgeId + " from element " + this.getActivity(fromVertice).toString());
            }

            flow.setProbability(prob);
        }

        PreCondition pc;
        if (hasProcessed && this.preConditionTable.containsKey(verticeIndex)) {
            pc = (PreCondition)this.preConditionTable.get(verticeIndex);
            this.getActivity(verticeIndex);
        } else {
            Activity a = new Activity(this.modelParser.getModelElementId(verticeIndex), verticeIndex, this.modelParser.getProcessId(verticeIndex));
            a.setDescription(this.modelParser.getElementSimpleName(verticeIndex));
            this.addActivity(a);
            pc = new PreCondition(a);
            this.preConditionTable.put(verticeIndex, pc);
            if (this.modelParser.isTask(a.getId())) {
                a.setType(Activity.ActivityType.TASK);
            } else if (!this.modelParser.isChoice(verticeIndex) && !this.modelParser.isParallel(verticeIndex) && !this.modelParser.isEventGateway(verticeIndex) && !this.modelParser.isOR(verticeIndex)) {
                if (this.modelParser.isSubProcess(verticeIndex)) {
                    Integer[] childElements = this.modelParser.getSubProcessStartActivityIndexes(verticeIndex);
                    if (childElements != null && childElements.length != 0) {
                        a.setType(Activity.ActivityType.SUB_PROCESS);
                        Integer[] var9 = childElements;
                        int var10 = childElements.length;

                        for(int var11 = 0; var11 < var10; ++var11) {
                            Integer startIx = var9[var11];
                            this.traverseVertice(startIx, -1, (String)null);
                        }
                    } else {
                        a.setType(Activity.ActivityType.TASK);
                    }
                } else {
                    a.setType(Activity.ActivityType.EVENT);
                    a.setEventType(this.modelParser.getEventType(verticeIndex));
                    a.setEventAction(this.modelParser.getEventAction(verticeIndex));
                    a.setEventCode(this.modelParser.getElementSimpleName(verticeIndex));
                    if (this.modelParser.isInterruptingEvent(verticeIndex)) {
                        a.setCancel(true);
                    }

                    if (a.getEventType() == null) {
                        throw new ProcessValidationException("Unsupported event detected for element " + a.getId());
                    }
                }
            } else {
                a.setSplit(this.modelParser.getGraph().isSplit(verticeIndex));
                a.setJoin(this.modelParser.getGraph().isJoin(verticeIndex));
                if (this.modelParser.isChoice(verticeIndex)) {
                    a.setGatewayType(GatewayType.XOR);
                } else if (this.modelParser.isEventGateway(verticeIndex)) {
                    a.setGatewayType(GatewayType.EVENT);
                } else if (this.modelParser.isParallel(verticeIndex)) {
                    a.setGatewayType(GatewayType.AND);
                } else if (this.modelParser.isOR(verticeIndex)) {
                    a.setGatewayType(GatewayType.OR);
                    if (a.isJoin()) {
                        this.orJoinManager.addOrJoin(verticeIndex);
                    }
                }

                a.setType(Activity.ActivityType.GATEWAY);
            }

            a.setFixedCost(this.modelParser.getElementFixedCost(a.getId()));
            a.setCostThreshold(this.modelParser.getElementCostThreshold(a.getId()));
            a.setDurationThreshold(this.modelParser.getElementDurationThreshold(a.getId()));
            if (a.getType() == Activity.ActivityType.TASK) {
                String resourceId = this.modelParser.getTaskResourceId(a.getId());
                if (resourceId != null) {
                    a.setResource(this.resourceManager.getDefinedResource(resourceId));
                }
            }


            DistributionInfo durationInfo = modelParser.getElementDurationInformation(a.getId());
            if (durationInfo != null) {
                a.setDurationDistributionInfo(durationInfo);
            }

            if (this.debug) {
                DebugLogger.println(a.getIndex() + " - " + a);
            }
        }

        BitSet fromState = new BitSet();
        if (fromVertice != -1) {
            fromState.set(flow.getIndex());
            this.postConditionTable.add(fromVertice, flow.getIndex());
        }

        pc.getCondition().or(fromState);
        if (!hasProcessed) {
            this.traverseStatus[verticeIndex] = true;
            Iterator var17 = this.modelParser.getGraph().getOutgoingEdges(verticeIndex).iterator();

            while(var17.hasNext()) {
                Graph.Edge e = (Graph.Edge)var17.next();
                edgeId = e.getName();
                this.traverseVertice(e.getTarget(), verticeIndex, edgeId);
                this.edgeIndexes.put(e, this.edgeIdToEdgeIndex.get(edgeId));
                if (this.debug) {
                    DebugLogger.println("flow: " + this.edgeIdToEdgeIndex.get(edgeId) + " from " + e.getSource() + "-" + e.getTarget());
                }
            }
        }
    }




    public Activity[] getSubProcessStartEvents(Integer subProcessIndex) {
        if (this.subProcessStartEventsCache.containsKey(subProcessIndex)) {
            return (Activity[])this.subProcessStartEventsCache.get(subProcessIndex);
        } else {
            Integer[] indexes = this.modelParser.getSubProcessStartActivityIndexes(subProcessIndex);
            Activity[] acts = new Activity[indexes.length];

            for(int i = 0; i < acts.length; ++i) {
                acts[i] = this.getActivity(indexes[i]);
            }

            this.subProcessStartEventsCache.put(subProcessIndex, acts);
            return acts;
        }
    }

    public Activity getCatchActivity(int parentProcessIndex, int subProcessIndex, String eventCode) {
        return this.getActivity(this.modelParser.getErrorHandlerActivity(parentProcessIndex, subProcessIndex, eventCode));
    }

    public Clock getClock() {
        return this.clock;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public SimulationStatus getStatus() {
        return this.status;
    }

    public int getTotalProcessInstances() {
        return this.totalProcessInstances;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public GatewayPathSelector getGatewayPathSelector() {
        return this.gatewayPathSelector;
    }

    public int getIdForNewProcessInstance() {
        return this.processInstancesCreated++;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public int getTotalProcessedEvents() {
        return this.processScheduler.getTotalProcessedEvents();
    }

    public void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted() || this.cancelled) {
            throw new InterruptedException("Simulation terminated");
        }
    }

    public void interrupt() {
        this.cancelled = true;
    }

    public RandomGenerator getRandomGenerator() {
        return this.randomGenerator;
    }

    public int getMaxAllowedCompletedElements() {
        return this.maxAllowedCompletedElements;
    }

    public void setMaxAllowedCompletedElements(int maxAllowedCompletedElements) {
        this.maxAllowedCompletedElements = maxAllowedCompletedElements;
    }

    public int getMaxAllowedArrivalPeriodLengthInSeoonds() {
        return this.maxAllowedArrivalPeriodLengthInSeconds;
    }

    public void setMaxAllowedArrivalPeriodLengthInSeoonds(int maxAllowedArrivalPeriodLengthInSeconds) {
        this.maxAllowedArrivalPeriodLengthInSeconds = maxAllowedArrivalPeriodLengthInSeconds;
    }

    public int getMaxAllowedResources() {
        return this.maxAllowedResources;
    }

    public void setMaxAllowedResources(int maxAllowedResources) {
        this.maxAllowedResources = maxAllowedResources;
    }

    public int getMaxAllowedResourceInstances() {
        return this.maxAllowedResourceInstances;
    }

    public void setMaxAllowedResourceInstances(int maxAllowedResourceInstances) {
        this.maxAllowedResourceInstances = maxAllowedResourceInstances;
    }

    public int getActivityCount() {
        return this.activities == null ? 0 : this.activities.size();
    }

    public int getMaxSimulationCycleTimeInSeconds() {
        return this.maxSimulationCycleTimeInSeconds;
    }

    public void setMaxSimulationCycleTimeInSeconds(int maxSimulationCycleTimeInSeconds) {
        this.maxSimulationCycleTimeInSeconds = maxSimulationCycleTimeInSeconds;
    }

    public double getSimulationStartTime() {
        return this.simulationStartTime;
    }
}
