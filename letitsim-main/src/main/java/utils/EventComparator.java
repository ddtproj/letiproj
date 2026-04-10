package utils;

import java.util.Comparator;

class EventComparator implements Comparator<Event> {
    EventComparator() {
    }

    public int compare(Event o1, Event o2) {
        return o1.getTime() == o2.getTime() ? o1.getType().ordinal() - o2.getType().ordinal() : (int)Math.signum(o1.getTime() - o2.getTime());
    }
}
