package parser;


import engine.exception.ModelParseException;
import engine.exception.ProcessValidationException;
import utils.Graph;

import java.util.List;

/**
 * General interface for BPMN parsers with BPSim support
 *
 */



import model.Collaboration;
import model.EventAction;
import model.EventType;
import model.Resource;
import model.TimeTable;
import model.xsd.DistributionInfo;
import utils.Graph;
import java.io.InputStream;
import java.util.List;



public interface IProcessModelParser {

    //bpmn graph model (control flow perspective)
    Graph getGraph();


    //??
    Collaboration[] getCollaborations();


    int getErrorHandlerActivity(int var1, int var2, String var3);

    EventAction getEventAction(Integer evActionId);

    EventType getEventType(Integer exTypeId);

    String getModelElementId(Integer modelElementId);

    Integer getParentBoundaryActivity(Integer activityId);

    String getProcessId(Integer processId);

    String getElementSimpleName(Integer var1);

    Integer[] getAllStartEventIndexes();

    Integer[] getStartEventIndexesForMainFile();

    Integer[] getSubProcessStartActivityIndexes(Integer var1);

    //
    boolean isInterruptingEvent(Integer var1);

    boolean isChoice(Integer var1);

    boolean isEventGateway(Integer var1);

    boolean isParallel(Integer var1);

    boolean isOR(Integer var1);

    boolean isTask(String var1);

    boolean isSubProcess(Integer var1);

    List<Resource> getAllResources() throws ProcessValidationException;

    DistributionInfo getArrivalRateDistributionInfo();

    double getEdgeProbability(String var1) throws ProcessValidationException;

    DistributionInfo getElementDurationInformation(String var1);

    String getTaskResourceId(String var1);

    int getTotalProcessInstances();

    double getStartTime() throws ProcessValidationException;

     TimeTable getArrivalRateTimeTable() throws ProcessValidationException;

    double getElementFixedCost(String var1);

    double getElementCostThreshold(String var1);

    double getElementDurationThreshold(String var1);

    void parse() throws ModelParseException, ProcessValidationException;

    boolean isApplicable();

    void setInputStreams(List<InputStream> var1);

    void setFiles(List<String> var1);
}
