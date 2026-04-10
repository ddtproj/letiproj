package utils;

import model.Activity;
import model.GatewayType;
import model.ProcessActivity;
import model.ProcessInstance;
import java.util.BitSet;
import java.util.HashMap;


/**
 * class represents Map <ActivityId. Precondition></>
 */
public class PreConditionTable extends HashMap<Integer, PreCondition> {

    //


    private static final long serialVersionUID = 1L;


    private OrJoinManager orJoinManager;

    public PreConditionTable() {
    }


    /**
     * Checks if conditions of activitie's starting are satisfied
     * @param activityId - identifier of activity
     * @param processInstance - identifier of instance
     * @return true if conditions are satisfied
     */
    public boolean isActivityEnabled(Integer activityId, ProcessInstance processInstance) {
        PreCondition p = this.get(activityId); //conditions of node activation
        Activity a = p.getActivity();
        BitSet currentState = processInstance.getState(); //current conditions
        if (a.isJoin() && a.getGatewayType() != GatewayType.XOR) {
            switch(a.getGatewayType()) {
                case AND:
                    BitSet testSet = (BitSet)currentState.clone();
                    testSet.and(p.getCondition());
                    return testSet.cardinality() == p.getCondition().cardinality(); //if all bits set to true the same(in environment and in condition)
                                                                            // then return true
                case OR:
                    Integer waitingActivity = this.orJoinManager.getWaitingActivityForOr(activityId, currentState);
                    if (waitingActivity != null) {
                        this.orJoinManager.registerWaitingOrJoin(ProcessActivity.getInstanceId(processInstance.getId(), waitingActivity), activityId);
                        return false;
                    }

                    return true;
                default:
                    return false;
            }
        } else {
            return currentState.intersects(p.getCondition());
        }
    }

    public void setOrJoinManager(OrJoinManager orJoinManager) {
        this.orJoinManager = orJoinManager;
    }

    public OrJoinManager getOrJoinManager() {
        return this.orJoinManager;
    }
}
