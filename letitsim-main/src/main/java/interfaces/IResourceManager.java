package interfaces;

import engine.exception.BPSimulatorException;
import model.ProcessActivity;


public interface IResourceManager {
    /**
     * Starts activities that have enough resources available.
     * @param var1
     * @throws BPSimulatorException
     */
    void notifyActivitiesEnabled(ProcessActivity[] var1) throws BPSimulatorException;

    /**
     * Notify the resource manager that resources used by activity are available
     * @param processActiviti
     * @throws BPSimulatorException
     */
    void notifyResourceAvailableFromActivity(ProcessActivity  processActiviti) throws BPSimulatorException;

    /**
     * Frees all resources allocated by a process instance
     *
     * @param processInstance - process from where to make resources available
     * @throws BPSimulatorException
     */
    void notifyResourcesAvailableFromProcess(Integer processInstance) throws BPSimulatorException;
}
