package model;



public class TaskResource {
    private Resource resource;
    private TaskResource next;
    private int id;

    public TaskResource(Resource resource, int id) {
        this.resource = resource;
        this.id = id;
    }

    public String toString() {
        return this.resource.toString() + "id: " + this.id;
    }

    public Resource getResource() {
        return this.resource;
    }

    public int getId() {
        return this.id;
    }

    public void setNext(TaskResource next) {
        this.next = next;
    }

    public TaskResource getNext() {
        return this.next;
    }
}
