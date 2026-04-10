package model.xsd;



import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TimeTable",
        propOrder = {"rules"}
)
public class TimeTable {
    @XmlElement(
            required = true
    )
    protected TimeTable.Rules rules;
    @XmlAttribute(
            name = "id",
            required = true
    )
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(
            name = "ID"
    )
    protected String id;
    @XmlAttribute(
            name = "default"
    )
    protected Boolean _default;
    @XmlAttribute(
            name = "name"
    )
    protected String name;

    public TimeTable() {
    }

    public TimeTable.Rules getRules() {
        return this.rules;
    }

    public void setRules(TimeTable.Rules value) {
        this.rules = value;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public Boolean isDefault() {
        return this._default;
    }

    public void setDefault(Boolean value) {
        this._default = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder = {"rule"}
    )
    public static class Rules {
        @XmlElement(
                required = true
        )
        protected List<TimeTableRule> rule;

        public Rules() {
        }

        public List<TimeTableRule> getRule() {
            if (this.rule == null) {
                this.rule = new ArrayList();
            }

            return this.rule;
        }
    }
}
