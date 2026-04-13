package model.xsd;


import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "ProcessSimulationInfoType",
        propOrder = {"arrivalRateDistribution", "timetables", "resources", "elements", "sequenceFlows", "any"}
)
@XmlSeeAlso({ProcessSimulationInfo.class})
public class ProcessSimulationInfoType {
    @XmlElement(
            required = true
    )
    protected DistributionInfo arrivalRateDistribution;
    protected ProcessSimulationInfoType.Timetables timetables;
    protected ProcessSimulationInfoType.Resources resources;
    protected ProcessSimulationInfoType.Elements elements;
    protected ProcessSimulationInfoType.SequenceFlows sequenceFlows;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(
            name = "id"
    )
    protected String id;
    @XmlAttribute(
            name = "processId"
    )
    protected String processId;
    @XmlAttribute(
            name = "processInstances",
            required = true
    )
    protected int processInstances;
    @XmlAttribute(
            name = "startDateTime"
    )
    @XmlSchemaType(
            name = "dateTime"
    )
    protected XMLGregorianCalendar startDateTime;
    @XmlAttribute(
            name = "currency"
    )
    protected String currency;
    @XmlAttribute(
            name = "version"
    )
    protected Integer version;

    public ProcessSimulationInfoType() {
    }

    public DistributionInfo getArrivalRateDistribution() {
        return this.arrivalRateDistribution;
    }

    public void setArrivalRateDistribution(DistributionInfo value) {
        this.arrivalRateDistribution = value;
    }

    public ProcessSimulationInfoType.Timetables getTimetables() {
        return this.timetables;
    }

    public void setTimetables(ProcessSimulationInfoType.Timetables value) {
        this.timetables = value;
    }

    public ProcessSimulationInfoType.Resources getResources() {
        return this.resources;
    }

    public void setResources(ProcessSimulationInfoType.Resources value) {
        this.resources = value;
    }

    public ProcessSimulationInfoType.Elements getElements() {
        return this.elements;
    }

    public void setElements(ProcessSimulationInfoType.Elements value) {
        this.elements = value;
    }

    public ProcessSimulationInfoType.SequenceFlows getSequenceFlows() {
        return this.sequenceFlows;
    }

    public void setSequenceFlows(ProcessSimulationInfoType.SequenceFlows value) {
        this.sequenceFlows = value;
    }

    public List<Object> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }

        return this.any;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getProcessId() {
        return this.processId;
    }

    public void setProcessId(String value) {
        this.processId = value;
    }

    public int getProcessInstances() {
        return this.processInstances;
    }

    public void setProcessInstances(int value) {
        this.processInstances = value;
    }

    public XMLGregorianCalendar getStartDateTime() {
        return this.startDateTime;
    }

    public void setStartDateTime(XMLGregorianCalendar value) {
        this.startDateTime = value;
    }

    public String getCurrency() {
        return this.currency == null ? "EUR" : this.currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer value) {
        this.version = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"timetable"}
    )
    public static class Timetables {
        @XmlElement(
                required = true
        )
        protected List<TimeTable> timetable;

        public Timetables() {
        }

        public List<TimeTable> getTimetable() {
            if (this.timetable == null) {
                this.timetable = new ArrayList();
            }

            return this.timetable;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"sequenceFlow"}
    )
    public static class SequenceFlows {
        @XmlElement(
                required = true
        )
        protected List<SequenceFlowSimulationInfoType> sequenceFlow;

        public SequenceFlows() {
        }

        public List<SequenceFlowSimulationInfoType> getSequenceFlow() {
            if (this.sequenceFlow == null) {
                this.sequenceFlow = new ArrayList();
            }

            return this.sequenceFlow;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"resource"}
    )
    public static class Resources {
        @XmlElement(
                required = true
        )
        protected List<Resource> resource;

        public Resources() {
        }

        public List<Resource> getResource() {
            if (this.resource == null) {
                this.resource = new ArrayList();
            }

            return this.resource;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"element"}
    )
    public static class Elements {
        @XmlElement(
                required = true
        )
        protected List<ElementSimulationInfoType> element;

        public Elements() {
        }

        public List<ElementSimulationInfoType> getElement() {
            if (this.element == null) {
                this.element = new ArrayList();
            }

            return this.element;
        }
    }
}
