package model;



import engine.BPSimulator;
import engine.exception.BPSimulatorException;
import utils.ProcessEventTimeline;
import utils.ProcessEventType;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




public class ProcessInstance {
    private BPSimulator simulationInstance;
    private int id;
    private int masterId = -1;
    private BitSet state;
    private boolean withdrawn;
    private int enabledActivities = 0;
    private Map<String, ProcessInstance> partnerProcesses;
    private List<SubProcessInstance> childProcesses;
    private double startTime;
    private double completionTime = 0.0D;
    private double totalCost = 0.0D;
    private ProcessEventTimeline timeline;
    private static final float EVENTS_PER_ACTIVITY_MULTIPLIER = 2.5F;

    public ProcessInstance(BPSimulator simulation) {
        this.id = simulation.getIdForNewProcessInstance();
        this.state = new BitSet();
        this.simulationInstance = simulation;
        this.timeline = new ProcessEventTimeline((int)((float)simulation.getActivityCount() * 2.5F) + 2);
    }

    public BitSet getState() {
        return this.state;
    }


    public void setState(BitSet state)
    {
        this.state = state;
    }

    public void addCost(double amount) {
        this.totalCost += amount;
    }

    public void notifyCompleted(ProcessActivity activity) throws BPSimulatorException {
        this.notifyEnabled(-1);
        Activity a = activity.getActivity();
        if (a.getType() == Activity.ActivityType.EVENT &&
                a.getEventType() == EventType.END &&
                (a.isCancel() || a.getEventAction() == EventAction.ERROR ||
                        a.getEventAction() == EventAction.TERMINATE)) {
            this.setWithdrawn(true);
        }

        this.addCost(activity.getCost());
        ArrayList<Double> intervals = activity.getEnabledIntervals();
        int i;
        Double from;
        Double to;
        if (intervals != null) {
            for(i = 1; i < intervals.size(); i += 2) {
                from = (Double)intervals.get(i - 1);
                to = (Double)intervals.get(i);
                if (to > from) {
                    this.timeline.addEvent(ProcessEventType.EnabledStart, from);
                    this.timeline.addEvent(ProcessEventType.EnabledStop, to);
                }
            }
        }

        intervals = activity.getWorkIntervals();
        if (intervals != null) {
            for(i = 1; i < intervals.size(); i += 2) {
                from = (Double)intervals.get(i - 1);
                to = (Double)intervals.get(i);
                if (to > from) {
                    this.timeline.addEvent(ProcessEventType.WorkStart, from);
                    this.timeline.addEvent(ProcessEventType.WorkStop, to);
                }
            }
        }

    }

    public void notifyWithdrawn(ProcessActivity activity) {
        if (!activity.isDiscarded()) {
            this.addCost(activity.getCost());
        }

    }

    public void notifyEnabled(int numEnabled) {
        this.enabledActivities += numEnabled;
    }

    public int getId() {
        return this.id;
    }

    public String toString() {
        return "Process " + this.id;
    }

    public boolean isWithdrawn() {
        return this.withdrawn;
    }

    public void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
        if (withdrawn) {
            this.enabledActivities = 0;
            this.completionTime = this.getSimulationInstance().getClock().getTime();
        }

        if (this.childProcesses != null) {
            Iterator var2 = this.childProcesses.iterator();

            while(var2.hasNext()) {
                ProcessInstance pi = (ProcessInstance)var2.next();
                pi.setWithdrawn(withdrawn);
            }
        }

    }

    public int getActivityIndex() {
        return -1;
    }

    public int getEnabledActivities() {
        return this.enabledActivities;
    }

    public void setPartnerProcess(String processId, ProcessInstance process) {
        if (this.partnerProcesses == null) {
            this.partnerProcesses = new HashMap();
        }

        this.partnerProcesses.put(processId, process);
    }

    public ProcessInstance getPartnerProcess(String processId) {
        return this.partnerProcesses == null ? null : (ProcessInstance)this.partnerProcesses.get(processId);
    }

    public void addChildProcess(SubProcessInstance childProcess) {
        if (this.childProcesses == null) {
            this.childProcesses = new ArrayList();
        }

        this.childProcesses.add(childProcess);
    }

    public List<SubProcessInstance> getChildProcesses() {
        return this.childProcesses;
    }

    public double getStartTime() {
        return this.startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
        this.timeline.addEvent(ProcessEventType.ProcessStart, startTime);
    }

    public double getCompletionTime() {
        return this.completionTime;
    }

    public void setCompletionTime(double completionTime) {
        this.completionTime = completionTime;
        this.timeline.addEvent(ProcessEventType.ProcessEnd, this.startTime);
    }

    public double getTotalCost() {
        return this.totalCost;
    }

    public BPSimulator getSimulationInstance() {
        return this.simulationInstance;
    }

    public int getMasterInstanceId() {
        return this.getIsMasterProcess() ? this.id : this.masterId;
    }

    public ProcessEventTimeline getTimeline() {
        return this.timeline;
    }

    public void setMasterInstanceId(int id) {
        this.masterId = id;
    }

    public boolean getIsMasterProcess() {
        return this.masterId == -1;
    }

    public boolean isCompleted() {
        return this.completionTime > 0.0D;
    }
}
