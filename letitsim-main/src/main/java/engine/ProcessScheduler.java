package engine;


import engine.exception.BPSimulatorException;
import interfaces.IProcessLogger;
import interfaces.IProcessScheduler;
import interfaces.IResourceManager;
import model.*;
import utils.PostConditionTable;
import utils.PreCondition;
import utils.PreConditionTable;



public class ProcessScheduler implements IProcessScheduler {
    private BPSimulator simInstance;
    private IResourceManager resourceManager;
    private IProcessLogger processLogger;
    private PreConditionTable preConditions;
    private PostConditionTable postConditions;
    private Activity[] startEvents;
    private int totalProcessedEvents = 0;

    public ProcessScheduler(BPSimulator bpSimulator) {
        this.simInstance = bpSimulator;
    }


    /**
     * method for simulation execution of activity
     * @param processActivity - current activity
     * @throws BPSimulatorException
     * @throws InterruptedException
     */
    public void processActivity(ProcessActivity processActivity) throws BPSimulatorException, InterruptedException {
        ++this.totalProcessedEvents;
        if (this.simInstance.getMaxAllowedCompletedElements() > 0 &&
                this.totalProcessedEvents > this.simInstance.getMaxAllowedCompletedElements()) {
            throw new BPSimulatorException("Too many elements to simulate across all process instances, maximum is " + this.simInstance.getMaxAllowedCompletedElements());

        } else if (this.simInstance.getMaxSimulationCycleTimeInSeconds() > 0 &&
                processActivity.getCompletionTime() > 0.0D &&
                processActivity.getCompletionTime() - this.simInstance.getSimulationStartTime() > (double)this.simInstance.getMaxSimulationCycleTimeInSeconds()) {
            throw new BPSimulatorException("Maximum allowed cycle time exceeded, maximum is " + this.simInstance.getMaxSimulationCycleTimeInSeconds() / 86400 + " days");
        } else {
            this.simInstance.checkInterrupted();
            Activity a = processActivity.getActivity();
            if (!processActivity.isEnabled()) {
                this.processLogger.logElementWithdrawn(processActivity); //fix execution in log
            } else {
                if (a.getType() == Activity.ActivityType.SUB_PROCESS) {
                    SubProcessInstance pi = this.createSubProcessInstance(a.getIndex(), processActivity.getProcessInstance());
                    processActivity.setHandlingProcessInstance(pi);
                    pi.setContainingActivity(processActivity);
                    processActivity.getProcessInstance().addChildProcess(pi);
                } else {
                    this.activityCompleted(processActivity); //execute activiti
                }
            }
        }
    }



    /**
     * Processing of activiti completion
     * @param processActivity
     * @throws BPSimulatorException
     * @throws InterruptedException
     */
    public void activityCompleted(ProcessActivity processActivity) throws BPSimulatorException, InterruptedException {
        ProcessInstance currentInstance = processActivity.getProcessInstance();

        //fix moment of finish
        processActivity.stamp(ProcessActivity.FINISHED, this.simInstance.getClock().getTime());

        // fix completion in log
        this.processLogger.logElementCompletion(processActivity);


        currentInstance.notifyCompleted(processActivity);

        Activity a = processActivity.getActivity();
        this.cleanUpTokensBeforeActivity(processActivity);
        Resource res = a.getResource();
        if (res != null) {
            this.resourceManager.notifyResourceAvailableFromActivity(processActivity);
        }

        try {
            if (a.getType() == Activity.ActivityType.EVENT) {
                if (a.getEventType() == EventType.END) {
                    if (currentInstance instanceof SubProcessInstance) {

                        SubProcessInstance subProc = (SubProcessInstance)currentInstance;

                        if (a.getEventType() != EventType.THROW ||
                                !a.isCancel() && a.getEventAction() != EventAction.ERROR) {
                            if (currentInstance.getEnabledActivities() == 0) {
                                this.activityCompleted(subProc.getContainingActivity());
                            }
                        } else {
                            this.handleThrowActivity(subProc, a);
                        }
                    }
                } else if (a.getEventType() == EventType.THROW) {
                    this.handleThrowActivity(currentInstance, a);
                }
            }

            if (currentInstance.isWithdrawn()) {
                return;
            }


            //
            int[] postFlowIndexes = this.postConditions.getFlowsByEventId(a.getIndex());
            GatewayType gt = processActivity.getActivity().getGatewayType();
            if (processActivity.getActivity().getType() == Activity.ActivityType.GATEWAY &&
                    a.isSplit() && (gt == GatewayType.XOR || gt == GatewayType.OR)) {

                //choose paths to continue instance (if gateway  is split)
                postFlowIndexes = this.simInstance.getGatewayPathSelector().getEnabledFlows(processActivity, postFlowIndexes);
            }

            int[] postFlInd = postFlowIndexes;
            int postFlIndLen = postFlowIndexes.length;

            int ix;
            for(int actInd = 0; actInd < postFlIndLen; ++actInd) {
                ix = postFlInd[actInd];
                if (ix >= 1) {
                    currentInstance.getState().set(ix); // set new flow to state
                }
            }

            int n = 0;

            //create new enabled tasks to complete
            ProcessActivity[] enabled = new ProcessActivity[postFlowIndexes.length];
            int[] postFlIndexs = postFlowIndexes;
            ix = postFlowIndexes.length;

            for(int flIndx = 0; flIndx < ix; ++flIndx) {
                int postFlowIndex = postFlIndexs[flIndx];
                if (postFlowIndex >= 1) {
                    postFlIndLen = this.simInstance.getTokenFlow(postFlowIndex).getTargetActivityIndex();
                    if (!this.preConditions.getOrJoinManager().isOrJoinWaitingForActivity(postFlIndLen, processActivity.getId()) &&
                            this.preConditions.isActivityEnabled(postFlIndLen, processActivity.getProcessInstance())) {
                        //add activity to enabled set
                        enabled[n++] = new ProcessActivity(currentInstance, this.simInstance.getActivity(postFlIndLen));
                    }
                }
            }

            this.checkForEnabledOrJoinActivities(processActivity);
            boolean isEventGw = processActivity.getActivity().getGatewayType() == GatewayType.EVENT;
            if (isEventGw) {
                this.simInstance.getEnvironment().generateExclusiveEvents(enabled);
            }

            //handling collaborations if exists (recursively)
            if (processActivity.getActivity().getCollaborations() != null) {
                this.handleCollaborations(processActivity);
            }

            if (isEventGw) {
                return;
            }

            //process enabled activities
            this.processEnabledActivities(enabled, currentInstance);
        } finally {
            if (currentInstance.getEnabledActivities() == 0) {
                this.resourceManager.notifyResourcesAvailableFromProcess(currentInstance.getId());
                processActivity.getProcessInstance().setCompletionTime(this.simInstance.getClock().getTime());
                this.processLogger.logProcessEnd(processActivity.getProcessInstance());
            }

        }

    }

    private void cleanUpTokensBeforeActivity(ProcessActivity processActivity) {
        processActivity.getProcessInstance().getState().andNot(((PreCondition)this.preConditions.get(processActivity.getActivity().getIndex())).getCondition());
    }


    //recursive process of process execution
    private void processEnabledActivities(ProcessActivity[] enabled, ProcessInstance processInstance) throws BPSimulatorException, InterruptedException {
        for(int i = 0; i < enabled.length && enabled[i] != null; ++i) {
            Activity.ActivityType at = enabled[i].getActivity().getType();
            if (at == Activity.ActivityType.GATEWAY) {
                this.processActivity(enabled[i]);
                if (processInstance.isWithdrawn()) { //cancelling
                    break;
                }

                enabled[i] = null;
            }
        }
        this.resourceManager.notifyActivitiesEnabled(enabled); //call for resources
    }

    public void checkForEnabledOrJoinActivities(ProcessActivity fromActivity) throws BPSimulatorException, InterruptedException {
        Integer[] enabledOrs = this.preConditions.getOrJoinManager().updateWaitingOrJoins(fromActivity.getActivity().getIndex(), fromActivity.getProcessInstance());
        if (enabledOrs != null) {
            ProcessActivity[] enabled = new ProcessActivity[enabledOrs.length];
            int i = 0;
            Integer[] var5 = enabledOrs;
            int var6 = enabledOrs.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Integer orJoinIndex = var5[var7];
                if (orJoinIndex == null) {
                    break;
                }

                enabled[i++] = new ProcessActivity(fromActivity.getProcessInstance(), this.simInstance.getActivity(orJoinIndex));
            }

            if (i > 0) {
                this.processEnabledActivities(enabled, fromActivity.getProcessInstance());
            }

        }
    }

    public void notifyActivityDisabled(ProcessActivity processActivity) throws BPSimulatorException, InterruptedException {
        this.cleanUpTokensBeforeActivity(processActivity);
        this.checkForEnabledOrJoinActivities(processActivity);
    }

    public IResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public IProcessLogger getProcessLogger() {
        return this.processLogger;
    }

    public void setProcessLogger(IProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

    public PreConditionTable getPreConditions() {
        return this.preConditions;
    }

    public void setPreConditions(PreConditionTable preConditions) {
        this.preConditions = preConditions;
    }

    public PostConditionTable getPostConditions() {
        return this.postConditions;
    }

    public void setPostConditions(PostConditionTable postConditions) {
        this.postConditions = postConditions;
    }

    public void initProcessInstance(double completionTime) throws BPSimulatorException {
        ProcessInstance pi = new ProcessInstance(this.simInstance);
        pi.setStartTime(completionTime);
        ProcessActivity[] startActs = new ProcessActivity[this.startEvents.length];

        for(int i = 0; i < startActs.length; ++i) {
            startActs[i] = new ProcessActivity(pi, this.startEvents[i]);
            startActs[i].setCompletionTime(completionTime);
            startActs[i].stamp((byte)0, completionTime);
        }

        this.enableNewProcess(startActs);
    }

    protected SubProcessInstance createSubProcessInstance(int subProcessIndex, ProcessInstance parentProcess) throws BPSimulatorException {
        SubProcessInstance pi = new SubProcessInstance(this.simInstance.getActivity(subProcessIndex), parentProcess);
        pi.setStartTime(this.simInstance.getClock().getTime());
        Activity[] startEvents = this.simInstance.getSubProcessStartEvents(subProcessIndex);
        ProcessActivity[] startActs = new ProcessActivity[startEvents.length];

        for(int i = 0; i < startActs.length; ++i) {
            startActs[i] = new ProcessActivity(pi, startEvents[i]);
        }

        this.enableNewProcess(startActs);
        return pi;
    }

    private void enableNewProcess(ProcessActivity[] startEvents) throws BPSimulatorException {
        this.processLogger.logProcessEnabled(startEvents[0].getProcessInstance());
        this.resourceManager.notifyActivitiesEnabled(startEvents);
    }

    public void handleThrowActivity(SubProcessInstance fromProcessInstance, Activity activity) throws BPSimulatorException, InterruptedException {
        int parentProcessIndex = fromProcessInstance.getParentProcess().getActivityIndex();
        String eventCode = activity.getEventCode();
        Activity a = this.simInstance.getCatchActivity(parentProcessIndex, fromProcessInstance.getActivityIndex(), eventCode);
        if (a != null) {
            ProcessActivity pa = new ProcessActivity(fromProcessInstance.getParentProcess(), a);
            this.processActivity(pa);
        } else {
            this.handleThrowActivity(fromProcessInstance.getParentProcess(), activity);
        }

    }

    public void handleThrowActivity(ProcessInstance fromProcessInstance, Activity activity) throws BPSimulatorException, InterruptedException {
        if (fromProcessInstance instanceof SubProcessInstance) {
            this.handleThrowActivity((SubProcessInstance)fromProcessInstance, activity);
        }

    }

    private void handleCollaborations(ProcessActivity fromActivity) throws BPSimulatorException {
        Collaboration[] var4 = fromActivity.getActivity().getCollaborations();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Collaboration c = var4[var6];
            String procId = c.getTargetProcessId();
            ProcessInstance partnerProcess = fromActivity.getProcessInstance().getPartnerProcess(procId);
            boolean isNew = partnerProcess == null;
            if (isNew) {
                partnerProcess = new ProcessInstance(this.simInstance);
                partnerProcess.setMasterInstanceId(fromActivity.getProcessInstance().getMasterInstanceId());
                fromActivity.getProcessInstance().setPartnerProcess(procId, partnerProcess);
                partnerProcess.setPartnerProcess(c.getSourceProcessId(), fromActivity.getProcessInstance());
            }

            this.processLogger.logCollaboration(fromActivity, c);
            if (c.getTargetActivity().getType() == Activity.ActivityType.EVENT) {
                ProcessActivity pa = new ProcessActivity(partnerProcess, c.getTargetActivity());
                pa.setIsFromCollaboration(true);
                if (isNew) {
                    this.enableNewProcess(new ProcessActivity[]{pa});
                } else {
                    this.resourceManager.notifyActivitiesEnabled(new ProcessActivity[]{pa});
                }
            }
        }

    }

    public void setStartEvents(Activity[] startEvents) {
        this.startEvents = startEvents;
    }

    public int getTotalProcessedEvents() {
        return this.totalProcessedEvents;
    }
}
