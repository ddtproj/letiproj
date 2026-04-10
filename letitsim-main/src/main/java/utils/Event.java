package utils;

class Event {
    private ProcessEventType type;
    private double time;

    public Event(ProcessEventType type, double time) {
        this.type = type;
        this.time = time;
    }

    public double getTime() {
        return this.time;
    }

    public ProcessEventType getType() {
        return this.type;
    }
}
