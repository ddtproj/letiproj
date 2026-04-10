package model.xsd;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Resource")
public class Resource {

    @XmlAttribute(name = "id",required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;


    @XmlAttribute(name = "name")
    protected String name;

    @XmlAttribute(name = "totalAmount")
    protected Integer totalAmount;

    @XmlAttribute(name = "costPerHour")
    protected Double costPerHour;

    @XmlAttribute(name = "timetableId")
    protected String timetableId;

    public Resource() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Integer getTotalAmount() {
        return this.totalAmount;
    }

    public void setTotalAmount(Integer value) {
        this.totalAmount = value;
    }

    public Double getCostPerHour() {
        return this.costPerHour;
    }

    public void setCostPerHour(Double value) {
        this.costPerHour = value;
    }

    public String getTimetableId() {
        return this.timetableId;
    }

    public void setTimetableId(String value) {
        this.timetableId = value;
    }
}
