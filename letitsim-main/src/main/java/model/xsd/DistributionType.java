package model.xsd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DistributionType")
@XmlEnum
public enum DistributionType {
    FIXED,
    EXPONENTIAL,
    GAMMA,
    LOGNORMAL,
    NORMAL,
    TRIANGULAR,
    UNIFORM,
    HISTOGRAM;

    private DistributionType() {
    }

    public String value() {
        return this.name();
    }

    public static DistributionType fromValue(String v) {
        return valueOf(v);
    }
}
