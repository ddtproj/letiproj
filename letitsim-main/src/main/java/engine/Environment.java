package engine;


import engine.exception.BPSimulatorException;
import interfaces.IEventProcessor;
import model.Activity;
import model.EventType;
import model.ProcessActivity;

public class Environment {
    private BPSimulator simInstance;

    //Event Processor class to maintain the queue of events to complete
    private IEventProcessor eventProcessor;

    public Environment(BPSimulator simulationInstance) {
        this.simInstance = simulationInstance;
    }


    public IEventProcessor getEventProcessor()
    {
        return this.eventProcessor;
    }

    public void setEventProcessor(IEventProcessor eventProcessor) {

        this.eventProcessor = eventProcessor;
    }

    public void generateExclusiveEvents(ProcessActivity[] fromActivities) throws BPSimulatorException {
        for(int i = 0; i < fromActivities.length; ++i) {
            Activity a = fromActivities[i].getActivity();
            if (a.getType() == Activity.ActivityType.EVENT && a.getEventType() == EventType.CATCH) {
                fromActivities[i].setExclusiveActivities(fromActivities);
                double eventTime = fromActivities[i].getDuration();
                switch(a.getEventAction()) {
                    case TIMER:
                        fromActivities[i].setCompletionTime(this.simInstance.getClock().getEventTime(eventTime));
                        break;
                    case MESSAGE:
                        if (!fromActivities[i].getActivity().hasIncomingMessageFlow() && fromActivities[i].getCompletionTime() == -1.0D) {
                            fromActivities[i].setCompletionTime(this.simInstance.getClock().getEventTime(eventTime));
                        }
                }
            }
        }

        this.eventProcessor.notifyStartedActivities(fromActivities);
    }
}
