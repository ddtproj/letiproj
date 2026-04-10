package model.xsd;



import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "DistributionInfo",
        propOrder = {"histogramDataBins"}
)
public class DistributionInfo {

    protected DistributionInfo.HistogramDataBins histogramDataBins;
    @XmlAttribute(name = "type", required = true)
    protected DistributionType type;


    @XmlAttribute( name = "mean")
    protected Double mean;
    @XmlAttribute(
            name = "arg1"
    )
    protected Double arg1;
    @XmlAttribute(
            name = "arg2"
    )
    protected Double arg2;

    public DistributionInfo() {
    }

    public DistributionInfo.HistogramDataBins getHistogramDataBins() {
        return this.histogramDataBins;
    }

    public void setHistogramDataBins(DistributionInfo.HistogramDataBins value) {
        this.histogramDataBins = value;
    }

    public DistributionType getType() {
        return this.type;
    }

    public void setType(DistributionType value) {
        this.type = value;
    }

    public Double getMean() {
        return this.mean;
    }

    public void setMean(Double value) {
        this.mean = value;
    }

    public Double getArg1() {
        return this.arg1;
    }

    public void setArg1(Double value) {
        this.arg1 = value;
    }

    public Double getArg2() {
        return this.arg2;
    }

    public void setArg2(Double value) {
        this.arg2 = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"histogramData"}
    )
    public static class HistogramDataBins {
        @XmlElement(
                required = true
        )
        protected List<DistributionHistogramBin> histogramData;

        public HistogramDataBins() {
        }

        public List<DistributionHistogramBin> getHistogramData() {
            if (this.histogramData == null) {
                this.histogramData = new ArrayList();
            }

            return this.histogramData;
        }
    }
}
