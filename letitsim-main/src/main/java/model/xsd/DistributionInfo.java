package model.xsd;



import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "DistributionInfo",
        propOrder = {"timeUnit", "histogramDataBins", "any"}
)
public class DistributionInfo {

    protected String timeUnit;
    protected DistributionInfo.HistogramDataBins histogramDataBins;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
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

    public String getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(String value) {
        this.timeUnit = value;
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

    public List<Object> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }

        return this.any;
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
