package parser.xml;


import engine.exception.ModelParseException;
import engine.exception.ProcessValidationException;
import model.Resource;
import model.TimeTable;
import model.xsd.DistributionInfo;
import model.xsd.ElementSimulationInfoType;
import model.xsd.ProcessSimulationInfo;
import model.xsd.SequenceFlowSimulationInfoType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;
import parser.BPMN2Parser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import java.util.*;

public class BPMN2XmlParser extends BPMN2Parser {
    public static final Namespace QBP_NS = Namespace.getNamespace("http://www.qbp-simulator.com/Schema201212");
    private static final String MODEL_SIMULATION_INFO_NODE = "processSimulationInfo";
    private static final String TIMETABLE_247_ID = "QBP_247_TIMETABLE";
    private ProcessSimulationInfo modelSimulationInfo;
    private Map<String, ElementSimulationInfoType> elementSimulationInfos = new HashMap();
    private Map<String, SequenceFlowSimulationInfoType> sequenceFlowSimulationInfos = new HashMap();

    public BPMN2XmlParser() {
    }

    public void parse() throws ModelParseException, ProcessValidationException {
        this.loadModelSimulationInfo();
        super.parse();
    }

    private void loadModelSimulationInfo() throws ModelParseException, ProcessValidationException {
        try {
            JAXBContext modelContext = JAXBContext.newInstance(ProcessSimulationInfo.class);
            Unmarshaller modelUnmarshaller = modelContext.createUnmarshaller();
            modelUnmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

            Iterator var3 = this.getDocuments().iterator();

            while(var3.hasNext()) {
                Document document = (Document)var3.next();
                Element simInfo = document.getRootElement().getChild("processSimulationInfo", QBP_NS);
                Source src = new JDOMSource(simInfo);
                if (simInfo != null) {


                    ProcessSimulationInfo simulationInfo = modelUnmarshaller.unmarshal(src,ProcessSimulationInfo.class).getValue();


                    String processId = simulationInfo.getProcessId();
                    if (processId == null) {
                        Element processElem = document.getRootElement().getChild("process", document.getRootElement().getNamespace());
                        if (processElem != null) {
                            processId = processElem.getAttributeValue("id");
                        }
                    }

                    if (this.modelSimulationInfo == null) {
                        this.modelSimulationInfo = simulationInfo;
                    }

                    Iterator var13;
                    if (simulationInfo.getElements() != null) {
                        var13 = simulationInfo.getElements().getElement().iterator();

                        while(var13.hasNext()) {
                            ElementSimulationInfoType el = (ElementSimulationInfoType)var13.next();
                            if (el.getElementId() != null) {
                                this.elementSimulationInfos.put(uniqueId(processId, el.getElementId()), el);
                            }
                        }
                    }

                    if (simulationInfo.getSequenceFlows() != null) {
                        var13 = simulationInfo.getSequenceFlows().getSequenceFlow().iterator();

                        while(var13.hasNext()) {
                            SequenceFlowSimulationInfoType el = (SequenceFlowSimulationInfoType)var13.next();
                            if (el.getElementId() != null) {
                                this.sequenceFlowSimulationInfos.put(uniqueId(processId, el.getElementId()), el);
                            }
                        }
                    }
                }
            }
        } catch (Exception var11) {
            throw new ModelParseException(var11.getMessage());
        }

        if (this.modelSimulationInfo == null) {
            throw new ProcessValidationException("Model simulation info not found");
        }
    }

    public List<Resource> getAllResources() throws ProcessValidationException {
        if (this.modelSimulationInfo != null && this.modelSimulationInfo.getResources() != null && this.modelSimulationInfo.getResources().getResource().size() > 0) {
            List<Resource> list = new ArrayList(this.modelSimulationInfo.getResources().getResource().size());

            Resource resource;
            for(Iterator var2 = this.modelSimulationInfo.getResources().getResource().iterator(); var2.hasNext(); list.add(resource)) {
                model.xsd.Resource r = (model.xsd.Resource)var2.next();
                resource = new Resource(r);
                if (r.getTimetableId() != null) {
                    if (!r.getTimetableId().equals("QBP_247_TIMETABLE")) {
                        TimeTable t = new TimeTable();
                        t.loadFrom(this.getTimeTableByResourceId(r.getTimetableId()));
                        if (t.getLocalRules() == null || t.getLocalRules().size() == 0) {
                            throw new ProcessValidationException("Invalid time table - no valid rules found");
                        }

                        resource.setTimeTable(t);
                    }
                } else {
                    resource.setTimeTable(this.getArrivalRateTimeTable());
                }
            }

            return list;
        } else {
            throw new ProcessValidationException("Resources not found from the model");
        }
    }

    public DistributionInfo getArrivalRateDistributionInfo() {
        return this.modelSimulationInfo.getArrivalRateDistribution();
    }

    public DistributionInfo getElementDurationInformation(String elementId) {
        return this.elementSimulationInfos.containsKey(elementId) ? ((ElementSimulationInfoType)this.elementSimulationInfos.get(elementId)).getDurationDistribution() : null;
    }

    public String getTaskResourceId(String activityId) {
        return this.elementSimulationInfos.containsKey(activityId) ? ((ElementSimulationInfoType)this.elementSimulationInfos.get(activityId)).getResourceIds().getResourceId() : null;
    }

    public int getTotalProcessInstances() {
        return this.modelSimulationInfo.getProcessInstances();
    }

    public double getStartTime() throws ProcessValidationException {
        if (this.modelSimulationInfo.getStartDateTime() == null) {
            throw new ProcessValidationException("Scenario start timestamp is required");
        } else {
            Calendar c = this.modelSimulationInfo.getStartDateTime().toGregorianCalendar();
            c.setTimeZone(TimeZone.getDefault());
            return (double)(c.getTimeInMillis() / 1000L);
        }
    }

    public TimeTable getArrivalRateTimeTable() throws ProcessValidationException {
        Iterator var1 = this.modelSimulationInfo.getTimetables().getTimetable().iterator();

        model.xsd.TimeTable tt;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            tt = (model.xsd.TimeTable)var1.next();
        } while(!tt.isDefault());

        TimeTable t = new TimeTable();
        t.loadFrom(tt);
        if (t.getLocalRules() != null && t.getLocalRules().size() != 0) {
            return t;
        } else {
            throw new ProcessValidationException("Invalid arrival rate time table - no valid rules found");
        }
    }

    public double getElementFixedCost(String elementId) {
        ElementSimulationInfoType tsi = this.getElementSimulationInfo(elementId);
        return tsi != null && tsi.getFixedCost() != null ? tsi.getFixedCost() : 0.0D;
    }

    public double getElementCostThreshold(String elementId) {
        ElementSimulationInfoType tsi = this.getElementSimulationInfo(elementId);
        return tsi != null && tsi.getCostThreshold() != null ? tsi.getCostThreshold() : -1.0D;
    }

    public double getElementDurationThreshold(String elementId) {
        ElementSimulationInfoType tsi = this.getElementSimulationInfo(elementId);
        return tsi != null && tsi.getDurationThreshold() != null ? tsi.getDurationThreshold() : -1.0D;
    }

    private ElementSimulationInfoType getElementSimulationInfo(String elementId) {
        return this.elementSimulationInfos.containsKey(elementId) ? (ElementSimulationInfoType)this.elementSimulationInfos.get(elementId) : null;
    }

    public boolean isApplicable() {
        try {
            this.loadDocuments();
            Iterator var1 = this.getDocuments().iterator();

            Document document;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                document = (Document)var1.next();
            } while(document.getRootElement().getChild("processSimulationInfo", QBP_NS) == null);

            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    private model.xsd.TimeTable getTimeTableByResourceId(Object timeTableId) {
        Iterator var2 = this.modelSimulationInfo.getTimetables().getTimetable().iterator();

        model.xsd.TimeTable tt;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            tt = (model.xsd.TimeTable)var2.next();
        } while(!tt.getId().equals(timeTableId));

        return tt;
    }

    public double getEdgeProbability(String edgeId) {
        SequenceFlowSimulationInfoType inf = (SequenceFlowSimulationInfoType)this.sequenceFlowSimulationInfos.get(edgeId);
        return inf != null ? inf.getExecutionProbability() : 1.0D;
    }

    protected boolean simulateSubProcessAsTask(String subProcessId) {
        ElementSimulationInfoType simInfo = this.getElementSimulationInfo(subProcessId);
        return simInfo != null && simInfo.isSimulateAsTask() != null && simInfo.isSimulateAsTask();
    }
}
