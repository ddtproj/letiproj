package interfaces;

import engine.exception.BPSimulatorException;
import model.ProcessActivity;

/**
 *
 */
public interface IProcessScheduler {
    //process current activity
    void processActivity(ProcessActivity currentActivity) throws BPSimulatorException, InterruptedException;

    //
    void notifyActivityDisabled(ProcessActivity activity) throws BPSimulatorException, InterruptedException;
}
