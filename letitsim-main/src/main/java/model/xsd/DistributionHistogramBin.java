package model.xsd;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "DistributionHistogramBin",
        propOrder = {"distribution"}
)
public class DistributionHistogramBin {
    @XmlElement(
            required = true
    )
    protected DistributionInfo distribution;
    @XmlAttribute(
            name = "probability"
    )
    protected Double probability;

    public DistributionHistogramBin() {
    }

    public DistributionInfo getDistribution() {
        return this.distribution;
    }

    public void setDistribution(DistributionInfo value) {
        this.distribution = value;
    }

    public Double getProbability() {
        return this.probability;
    }

    public void setProbability(Double value) {
        this.probability = value;
    }
}
