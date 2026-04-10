package interfaces;


import model.Collaboration;
import model.ProcessActivity;
import model.ProcessInstance;




public interface IProcessLogger {
    void logElementCompletion(ProcessActivity var1);

    void logElementEnabled(ProcessActivity var1);

    void logElementWithdrawn(ProcessActivity var1);

    void logProcessEnd(ProcessInstance var1);

    void logProcessEnabled(ProcessInstance var1);

    void logCollaboration(ProcessActivity var1, Collaboration var2);

    void logEnabledPending(ProcessActivity var1);

    void logMessageRegistered(ProcessActivity var1);

    void logResourceAvailable(ProcessActivity var1, int var2);

    void logResourceUnavailable(ProcessActivity var1);

    void finish();

    void init();
}
