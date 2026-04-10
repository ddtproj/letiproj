package model;

import engine.exception.ProcessValidationException;
import java.util.ArrayList;


/**
 * Class which represent an element from process model in a process instance.
 */
public class ProcessActivity implements Comparable<ProcessActivity> {
    public static final byte ENABLED = 0;  //Indicates event has been enabled
    public static final byte STARTED = 1;  //Indicates event has been started
    public static final byte FINISHED = 2; //Indicates event has been completed
    public static final byte WITHDRAWN = 3; //Indicates event has been terminated without completion

    // timestamps of states
    private double[] stamps = new double[4];

    private Activity activity;
    private ProcessInstance processInstance;
    private double completionTime = -1.0D;
    private ProcessActivity[] exclusiveActivities;
    private ProcessActivity[] parallelTimerActivities;
    private boolean enabled;
    private double duration = 0.0D;
    private ProcessInstance handlingProcessInstance;
    private String id;

    //the time period in seconds that the task has
    // been waiting for the resource after it has been
    // started to start working according to the work schedule
    private double workingIdleTime;

    //resource idle time between enabled and started states
    private double enabledIdleTime;

    private boolean discarded = false;

    private TaskResource assignedResource;

    private boolean isFromCollaboration = false;

    private ArrayList<Double> workIntervalTimeStamps;

    private ArrayList<Double> enabledIntervalTimeStamps;


    /**
     * Default constructor associated process instance and element in the model
     * @param processInstance
     * @param activity
     */
    public ProcessActivity(ProcessInstance processInstance, Activity activity) {
        this.activity = activity;
        this.processInstance = processInstance;
        this.completionTime = -1.0D;
        this.setEnabled(true);
    }


    /**
     * returns associated activity
     * @return
     */
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * associates activity with processactivity
     * @param activity
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * return  process instance which process activity belongs to
     * @return
     */
    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public int compareTo(ProcessActivity o) {
        double dif = this.completionTime - o.getCompletionTime();
        if (dif == 0.0D) {
            return this.processInstance.getId() - o.getProcessInstance().getId();
        } else {
            return dif > 0.0D ? 1 : (dif < 0.0D ? -1 : 0);
        }
    }


    /**
     * Returns time when this element will be completed
     * @return
     */
    public double getCompletionTime() {
        return this.completionTime;
    }

    public void setCompletionTime(double completionTime) {
        this.completionTime = completionTime;
    }


    public boolean isCompleted() {
        return this.stamps[2] > 0.0D || this.stamps[3] > 0.0D;
    }

    public ProcessActivity[] getExclusiveActivities() {
        return this.exclusiveActivities;
    }

    public void setExclusiveActivities(ProcessActivity[] exclusiveActivities) {
        this.exclusiveActivities = exclusiveActivities;
    }

    /**
     * adding array of exclusive activities
     * @param exclusiveActivities
     */
    public void addExclusiveActivities(ProcessActivity[] exclusiveActivities) {
        if (exclusiveActivities != null) {
            int len1 = this.exclusiveActivities == null ? 0 : this.exclusiveActivities.length;
            int len2 = exclusiveActivities == null ? 0 : exclusiveActivities.length;
            ProcessActivity[] newArr = new ProcessActivity[len1 + len2];
            if (this.exclusiveActivities != null) {
                System.arraycopy(this.exclusiveActivities, 0, newArr, 0, len1);
            }

            if (exclusiveActivities != null) {
                System.arraycopy(exclusiveActivities, 0, newArr, len1, len2);
            }

            this.exclusiveActivities = newArr;
        }
    }

    public boolean isEnabled() {
        return this.enabled && !this.processInstance.isWithdrawn();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled && !enabled && this.activity.getType() == Activity.ActivityType.SUB_PROCESS && this.handlingProcessInstance != null) {
            this.handlingProcessInstance.setWithdrawn(true);
        }

        if (this.enabled != enabled) {
            this.processInstance.notifyEnabled(enabled ? 1 : -1);
            if (!enabled && !this.discarded) {
                this.processInstance.notifyWithdrawn(this);
                double currentTime = this.processInstance.getSimulationInstance().getClock().getTime();
                this.stamp((byte)3, currentTime);
                if (this.workIntervalTimeStamps != null) {
                    int clearFrom = -1;

                    for(int i = 1; i < this.workIntervalTimeStamps.size(); i += 2) {
                        if (currentTime < (Double)this.workIntervalTimeStamps.get(i)) {
                            if (currentTime >= (Double)this.workIntervalTimeStamps.get(i - 1)) {
                                this.workIntervalTimeStamps.set(i, currentTime);
                                clearFrom = i + 1;
                            } else {
                                clearFrom = i - 1;
                            }
                            break;
                        }
                    }

                    if (clearFrom > -1) {
                        while(this.workIntervalTimeStamps.size() > clearFrom) {
                            this.workIntervalTimeStamps.remove(clearFrom);
                        }
                    }
                }
            }
        }
        this.enabled = enabled;
    }



    public void disableExclusiveEvents() {
        if (this.exclusiveActivities != null) {
            ProcessActivity[] var1 = this.exclusiveActivities;
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                ProcessActivity pa = var1[var3];
                if (pa != this && pa != null) {
                    pa.setEnabled(false);
                    pa.setExclusiveActivities((ProcessActivity[])null);
                }
            }

            this.exclusiveActivities = null;
        }

    }



    public String getId() {
        if (this.id == null) {
            this.id = getInstanceId(this.processInstance.getId(), this.activity.getIndex());
        }

        return this.id;
    }

    public double getDuration() throws ProcessValidationException {
        if (this.duration == 0.0D) {
            this.duration = processInstance.getSimulationInstance().getRandomGenerator().fromDistributionInfo(this.activity.getDurationDistributionInfo());
        }

        return this.duration;
    }



    public void adjustWorkingTime() {
        ++this.completionTime;
    }


    public void setParallelTimerActivities(ProcessActivity[] parallelTimerActivities) {
        this.parallelTimerActivities = parallelTimerActivities;
    }

    public ProcessActivity[] getParallelTimerActivities() {
        return this.parallelTimerActivities;
    }

    public void disableParallelTimerEvents() {
        if (this.parallelTimerActivities != null) {
            ProcessActivity[] parallelActivities = this.parallelTimerActivities;

            for(int actIndex = 0; actIndex < parallelActivities.length; ++actIndex) {
                ProcessActivity pa = parallelActivities[actIndex];
                if (pa != this && pa != null && !pa.isCompleted()) {
                    pa.setEnabled(false);
                    pa.setExclusiveActivities((ProcessActivity[])null);
                }
            }

            this.parallelTimerActivities = null;
        }

    }

    public void setHandlingProcessInstance(ProcessInstance handlingProcessInstance) {
        this.handlingProcessInstance = handlingProcessInstance;
    }

    public ProcessInstance getHandlingProcessInstance() {
        return this.handlingProcessInstance;
    }

    public String toString() {
        try {
            return this.activity.toString() + " duration: " + this.getDuration() + " - " + this.processInstance.toString();
        } catch (ProcessValidationException var2) {
            var2.printStackTrace();
            return this.activity.toString() + " duration: n/a " + this.processInstance.toString();
        }
    }

    /**
     * discard execution
     */
    public void discard() {
        this.discarded = true;
        this.setEnabled(false);
    }

    /**
     *
     * @param action
     * @param timestamp
     */
    public void stamp(byte action, double timestamp) {
        if (this.stamps[action] <= 0.0D) {
            this.stamps[action] = timestamp;
        }

    }

    public double getTimeStamp(byte action) {
        double stamp = this.stamps[action];
        if (action == 3 && stamp == 0.0D && !this.isEnabled()) {
            this.stamps[action] = this.processInstance.getCompletionTime();
            return this.stamps[action];
        } else {
            return stamp;
        }
    }

    public static String getInstanceId(int processId, int activityIndex) {
        return processId + "|" + activityIndex;
    }

    public void setWorkingIdleTime(double idleTime) {
        this.workingIdleTime = idleTime;
    }

    public double getWorkingIdleTime() {
        return this.workingIdleTime;
    }

    public void setEnabledIdleTime(double enabledIdleTime) {
        this.enabledIdleTime = enabledIdleTime;
    }

    public double getEnabledIdleTime() {
        return this.enabledIdleTime;
    }

    public double getCost() {
        double cost = this.activity.getFixedCost();
        double duration = Math.max(this.getTimeStamp((byte)2), this.getTimeStamp((byte)3)) - this.getTimeStamp((byte)1) - this.getWorkingIdleTime();
        if (duration > 0.0D && this.activity.getResource() != null && this.activity.getResource().getCostPerHour() != null) {
            cost += duration * this.activity.getResource().getCostPerHour() / 3600.0D;
        }

        return cost;
    }

    public boolean isDiscarded() {
        return this.discarded;
    }

    public void setAssignedResource(TaskResource assignedResource) {
        this.assignedResource = assignedResource;
    }

    public TaskResource getAssignedResource() {
        return this.assignedResource;
    }

    public void addWorkInterval(double from, double to) {
        if (this.workIntervalTimeStamps == null) {
            this.workIntervalTimeStamps = new ArrayList(2);
        }

        this.workIntervalTimeStamps.add(from);
        this.workIntervalTimeStamps.add(to);
    }

    public ArrayList<Double> getWorkIntervals() {
        return this.workIntervalTimeStamps;
    }

    public void addEnabledInterval(double from, double to) {
        if (this.enabledIntervalTimeStamps == null) {
            this.enabledIntervalTimeStamps = new ArrayList(2);
        }

        this.enabledIntervalTimeStamps.add(from);
        this.enabledIntervalTimeStamps.add(to);
    }

    public ArrayList<Double> getEnabledIntervals() {
        return this.enabledIntervalTimeStamps;
    }

    public boolean getIsFromCollaboration() {
        return this.isFromCollaboration;
    }

    public void setIsFromCollaboration(boolean value) {
        this.isFromCollaboration = value;
    }
}
