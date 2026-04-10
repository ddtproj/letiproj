package model.xsd;


import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ElementSimulationInfoType",
        propOrder = {"durationDistribution", "resourceIds"}
)
public class ElementSimulationInfoType {
    @XmlElement(
            required = true
    )
    protected DistributionInfo durationDistribution;
    @XmlElement(
            required = true
    )
    protected ElementSimulationInfoType.ResourceIds resourceIds;
    @XmlAttribute(
            name = "id"
    )
    protected String id;
    @XmlAttribute(
            name = "elementId",
            required = true
    )
    protected String elementId;
    @XmlAttribute(
            name = "fixedCost"
    )
    protected Double fixedCost;
    @XmlAttribute(
            name = "costThreshold"
    )
    protected Double costThreshold;
    @XmlAttribute(
            name = "durationThreshold"
    )
    protected Double durationThreshold;
    @XmlAttribute(
            name = "simulateAsTask"
    )
    protected Boolean simulateAsTask;

    public ElementSimulationInfoType() {
    }

    public DistributionInfo getDurationDistribution() {
        return this.durationDistribution;
    }

    public void setDurationDistribution(DistributionInfo value) {
        this.durationDistribution = value;
    }

    public ElementSimulationInfoType.ResourceIds getResourceIds() {
        return this.resourceIds;
    }

    public void setResourceIds(ElementSimulationInfoType.ResourceIds value) {
        this.resourceIds = value;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String value) {
        this.elementId = value;
    }

    public Double getFixedCost() {
        return this.fixedCost;
    }

    public void setFixedCost(Double value) {
        this.fixedCost = value;
    }

    public Double getCostThreshold() {
        return this.costThreshold;
    }

    public void setCostThreshold(Double value) {
        this.costThreshold = value;
    }

    public Double getDurationThreshold() {
        return this.durationThreshold;
    }

    public void setDurationThreshold(Double value) {
        this.durationThreshold = value;
    }

    public Boolean isSimulateAsTask() {
        return this.simulateAsTask;
    }

    public void setSimulateAsTask(Boolean value) {
        this.simulateAsTask = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"resourceId"}
    )
    public static class ResourceIds {
        @XmlElement(
                required = true
        )
        protected String resourceId;

        public ResourceIds() {
        }

        public String getResourceId() {
            return this.resourceId;
        }

        public void setResourceId(String value) {
            this.resourceId = value;
        }
    }
}
