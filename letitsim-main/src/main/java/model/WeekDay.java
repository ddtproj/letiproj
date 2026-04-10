package model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "WeekDay")
@XmlEnum
public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    private WeekDay() {
    }

    public String value()
    {
        return this.name();
    }

    public static WeekDay fromValue(String v)
    {
        return valueOf(v);
    }

}
