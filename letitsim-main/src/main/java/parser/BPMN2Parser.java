package parser;

import engine.exception.ModelParseException;
import engine.exception.ProcessValidationException;

import model.*;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;
import utils.Graph;
import utils.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import org.jdom2.*;





public abstract class BPMN2Parser implements IProcessModelParser {

    protected Graph graph;
    protected Map<String, Integer> elementMap;
    protected Map<Integer, String> elementIdMap;
    protected Map<Integer, GatewayType> gatewayMap;
    protected Map<Integer, EventType> eventTypeMap;
    protected Map<Integer, EventAction> eventActionMap;
    protected Map<Integer, Integer[]> subProcessStartMap;
    private Map<Integer, List<Integer>> subProcessStartMapCache;
    private Map<Integer, Boolean> subProcesses;
    private Map<String, List<Integer>> startEventsByProcessId;
    protected Map<String, Integer> errorHandlerMap;
    protected Set<String> tasks;
    private Map<String, Element> processRootElements;
    private Map<String, Element> collaborationElements;
    private Map<Integer, String> simpleNames;
    private Map<Integer, String> processIdMap;
    private Collaboration[] collaborations;
    private Map<Integer, Boolean> cancelActivityMap;
    private Map<Integer, Integer> boundaryEventMap;
    private Map<String, String> elementConditionExpressionMap;
    private List<String> modelFileNames = null;
    private List<InputStream> inputStreams = null;
    private List<Integer> startEventIndexes;
    private List<Document> documents;
    private Map<Integer, Element> callActivities;
    private List<String> mainProcessIds = new ArrayList();


    public BPMN2Parser() {
    }


    public void setInputStreams(List<InputStream> inputStreams) {
        this.inputStreams = inputStreams;
    }


    public void setFiles(List<String> fileNames) {
        this.modelFileNames = fileNames;
    }


    public void parse() throws ModelParseException, ProcessValidationException {
        this.graph = null;
        this.elementMap = new HashMap();
        this.elementIdMap = new HashMap();
        this.gatewayMap = new HashMap();
        this.eventTypeMap = new HashMap();
        this.tasks = new HashSet();
        this.subProcessStartMap = new HashMap();
        this.subProcessStartMapCache = new HashMap();
        this.subProcesses = new HashMap();
        this.eventActionMap = new HashMap();
        this.errorHandlerMap = new HashMap();
        this.processRootElements = new LinkedHashMap();
        this.collaborationElements = new HashMap();
        this.simpleNames = new HashMap();
        this.processIdMap = new HashMap();
        this.cancelActivityMap = new HashMap();
        this.boundaryEventMap = new HashMap();
        this.elementConditionExpressionMap = new HashMap();
        this.startEventIndexes = new ArrayList();
        this.callActivities = new HashMap();
        this.startEventsByProcessId = new HashMap();
        this.loadDocuments();
        Document mainDocument = this.documents.isEmpty() ? null : (Document)this.documents.get(0);
        Iterator var2 = this.documents.iterator();

        while(var2.hasNext()) {
            Document document = (Document)var2.next();
            Namespace bpmn2ns = document.getRootElement().getNamespace();
            Iterator var5 = document.getRootElement().getChildren("process", bpmn2ns).iterator();

            Object e;
            while(var5.hasNext()) {
                e = var5.next();
                if (e instanceof Element) {
                    String processId = ((Element)e).getAttributeValue("id");
                    if (document == mainDocument) {
                        this.mainProcessIds.add(processId);
                    }

                    this.processRootElements.put(processId, (Element)e);
                }
            }

            var5 = document.getRootElement().getChildren("collaboration", bpmn2ns).iterator();

            while(var5.hasNext()) {
                e = var5.next();
                if (e instanceof Element) {
                    this.collaborationElements.put(((Element)e).getAttributeValue("id"), (Element)e);
                }
            }
        }

        if (this.processRootElements.size() == 0) {
            throw new ProcessValidationException("Process root elements not found from file");
        } else {
            this.initGraph();
        }
    }



    protected static String uniqueId(String processId, String elementId)
    {
        return processId + '.' + elementId;
    }


    protected void loadDocuments() throws ModelParseException {
        try {
            if (this.documents == null) {
                this.documents = new ArrayList();
                Iterator var1;
                if (this.modelFileNames != null) {
                    var1 = this.modelFileNames.iterator();

                    while(var1.hasNext()) {
                        String modelFileName = (String)var1.next();
                        this.documents.add((new SAXBuilder()).build(modelFileName));
                }
                } else if (this.inputStreams != null) {
                    var1 = this.inputStreams.iterator();

                    while(var1.hasNext()) {
                        InputStream inputStream = (InputStream)var1.next();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        InputSource is = new InputSource(reader);
                        is.setEncoding("UTF-8");

                        try {
                            this.documents.add((new SAXBuilder()).build(is));
                        } finally {
                            reader.close();
                        }
                    }
                }
            }

        } catch (Exception var9) {
            throw new ModelParseException(var9.getMessage());
        }
    }



    protected void initGraph() throws ProcessValidationException {
        this.graph = new Graph();
        this.startEventIndexes.clear();
        Iterator var1 = this.processRootElements.values().iterator();

        while(var1.hasNext()) {
            Element process = (Element)var1.next();
            this.processElement(process, -1, (String)null);
        }

        this.processAllCallActivities();
        var1 = this.subProcessStartMapCache.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Integer, List<Integer>> en = (Map.Entry)var1.next();
            Integer[] indexes = new Integer[((List)en.getValue()).size()];
            ((List)en.getValue()).toArray(indexes);
            this.subProcessStartMap.put(en.getKey(), indexes);
        }

        this.subProcessStartMapCache = null;
        List<Collaboration> collaborationList = new ArrayList();
        Iterator var4 = this.collaborationElements.values().iterator();

        while(var4.hasNext()) {
            Element collaboration = (Element)var4.next();
            Namespace bpmn2ns = collaboration.getNamespace();
            List<Element> elements = collaboration.getChildren("messageFlow", bpmn2ns);
            Iterator var7 = elements.iterator();

            while(var7.hasNext()) {
                Element flow = (Element)var7.next();
                Integer src = (Integer)this.elementMap.get(flow.getAttributeValue("sourceRef"));
                if (src == null) {
                    src = (Integer)this.elementMap.get(withoutProcessPrefix(flow.getAttributeValue("sourceRef")));
                }

                Integer tgt = (Integer)this.elementMap.get(flow.getAttributeValue("targetRef"));
                if (tgt == null) {
                    tgt = (Integer)this.elementMap.get(withoutProcessPrefix(flow.getAttributeValue("targetRef")));
                }

                if (src != null && tgt != null) {
                    Collaboration c = new Collaboration(flow.getAttributeValue("name"), (String)this.processIdMap.get(src), (String)this.processIdMap.get(tgt), src, tgt);
                    collaborationList.add(c);
                    this.startEventIndexes.remove(tgt);
                }
            }
        }

        this.collaborations = new Collaboration[collaborationList.size()];
        collaborationList.toArray(this.collaborations);
    }

    private static String withoutProcessPrefix(String uniqueId) {
        int p = uniqueId.indexOf(46);
        return p > 0 ? uniqueId.substring(p) : uniqueId;
    }


    protected static boolean isKnownElement(String name) {
        return name.equals("sequenceFlow") || name.equals("task") || name.endsWith("Task") || name.endsWith("Event") || name.endsWith("Gateway") || name.equals("subProcess") || name.equals("callActivity") || name.equals("laneSet");
    }


    protected void processElement(Element el, Integer fromVertice, String processId) throws ProcessValidationException {
        List<Element> edges = new ArrayList();
        List<Element> boundaryEvents = new ArrayList();
        String elemName = el.getName();
        if (processId == null && elemName.equals("process")) {
            processId = el.getAttributeValue("id");
        }

        if (processId != null && !processId.isEmpty()) {
            boolean startEventExits = el.getChildren("startEvent", el.getNamespace()).size() > 0;
            this.processElement(processId, el);
            List<Element> children = el.getChildren();
            Iterator var9 = children.iterator();

            while(true) {
                while(true) {
                    Element elem;
                    String id;
                    String simpleName;
                    String name;
                    Namespace bpmn2ns;
                    do {
                        if (!var9.hasNext()) {
                            var9 = boundaryEvents.iterator();

                            while(var9.hasNext()) {
                                elem = (Element)var9.next();
                                id = elem.getAttributeValue("name");
                                simpleName = uniqueId(processId, elem.getAttributeValue("id"));
                                name = uniqueId(processId, elem.getAttributeValue("attachedToRef"));
                                int handlerProcess = (Integer)this.elementMap.get(name);
                                this.boundaryEventMap.put(this.elementMap.get(simpleName), this.elementMap.get(name));
                                this.errorHandlerMap.put(fromVertice + "|" + handlerProcess + "|" + id, this.elementMap.get(simpleName));
                            }

                            Iterator var20 = edges.iterator();

                            while(true) {
                                String sourceRef;
                                Element flow;
                                do {
                                    do {
                                        if (!var20.hasNext()) {
                                            return;
                                        }

                                        flow = (Element)var20.next();
                                        sourceRef = flow.getAttributeValue("sourceRef");
                                    } while(sourceRef == null);
                                } while(sourceRef.equals(""));

                                Integer src = (Integer)this.elementMap.get(uniqueId(processId, sourceRef));
                                String targetRef = flow.getAttributeValue("targetRef");
                                if (targetRef != null && !targetRef.equals("")) {
                                    Integer tgt = (Integer)this.elementMap.get(uniqueId(processId, flow.getAttributeValue("targetRef")));
                                    if (src != null && tgt != null) {
                                        if (!this.graph.containsEdge(src, tgt)) {
                                            this.graph.addEdge(src, tgt, uniqueId(processId, flow.getAttributeValue("id")));
                                        } else {
                                            DebugLogger.println("Warning. Duplicate edge " + flow.getAttributeValue("id") + " from " + sourceRef + " to " + flow.getAttributeValue("targetRef"));
                                        }
                                        continue;
                                    }

                                    throw new RuntimeException("Malformed graph");
                                }

                                throw new ProcessValidationException("targetRef attribute not found for sequence flow with id: " + flow.getAttributeValue("id"));
                            }
                        }

                        elem = (Element)var9.next();
                        id = uniqueId(processId, elem.getAttributeValue("id"));
                        simpleName = elem.getAttributeValue("name");
                        name = processId + simpleName;
                        name = name.trim();
                        elemName = elem.getName();
                        bpmn2ns = elem.getNamespace();
                    } while(!isKnownElement(elemName));

                    this.processElement(id, elem);
                    if (!startEventExits) {
                        DebugLogger.println("Start event not found inside process " + el.getAttributeValue("id"));
                        throw new ProcessValidationException("Start event not found inside process " + el.getAttributeValue("id"));
                    }

                    if (elemName.equals("sequenceFlow")) {
                        edges.add(elem);
                        Element condition = elem.getChild("conditionExpression", bpmn2ns);
                        if (condition != null) {
                            this.elementConditionExpressionMap.put(id, condition.getText());
                        }
                    } else if (!this.elementMap.containsKey(id)) {
                        Integer vertex = this.graph.addVertex(name);
                        this.elementMap.put(id, vertex);
                        this.elementIdMap.put(vertex, id);
                        this.simpleNames.put(vertex, simpleName);
                        this.processIdMap.put(vertex, processId);
                        if (!elemName.equals("task") && !elemName.endsWith("Task")) {
                            if (elemName.endsWith("Event")) {
                                if (elem.getName().equals("startEvent")) {
                                    if (fromVertice == -1) {
                                        this.startEventIndexes.add(vertex);
                                        if (!this.startEventsByProcessId.containsKey(fromVertice)) {
                                            this.startEventsByProcessId.put(processId, new ArrayList());
                                        }

                                        ((List)this.startEventsByProcessId.get(processId)).add(vertex);
                                    } else {
                                        if (!this.subProcessStartMapCache.containsKey(fromVertice)) {
                                            this.subProcessStartMapCache.put(fromVertice, new ArrayList());
                                        }

                                        ((List)this.subProcessStartMapCache.get(fromVertice)).add(vertex);
                                    }

                                    this.eventTypeMap.put(vertex, EventType.START);
                                } else if (elemName.equals("endEvent")) {
                                    this.eventTypeMap.put(vertex, EventType.END);
                                } else if (elemName.equals("boundaryEvent")) {
                                    this.eventTypeMap.put(vertex, EventType.BOUNDARY);
                                    boundaryEvents.add(elem);
                                } else if (elemName.equals("intermediateThrowEvent")) {
                                    this.eventTypeMap.put(vertex, EventType.THROW);
                                } else if (elemName.equals("intermediateCatchEvent")) {
                                    this.eventTypeMap.put(vertex, EventType.CATCH);
                                } else {
                                    this.eventTypeMap.put(vertex, EventType.NA);
                                }

                                if (!elem.getChildren("errorEventDefinition", bpmn2ns).isEmpty()) {
                                    this.eventActionMap.put(vertex, EventAction.ERROR);
                                } else if (!elem.getChildren("timerEventDefinition", bpmn2ns).isEmpty()) {
                                    this.eventActionMap.put(vertex, EventAction.TIMER);
                                } else if (!elem.getChildren("messageEventDefinition", bpmn2ns).isEmpty()) {
                                    this.eventActionMap.put(vertex, EventAction.MESSAGE);
                                } else if (!elem.getChildren("terminateEventDefinition", bpmn2ns).isEmpty()) {
                                    this.eventActionMap.put(vertex, EventAction.TERMINATE);
                                } else if (!elem.getChildren("signalEventDefinition", bpmn2ns).isEmpty()) {
                                    this.eventActionMap.put(vertex, EventAction.MESSAGE);
                                }

                                try {
                                    Attribute cancelAttr = elem.getAttribute("cancelActivity");
                                    if (cancelAttr != null && cancelAttr.getBooleanValue()) {
                                        this.cancelActivityMap.put(vertex, true);
                                    }
                                } catch (DataConversionException var17) {
                                    ;
                                }
                            } else if (elemName.endsWith("Gateway")) {
                                if (elemName.equals("exclusiveGateway")) {
                                    this.gatewayMap.put(vertex, GatewayType.XOR);
                                } else if (elemName.equals("parallelGateway")) {
                                    this.gatewayMap.put(vertex, GatewayType.AND);
                                } else if (elemName.equals("inclusiveGateway")) {
                                    this.gatewayMap.put(vertex, GatewayType.OR);
                                } else if (elemName.equals("eventBasedGateway")) {
                                    this.gatewayMap.put(vertex, GatewayType.EVENT);
                                }
                            } else {
                                boolean handleAsTask;
                                if (elemName.equals("subProcess")) {
                                    this.subProcesses.put(vertex, true);
                                    handleAsTask = this.simulateSubProcessAsTask(id);
                                    if (!handleAsTask) {
                                        this.processElement(elem, vertex, processId);
                                    }
                                } else if (elemName.equals("callActivity")) {
                                    this.subProcesses.put(vertex, true);
                                    handleAsTask = this.simulateSubProcessAsTask(id);
                                    if (!handleAsTask) {
                                        this.callActivities.put(vertex, elem);
                                    }
                                }
                            }
                        } else {
                            this.tasks.add(id);
                        }
                    }
                }
            }
        } else {
            throw new ProcessValidationException("Process id not set");
        }
    }

    protected void processAllCallActivities() throws ProcessValidationException {
        Iterator var1 = this.callActivities.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Integer, Element> entry = (Map.Entry)var1.next();
            Element elem = (Element)entry.getValue();
            String callId = elem.getAttributeValue("calledElement");
            DebugLogger.println("Processing call activity for process " + callId + ", from vertice " + entry.getKey() + ", callActivityId " + elem.getAttributeValue("id"));
            if (!this.startEventsByProcessId.containsKey(callId)) {
                return;
            }

            this.subProcessStartMapCache.put(entry.getKey(), this.startEventsByProcessId.get(callId));
        }

    }

    protected void processElement(String id, Element elem) throws ProcessValidationException {
    }

    public Integer[] getAllStartEventIndexes() {
        Integer[] indexes = new Integer[this.startEventIndexes.size()];
        this.startEventIndexes.toArray(indexes);
        return indexes;
    }

    public Integer[] getStartEventIndexesForMainFile() {
        List<Integer> indexes = new ArrayList();
        Iterator var2 = this.mainProcessIds.iterator();

        while(var2.hasNext()) {
            String processId = (String)var2.next();
            indexes.addAll((Collection)this.startEventsByProcessId.get(processId));
        }

        Integer[] indexArr = new Integer[indexes.size()];
        indexes.toArray(indexArr);
        return indexArr;
    }

    public String getElementSimpleName(Integer verticeIndex) {
        return (String)this.simpleNames.get(verticeIndex);
    }

    public String getProcessId(Integer verticeIndex) {
        return (String)this.processIdMap.get(verticeIndex);
    }

    public Collaboration[] getCollaborations() {
        return this.collaborations;
    }

    public boolean isInterruptingEvent(Integer verticeIndex) {
        Boolean isCancel = (Boolean)this.cancelActivityMap.get(verticeIndex);
        return isCancel != null && isCancel;
    }

    public Integer getParentBoundaryActivity(Integer boundaryEventIndex) {
        return (Integer)this.boundaryEventMap.get(boundaryEventIndex);
    }

    public double getEdgeProbability(String edgeId) throws ProcessValidationException {
        String condExp = (String)this.elementConditionExpressionMap.get(edgeId);
        if (condExp != null) {
            try {
                return Double.parseDouble(condExp);
            } catch (NumberFormatException var4) {
                throw new ProcessValidationException("Unable to parse sequence flow probability from: " + condExp);
            }
        } else {
            return 1.0D;
        }
    }

    public String getModelElementId(Integer verticeIndex) {
        return (String)this.elementIdMap.get(verticeIndex);
    }

    public boolean isParallel(Integer verticeIndex) {
        return this.gatewayMap.get(verticeIndex) != null && ((GatewayType)this.gatewayMap.get(verticeIndex)).equals(GatewayType.AND);
    }

    public boolean isChoice(Integer verticeIndex) {
        return this.gatewayMap.get(verticeIndex) != null && ((GatewayType)this.gatewayMap.get(verticeIndex)).equals(GatewayType.XOR);
    }

    public boolean isEventGateway(Integer verticeIndex) {
        return this.gatewayMap.get(verticeIndex) != null && ((GatewayType)this.gatewayMap.get(verticeIndex)).equals(GatewayType.EVENT);
    }

    public Graph getGraph() {
        return this.graph;
    }

    public boolean isTask(String name) {
        return this.tasks.contains(name);
    }

    public EventType getEventType(Integer vertex) {
        return (EventType)this.eventTypeMap.get(vertex);
    }

    public EventAction getEventAction(Integer vertex) {
        return (EventAction)this.eventActionMap.get(vertex);
    }

    public Integer[] getSubProcessStartActivityIndexes(Integer verticeIndex) {
        return (Integer[])this.subProcessStartMap.get(verticeIndex);
    }

    public int getErrorHandlerActivity(int processIndex, int subProcessIndex, String errorCode) {
        String key = processIndex + "|" + subProcessIndex + "|" + errorCode;
        return this.errorHandlerMap.containsKey(key) ? (Integer)this.errorHandlerMap.get(key) : -1;
    }

    public boolean isSubProcess(Integer verticeIndex) {
        return this.subProcesses.containsKey(verticeIndex);
    }

    public boolean isOR(Integer verticeIndex) {
        return this.gatewayMap.get(verticeIndex) == GatewayType.OR;
    }

    protected List<Document> getDocuments() {
        return this.documents;
    }

    protected Map<String, Element> getProcessRootElements() {
        return this.processRootElements;
    }

    protected boolean simulateSubProcessAsTask(String subProcessId) {
        return false;
    }
}
