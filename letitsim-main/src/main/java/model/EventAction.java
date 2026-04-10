package model;


// type of Event
public enum EventAction {
    NA,
    ERROR,
    TIMER,
    MESSAGE,
    TERMINATE;


    private EventAction() {
    }
}
