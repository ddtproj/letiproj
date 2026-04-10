package model;


import engine.BPSimulator;

public class SubProcessInstance extends ProcessInstance {
    private Activity subProcessActivity;
    private ProcessInstance parentProcess;
    private ProcessActivity containingActivity;

    public SubProcessInstance(Activity subProcessActivity, ProcessInstance parentProcess) {
        super(parentProcess.getSimulationInstance());
        this.subProcessActivity = subProcessActivity;
        this.parentProcess = parentProcess;
    }

    public String toString() {
        return "Sub-Process " + this.getId();
    }

    public ProcessInstance getParentProcess() {
        return this.parentProcess;
    }

    public int getActivityIndex() {
        return this.subProcessActivity.getIndex();
    }

    public void setContainingActivity(ProcessActivity containingActivity) {
        this.containingActivity = containingActivity;
    }

    public ProcessActivity getContainingActivity() {
        return this.containingActivity;
    }

    public void addCost(double amount) {
        super.addCost(amount);
        if (this.parentProcess != null) {
            this.parentProcess.addCost(amount);
        }

    }



    public BPSimulator getSimulationInstance() {
        return this.parentProcess.getSimulationInstance();
    }


    public int getMasterInstanceId() {
        return this.parentProcess.getMasterInstanceId();
    }
}
