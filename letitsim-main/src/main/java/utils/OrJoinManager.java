package utils;
import engine.BPSimulator;
import model.ProcessActivity;
import model.ProcessInstance;
import parser.IProcessModelParser;
import java.util.*;


/**
 * Class that handles OR-joins in all process instances.
 */
public class OrJoinManager {

    //List of all OR-joins
    private List<Integer> orJoins = new ArrayList<>();

    //process model parser
    private IProcessModelParser processModelParser;

    // map, indicating which flows should completed to ORJoin will switched on
    private Map<Integer, BitSet> flowEdges;

    //indexes for Edges (from process graph)
    private Map<Graph.Edge, Integer> edgeIndexes;

    private Map<Integer, int[]> orToIncomingEdges;

    private BPSimulator simulator;
    private Map<String, BitSet> waitingOrJoins;


    public OrJoinManager() {
    }

    public void setProcessHelper(IProcessModelParser helper) {
        this.processModelParser = helper;
    }


    public void addOrJoin(Integer orJoinIndex) {
        this.orJoins.add(orJoinIndex);
    }


    public void setEdgeIndexes(Map<Graph.Edge, Integer> indexes) {
        this.edgeIndexes = indexes;
    }


    public void setSimulator(BPSimulator bpSimulator) {
        this.simulator = bpSimulator;
    }


    /**
     * Initializes the component
     */
    public void init() {
        if (!this.orJoins.isEmpty()) {
            this.orToIncomingEdges = new HashMap<>();
            this.flowEdges = new HashMap<>();
            this.waitingOrJoins = new HashMap<>();
        }

        //cycle on all orJoins
        for(Integer orJoinIndex:orJoins){

            //get all incoming edges to Orjoin
            Set<Graph.Edge> edges = processModelParser.getGraph().getIncomingEdges(orJoinIndex);
            Graph.Edge[] incomingEdges = edges.toArray(new Graph.Edge[edges.size()]);

            //create mask for incoming edges
            BitSet[] incomingFlows = new BitSet[incomingEdges.length];
            BitSet commonFlows = new BitSet();

            for(int i = 0; i < incomingFlows.length; ++i) {
                incomingFlows[i] = new BitSet(); //bit mask for each flow
                BitSet visitedNodes = new BitSet();
                visitedNodes.set(orJoinIndex);

                this.traceAllPathsToRoot(incomingEdges[i].getSource(), visitedNodes, incomingFlows[i]);
                if (i == 0) {
                    commonFlows.or(incomingFlows[i]);
                } else {
                    commonFlows.and(incomingFlows[i]);
                }
            }

            int[] edgeIndexes = new int[incomingFlows.length];

            for(int i = 0; i < incomingFlows.length; ++i) {
                incomingFlows[i].andNot(commonFlows);
                edgeIndexes[i] = this.edgeIndexes.get(incomingEdges[i]);
                this.flowEdges.put(edgeIndexes[i], incomingFlows[i]);
            }

            this.orToIncomingEdges.put(orJoinIndex, edgeIndexes);
        }
    }



    private void traceAllPathsToRoot(Integer fromActivity, BitSet visitedNodes, BitSet flow) {
        Set<Graph.Edge> incomingEdges = this.processModelParser.getGraph().getIncomingEdges(fromActivity);
        visitedNodes.set(fromActivity);

        for(Graph.Edge e:incomingEdges){
            flow.set(edgeIndexes.get(e));
            if (!visitedNodes.get(e.getSource())) {
                    traceAllPathsToRoot(e.getSource(), visitedNodes, flow);
            }
        }
    }


    /**
     *Returns the index of the activity for which an OR-join in waiting for.
     *
     *     orIndex - index of an OR-join
     *     processState - current process state
     */
    public Integer getWaitingActivityForOr(Integer orIndex, BitSet processState) {
        if (this.orToIncomingEdges == null) {
            return null;
        } else {
            int[] edges = orToIncomingEdges.get(orIndex); //get all incoming edges of node

            for(int edgeIndex = 0; edgeIndex < edges.length; ++edgeIndex) {
                int edge = edges[edgeIndex];

                BitSet branchFlow = flowEdges.get(edge); //get  flow bit mask for edge
                if (branchFlow.intersects(processState)) { //return for first waiting node
                    branchFlow = (BitSet)branchFlow.clone();
                    branchFlow.and(processState);

                    int flowIndex = branchFlow.nextSetBit(0);//index of the first bit that is set to true

                    return simulator.getTokenFlow(flowIndex).getTargetActivityIndex(); //get waiting activity
                }
            }
            return null;
        }
    }


    /**
     * Registers an OR join to be waiting for some activity to be completed in the process state
     * @param activityInstanceId - instance id
     * @param orIndex - index of or node
     */
    public void registerWaitingOrJoin(String activityInstanceId, Integer orIndex) {
        if (this.orToIncomingEdges != null) {
            BitSet state = waitingOrJoins.get(activityInstanceId);
            if (state == null) {
                state = new BitSet();
                this.waitingOrJoins.put(activityInstanceId, state);
            }
            state.set(orIndex);
        }
    }


    /**
     * Update global cache of activities that OR joins are waiting for
     * @param activityIndex
     * @param processInstance
     * @return
     */
    public Integer[] updateWaitingOrJoins(int activityIndex, ProcessInstance processInstance) {
        if (this.orToIncomingEdges == null) {
            return null;
        } else {
            String hashId = ProcessActivity.getInstanceId(processInstance.getId(), activityIndex);
            BitSet state = this.waitingOrJoins.get(hashId);
            if (state == null) {
                return null;
            } else {
                Integer[] enabledOrs = new Integer[state.cardinality()];
                int c = 0;

                for(int i = state.nextSetBit(0); i >= 0; i = state.nextSetBit(i + 1)) {
                    Integer waitingActivityId = this.getWaitingActivityForOr(i, processInstance.getState());
                    if (waitingActivityId == null) {
                        enabledOrs[c++] = i;
                    } else {
                        this.registerWaitingOrJoin(ProcessActivity.getInstanceId(processInstance.getId(), waitingActivityId), i);
                    }
                }
                this.waitingOrJoins.remove(hashId);
                return enabledOrs;
            }
        }
    }


    /**
     * Returns if OR-join is waiting for an activity to be completed.
     *
     * @param activityIndex - index of an element to check if OR join is waiting for it.
     * @param activityId - unique index of an OR-join
     * @return
     */
    public boolean isOrJoinWaitingForActivity(int activityIndex, String activityId) {
        if (this.orToIncomingEdges == null) {
            return false;
        } else {
            BitSet waitingState = this.waitingOrJoins.get(activityId);
            return waitingState != null && waitingState.get(activityIndex);
        }
    }
}
