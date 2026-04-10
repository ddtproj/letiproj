package model.xsd;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "SequenceFlowSimulationInfoType"
)
public class SequenceFlowSimulationInfoType {
    @XmlAttribute(
            name = "elementId",
            required = true
    )
    protected String elementId;
    @XmlAttribute(
            name = "executionProbability"
    )
    protected Double executionProbability;

    public SequenceFlowSimulationInfoType() {
    }

    public String getElementId() {
        return this.elementId;
    }

    public void setElementId(String value) {
        this.elementId = value;
    }

    public Double getExecutionProbability() {
        return this.executionProbability;
    }

    public void setExecutionProbability(Double value) {
        this.executionProbability = value;
    }
}
