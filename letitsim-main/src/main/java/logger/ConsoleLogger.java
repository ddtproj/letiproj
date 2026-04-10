package logger;

import engine.BPSimulator;
import interfaces.IProcessLogger;
import model.Collaboration;
import model.ProcessActivity;
import model.ProcessInstance;

public class ConsoleLogger implements IProcessLogger {
    private BPSimulator simInstance;

    public ConsoleLogger(BPSimulator simulationInstance) {
        this.simInstance = simulationInstance;
    }

    public void logElementCompletion(ProcessActivity activity) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " Completed: " + activity.toString() + " at time " + this.simInstance.getClock().getFormattedTime() + ", idle for: " + (activity.getWorkingIdleTime() + activity.getEnabledIdleTime()) + "s");
    }

    public void logElementEnabled(ProcessActivity activity) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " Enabled: " + activity.toString() + " at time " + this.simInstance.getClock().getFormattedTime() + " completion time: " + this.simInstance.getClock().timeToString(activity.getCompletionTime()));
    }

    public void logProcessEnd(ProcessInstance process) {
        System.out.println("Process: " + process.getId() + " completed at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logProcessEnabled(ProcessInstance process) {
        System.out.println("Process: " + process.getId() + " started at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logElementWithdrawn(ProcessActivity activity) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " NOT processed: " + activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logCollaboration(ProcessActivity activity, Collaboration collaboration) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " collaboration '" + collaboration.getName() + "' from " + activity.getActivity().toString() + " " + collaboration.getTargetActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logEnabledPending(ProcessActivity activity) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " Pending: " + activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logMessageRegistered(ProcessActivity activity) {
        System.out.println("Process: " + activity.getProcessInstance().getId() + " Message received: " + activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logResourceAvailable(ProcessActivity activity, int totalAvailable) {
        System.out.println(totalAvailable + " resources (" + activity.getActivity().getResource() + ") available for " + activity);
    }

    public void logResourceUnavailable(ProcessActivity activity) {
        System.out.println("NO resources (" + activity.getActivity().getResource() + ") available for " + activity);
    }

    public void finish() {
        System.out.println("Simulation finished");
    }

    public void init() {
    }
}
