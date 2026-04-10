package interfaces;


import engine.exception.BPSimulatorException;
import model.ProcessActivity;

public interface IEventProcessor {

    //Enables list of activities in a process instance.
    void notifyStartedActivities(ProcessActivity[] var1) throws BPSimulatorException;

    //Starts processing event from the queue
    void processEvents() throws BPSimulatorException, InterruptedException;

    //Returns already created instance of a message event
    // which was discarded if there exist one and discards the new one.
    ProcessActivity getRegisteredMessage(String eventId);
}
