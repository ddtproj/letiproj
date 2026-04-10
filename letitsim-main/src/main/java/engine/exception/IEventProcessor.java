package engine.exception;


import model.ProcessActivity;

public interface IEventProcessor {
    void notifyStartedActivities(ProcessActivity[] var1) throws BPSimulatorException;

    void processEvents() throws BPSimulatorException, InterruptedException;

    ProcessActivity getRegisteredMessage(String var1);
}
