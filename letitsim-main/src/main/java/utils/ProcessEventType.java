package utils;


public enum ProcessEventType {
    ProcessStart,
    EnabledStart,
    EnabledStop,
    WorkStart,
    WorkStop,
    ProcessEnd;

    private ProcessEventType() {
    }
}
