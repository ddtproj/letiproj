package utils;


import java.util.ArrayList;
import java.util.Collections;

public class ProcessEventTimeline {

    private ArrayList<Event> events;
    private boolean sorted = false;
    private double totalIdleTime;
    private double totalWaitingTime;
    private double cachedUntil;
    private static final EventComparator comparator = new EventComparator();

    public ProcessEventTimeline(int capacity) {
        this.events = new ArrayList(capacity);
    }

    public void addEvent(ProcessEventType state, double time) {
        Event e = new Event(state, time);
        this.events.add(e);
        this.sorted = false;
    }

    private void sortEvents() {
        if (!this.sorted) {
            Collections.sort(this.events, comparator);
            this.sorted = true;
        }

    }




    private void calculateTimes(double until) {
        if (this.cachedUntil != until) {
            this.cachedUntil = until;
            this.sortEvents();
            this.totalIdleTime = 0.0D;
            this.totalWaitingTime = 0.0D;
            double lastIdle = 0.0D;
            int busyCount = 0;

            for(int i = 0; i < this.events.size(); ++i) {
                Event e = (Event)this.events.get(i);
                if (e.getTime() > until) {
                    if (busyCount == 0) {
                        this.totalIdleTime += until - lastIdle;
                    }

                    return;
                }

                switch(e.getType()) {
                    case EnabledStart:
                    case WorkStart:
                        ++busyCount;
                        if (busyCount == 1) {
                            this.totalIdleTime += e.getTime() - lastIdle;
                        }
                        break;
                    case EnabledStop:
                    case WorkStop:
                        --busyCount;
                        if (busyCount == 0) {
                            lastIdle = e.getTime();
                        }
                        break;
                    case ProcessStart:
                        lastIdle = e.getTime();
                        break;
                    case ProcessEnd:
                        this.totalIdleTime += e.getTime() - lastIdle;
                }
            }

        }
    }

    public double getTotalIdleTime(double until) {
        this.calculateTimes(until);
        return this.totalIdleTime;
    }
}
