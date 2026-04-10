package model;


import engine.exception.ProcessValidationException;

import javax.xml.datatype.XMLGregorianCalendar;

public class TimeTableRule {
    private int day;
    private int timeFrom;
    private int timeTo;

    public TimeTableRule() {
    }

    public void init(WeekDay forDay, XMLGregorianCalendar fromTime, XMLGregorianCalendar toTime) throws ProcessValidationException {
        switch(forDay) {
            case FRIDAY:
                this.day = 6;
                break;
            case MONDAY:
                this.day = 2;
                break;
            case SATURDAY:
                this.day = 7;
                break;
            case SUNDAY:
                this.day = 1;
                break;
            case THURSDAY:
                this.day = 5;
                break;
            case TUESDAY:
                this.day = 3;
                break;
            case WEDNESDAY:
                this.day = 4;
                break;
            default:
                this.day = 0;
        }

        this.timeFrom = fromTime.getHour() * 3600 + 60 * fromTime.getMinute() + fromTime.getSecond();
        this.timeTo = toTime.getHour() * 3600 + 60 * toTime.getMinute() + toTime.getSecond();
        if (this.timeTo <= this.timeFrom) {
            throw new ProcessValidationException("Invalid timetable rule: to time must be greater than from time");
        }
    }

    public TimeTableRule(int day, int timeFromSeconds, int timeToSeconds) {
        this.day = day;
        this.timeFrom = timeFromSeconds;
        this.timeTo = timeToSeconds;
    }

    public int getDay() {
        return this.day;
    }

    public int getTimeFrom() {
        return this.timeFrom;
    }

    public int getTimeTo() {
        return this.timeTo;
    }
}
