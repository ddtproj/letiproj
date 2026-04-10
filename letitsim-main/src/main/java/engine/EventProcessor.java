package engine;


import engine.exception.BPSimulatorException;
import interfaces.IEventProcessor;
import interfaces.IProcessLogger;
import interfaces.IProcessScheduler;
import model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


/**
 * Event Processor class to maintain the queue of events to complete
 */
public class EventProcessor implements IEventProcessor {
    private BPSimulator simInstance;
    private IProcessScheduler processScheduler;
    private IProcessLogger processLogger;

    //queue
    private PriorityQueue<ProcessActivity> eventQueue = new PriorityQueue<>();

    //
    private Map<String, ProcessActivity> pendingEvents = new HashMap<>();

    //
    private Map<String, ProcessActivity> registeredMessages = new HashMap<>();


    /**
     * Main constructor. Initializes queue and maps.
     * @param simulationInstance
     */
    public EventProcessor(BPSimulator simulationInstance) {
        this.simInstance = simulationInstance;
    }

    /**
     * Enables list of activities in a process instance.
     * call every time when activity enabled. Call by resource manager
     * @param enabledActivities
     * @throws BPSimulatorException
     */
    public void notifyStartedActivities(ProcessActivity[] enabledActivities) throws BPSimulatorException {
        ProcessActivity activity = null;
        ProcessActivity[] enabledActArr = enabledActivities;


        for(int enabIndex = 0; enabIndex < enabledActivities.length; ++enabIndex) {
            ProcessActivity newActivity = enabledActArr[enabIndex];
            if (newActivity != null) {
                //remove from pending queue
                activity = this.pendingEvents.remove(newActivity.getId());

                if (activity == null) {
                    //remove from message queue
                    activity = this.getRegisteredMessage(newActivity.getId());
                    if (activity != null) {
                        activity.setEnabled(true);
                        newActivity.discard();
                        if (activity.getCompletionTime() < this.simInstance.getClock().getTime()) {
                            // set current execution time
                            activity.setCompletionTime(this.simInstance.getClock().getTime());
                        }
                    }
                }

                if (activity != null) {
                    ProcessActivity[] discardedExclusiveActivities = newActivity.getExclusiveActivities();
                    if (discardedExclusiveActivities != null) {
                        activity.addExclusiveActivities(discardedExclusiveActivities);
                    }

                    if (newActivity.getIsFromCollaboration()) {
                        newActivity.discard();
                    }
                }

                if (activity == null) {
                    activity = newActivity;
                    Activity sourceActivity = newActivity.getActivity();
                    if (sourceActivity.getType() == Activity.ActivityType.EVENT && sourceActivity.getEventType() == EventType.CATCH && sourceActivity.getEventAction() == EventAction.MESSAGE) {
                        if (newActivity.getIsFromCollaboration()) {
                            ProcessInstance processInstance = newActivity.getProcessInstance();
                            if (!processInstance.isCompleted() && !processInstance.isWithdrawn()) {
                                this.registeredMessages.put(newActivity.getId(), newActivity);
                                this.processLogger.logMessageRegistered(newActivity);
                            }
                            continue;
                        }

                        if (sourceActivity.hasIncomingMessageFlow()) {
                            this.pendingEvents.put(newActivity.getId(), newActivity);
                            this.processLogger.logEnabledPending(newActivity);
                            continue;
                        }
                    }
                }

                if (activity.getCompletionTime() == -1.0D) {
                    Resource r = activity.getActivity().getResource();
                    TimeTable tt = r != null ? r.getTimeTable() : null;
                    if (tt != null) {
                        if (tt.getClock() == null) {
                            tt.setClock(this.simInstance.getClock());
                        }

                        try {
                            tt.setCompletionTime(activity);
                        } catch (InterruptedException var11) {
                            throw new BPSimulatorException("Simulation interrupted");
                        }
                    } else {
                        activity.setCompletionTime(this.simInstance.getClock().getEventTime(activity.getDuration()));
                    }
                }

                double startedStamp = Math.max(this.simInstance.getClock().getTime(), activity.getTimeStamp((byte)0));
                activity.stamp((byte)1, startedStamp);
                this.processLogger.logElementEnabled(activity);
                this.eventQueue.add(activity);
                this.initBoundaryTimerEvents(activity);
            }
        }

    }






    /**
     * Process next activity
     * @throws BPSimulatorException
     * @throws InterruptedException
     */
    private void processNextActivity() throws BPSimulatorException, InterruptedException {
        ProcessActivity pa = this.eventQueue.poll();

        if (pa.isEnabled()) { //if activity executing
            if (pa.getActivity().getEventAction() == EventAction.MESSAGE) {
                this.registeredMessages.remove(pa.getId());
            }

            this.disableLinkedExclusiveActivities(pa);
            pa.disableParallelTimerEvents();
            pa.setEnabled(true);

            while(!this.eventQueue.isEmpty()) {
                ProcessActivity po = this.eventQueue.peek();
                if (po.compareTo(pa) != 0) {
                    break;
                }

                po = this.eventQueue.poll();
                po.adjustWorkingTime();
                this.eventQueue.add(po);
            }
        }

        this.simInstance.getClock().setTime(pa.getCompletionTime());
        this.processScheduler.processActivity(pa);
    }



    private void disableLinkedExclusiveActivities(ProcessActivity fromActivity) throws BPSimulatorException, InterruptedException {
        ProcessActivity[] disable = fromActivity.getExclusiveActivities();
        if (disable != null) {
            ProcessActivity[] var3 = disable;
            int var4 = disable.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                ProcessActivity pa = var3[var5];
                if (pa != null && pa != fromActivity) {
                    this.processScheduler.notifyActivityDisabled(pa);
                    this.pendingEvents.remove(pa.getId());
                }
            }

            fromActivity.disableExclusiveEvents();
        }

    }

    private void initBoundaryTimerEvents(ProcessActivity processActivity) throws BPSimulatorException {
        Activity activity = processActivity.getActivity();
        if (activity.getBoundaryTimerEvents() != null) {
            Activity[] boundaryActivities = activity.getBoundaryTimerEvents();
            ProcessActivity[] timerActivities = new ProcessActivity[boundaryActivities.length];

            for(int i = 0; i < timerActivities.length; ++i) {
                timerActivities[i] = new ProcessActivity(processActivity.getProcessInstance(), boundaryActivities[i]);
                timerActivities[i].setCompletionTime(this.simInstance.getClock().getEventTime(timerActivities[i].getDuration()));
                if (boundaryActivities[i].isCancel()) {
                    timerActivities[i].setExclusiveActivities(new ProcessActivity[]{processActivity});
                }
            }

            processActivity.setParallelTimerActivities(timerActivities);
            this.notifyStartedActivities(timerActivities);
        }
    }

    public IProcessScheduler getProcessScheduler() {
        return this.processScheduler;
    }

    public void setProcessScheduler(IProcessScheduler processScheduler) {
        this.processScheduler = processScheduler;
    }


    /**
     * Starts processing event from the queue.
     * @throws BPSimulatorException
     * @throws InterruptedException
     */
    public void processEvents() throws BPSimulatorException, InterruptedException {
        while(!this.eventQueue.isEmpty()) {
            this.processNextActivity();
            this.simInstance.checkInterrupted();
        }

    }

    public void setProcessLogger(IProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

    public IProcessLogger getProcessLogger() {
        return this.processLogger;
    }

    /**
     * Returns already created instance of a message event
     * which was discarded if there exist one and discards the new one.
     * @param eventId
     * @return
     */
    public ProcessActivity getRegisteredMessage(String eventId) {
        ProcessActivity existing = (ProcessActivity)this.registeredMessages.get(eventId);
        return existing;
    }
}
