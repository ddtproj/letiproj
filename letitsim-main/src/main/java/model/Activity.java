package model;


import model.xsd.DistributionInfo;

/**
 * A class that represents an
 * element in the process model.
 * Elements in process instances refer to instances of this class.
 */


public class Activity {
    //unique identifier of activity
    private String id;

    //description
    private String description;

    //
    private String eventCode;

    private int index = -1;


    public enum ActivityType {
        NA,
        EVENT,
        TASK,
        GATEWAY,
        SUB_PROCESS;

        private ActivityType() {
        }
    }


    //type of node (event,gateway, etc)
    private ActivityType type;

    //type of gateway if activity is gateway
    private GatewayType gatewayType;

    //type of event if activity is event
    private EventType eventType;

    // type of event
    private EventAction eventAction;

    //
    private String processId;

    // collaborations between other processes
    private Collaboration[] collaborations;

    private boolean hasIncomingMessageFlow = false;

    private boolean isCancel = false;

    private Activity[] boundaryTimerEvents;

    //resource used by activity
    private Resource resource;

    private DistributionInfo durationDistributionInfo;

    //if event splits the flow (has multiple outgoing flows)
    private boolean split;

    //if event merges the flow (has multiple incoming flows)
    private boolean join;

    private double fixedCost;

    private double costThreshold = 0.0D;

    private double durationThreshold = 0.0D;

    public Activity(String id, int index, String processId) {
        this.setId(id);
        this.setIndex(index);
        this.setProcessId(processId);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.toString()).append(' ');
        if (this.type == ActivityType.GATEWAY) {
            sb.append("gw: ").append(this.gatewayType.toString()).append(' ');
        } else if (this.type == ActivityType.EVENT) {
            sb.append(this.eventType.toString()).append(' ');
        }

        if (this.description != null && !this.description.equals("")) {
            sb.append('(').append(this.description).append(')').append(' ');
        }

        if (!this.id.equals("")) {
            sb.append("id: ").append(this.id).append(' ');
        }

        sb.append("index: ").append(this.index).append(' ');
        return sb.toString();
    }

    public String getDescription() {
        if (this.description == null || this.description.equals("")) {
            this.description = this.type.toString() + " " + this.index;
            if (this.gatewayType != null) {
                this.description = this.description + " " + this.gatewayType.toString();
            } else if (this.eventType != null) {
                this.description = this.description + " " + this.eventType.toString();
            }

            if (this.eventAction != null) {
                this.description = this.description + " " + this.eventAction.toString();
            }
        }

        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityType getType() {
        return this.type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public GatewayType getGatewayType() {
        return this.gatewayType;
    }

    public void setGatewayType(GatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventAction getEventAction() {
        return this.eventAction;
    }

    public void setEventAction(EventAction eventAction) {
        this.eventAction = eventAction;
    }

    public String getEventCode() {
        return this.eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getProcessId() {
        return this.processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setCollaborations(Collaboration[] collaborations) {
        this.collaborations = collaborations;
    }

    public Collaboration[] getCollaborations() {
        return this.collaborations;
    }

    public boolean hasIncomingMessageFlow() {
        return this.hasIncomingMessageFlow;
    }

    public void setHasIncomingMessageFlow(boolean hasIncomingMessageFlow) {
        this.hasIncomingMessageFlow = hasIncomingMessageFlow;
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    public boolean isCancel() {
        return this.isCancel;
    }

    public void setBoundaryTimerEvents(Activity[] boundaryTimerEvents) {
        this.boundaryTimerEvents = boundaryTimerEvents;
    }

    public Activity[] getBoundaryTimerEvents() {
        return this.boundaryTimerEvents;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return this.resource;
    }

    public void setDurationDistributionInfo(DistributionInfo durationDistributionInfo) {
        this.durationDistributionInfo = durationDistributionInfo;
    }

    public DistributionInfo getDurationDistributionInfo() {
        return this.durationDistributionInfo;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public boolean isSplit() {
        return this.split;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public boolean isJoin() {
        return this.join;
    }

    public void setFixedCost(double fixedCost) {
        this.fixedCost = fixedCost;
    }

    public double getFixedCost() {
        return this.fixedCost;
    }

    public double getCostThreshold() {
        return this.costThreshold;
    }

    public void setCostThreshold(double costThreshold) {
        this.costThreshold = costThreshold;
    }

    public double getDurationThreshold() {
        return this.durationThreshold;
    }

    public void setDurationThreshold(double durationThreshold) {
        this.durationThreshold = durationThreshold;
    }
}
