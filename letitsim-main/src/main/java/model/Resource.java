package model;

public class Resource extends model.xsd.Resource {
    private Integer index;
    private TimeTable timeTable;

    public Resource(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Resource(model.xsd.Resource sourceResource) {
        this.setId(sourceResource.getId());
        this.setName(sourceResource.getName());
        this.setTotalAmount(sourceResource.getTotalAmount());
        this.setCostPerHour(sourceResource.getCostPerHour());
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String toString() {
        return "Resource " + this.name + " id: " + this.id + " index: " + this.index;
    }

    public TimeTable getTimeTable() {
        return this.timeTable;
    }

    public void setTimeTable(TimeTable timeTable) {
        this.timeTable = timeTable;
    }
}
