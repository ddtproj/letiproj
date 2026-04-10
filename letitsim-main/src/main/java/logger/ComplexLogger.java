package logger;


import interfaces.IProcessLogger;
import model.Collaboration;
import model.ProcessActivity;
import model.ProcessInstance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComplexLogger implements IProcessLogger {
    private List<IProcessLogger> loggers = new ArrayList();

    public ComplexLogger() {
    }

    public boolean addLogger(IProcessLogger logger) {
        return this.loggers.add(logger);
    }

    public boolean removeLogger(IProcessLogger logger) {
        return this.loggers.remove(logger);
    }

    public void logElementCompletion(ProcessActivity activity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logElementCompletion(activity);
        }

    }

    public void logElementEnabled(ProcessActivity activity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logElementEnabled(activity);
        }

    }

    public void logElementWithdrawn(ProcessActivity activity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logElementWithdrawn(activity);
        }

    }

    public void logProcessEnd(ProcessInstance process) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logProcessEnd(process);
        }

    }

    public void logProcessEnabled(ProcessInstance process) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logProcessEnabled(process);
        }

    }

    public void logCollaboration(ProcessActivity activity, Collaboration collaboration) {
        Iterator var3 = this.loggers.iterator();

        while(var3.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var3.next();
            logger.logCollaboration(activity, collaboration);
        }

    }

    public void logEnabledPending(ProcessActivity activity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logEnabledPending(activity);
        }

    }

    public void logResourceAvailable(ProcessActivity activity, int totalAvailable) {
        Iterator var3 = this.loggers.iterator();

        while(var3.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var3.next();
            logger.logResourceAvailable(activity, totalAvailable);
        }

    }

    public void logResourceUnavailable(ProcessActivity activity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logResourceUnavailable(activity);
        }

    }

    public void finish() {
        Iterator var1 = this.loggers.iterator();

        while(var1.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var1.next();
            logger.finish();
        }

    }

    public void init() {
        Iterator var1 = this.loggers.iterator();

        while(var1.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var1.next();
            logger.init();
        }

    }

    public void logMessageRegistered(ProcessActivity newActivity) {
        Iterator var2 = this.loggers.iterator();

        while(var2.hasNext()) {
            IProcessLogger logger = (IProcessLogger)var2.next();
            logger.logMessageRegistered(newActivity);
        }

    }
}
