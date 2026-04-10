package model.xsd;


import model.WeekDay;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TimeTableRule"
)
public class TimeTableRule {
    @XmlAttribute(
            name = "fromTime",
            required = true
    )
    @XmlSchemaType(
            name = "time"
    )
    protected XMLGregorianCalendar fromTime;
    @XmlAttribute(
            name = "toTime",
            required = true
    )
    @XmlSchemaType(
            name = "time"
    )
    protected XMLGregorianCalendar toTime;
    @XmlAttribute(
            name = "fromWeekDay",
            required = true
    )
    protected WeekDay fromWeekDay;
    @XmlAttribute(
            name = "toWeekDay"
    )
    protected WeekDay toWeekDay;

    public TimeTableRule() {
    }

    public XMLGregorianCalendar getFromTime() {
        return this.fromTime;
    }

    public void setFromTime(XMLGregorianCalendar value) {
        this.fromTime = value;
    }

    public XMLGregorianCalendar getToTime() {
        return this.toTime;
    }

    public void setToTime(XMLGregorianCalendar value) {
        this.toTime = value;
    }

    public WeekDay getFromWeekDay() {
        return this.fromWeekDay;
    }

    public void setFromWeekDay(WeekDay value) {
        this.fromWeekDay = value;
    }

    public WeekDay getToWeekDay() {
        return this.toWeekDay;
    }

    public void setToWeekDay(WeekDay value) {
        this.toWeekDay = value;
    }
}
