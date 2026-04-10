package model;


/**
 *Class which represents a collaboration between two events in different processes.
 */
public class Collaboration {

    private String sourceProcessId;
    private String targetProcessId;
    private String name;
    private Integer targetActivityIndex;
    private Integer sourceActivityIndex;
    private Activity targetActivity;
    private Activity sourceActivity;


    public Collaboration(String name, String sourceProcessId, String targetProcessId, Integer sourceActivityIndex, Integer targetActivityIndex) {
        this.name = name;
        this.sourceProcessId = sourceProcessId;
        this.targetProcessId = targetProcessId;
        this.setSourceActivityIndex(sourceActivityIndex);
        this.setTargetActivityIndex(targetActivityIndex);
    }

    public String getSourceProcessId() {
        return this.sourceProcessId;
    }

    public void setSourceProcessId(String sourceProcessId) {
        this.sourceProcessId = sourceProcessId;
    }

    public String getTargetProcessId() {
        return this.targetProcessId;
    }

    public void setTargetProcessId(String targetProcessId) {

        this.targetProcessId = targetProcessId;
    }

    public Activity getTargetActivity() {

        return this.targetActivity;
    }

    public void setTargetActivity(Activity targetActivity) {
        this.targetActivity = targetActivity;
    }

    public void setSourceActivity(Activity sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    public Activity getSourceActivity() {
        return this.sourceActivity;
    }

    public void setSourceActivityIndex(Integer sourceActivityIndex) {
        this.sourceActivityIndex = sourceActivityIndex;
    }

    public Integer getSourceActivityIndex() {
        return this.sourceActivityIndex;
    }

    public void setTargetActivityIndex(Integer targetActivityIndex) {
        this.targetActivityIndex = targetActivityIndex;
    }

    public Integer getTargetActivityIndex() {
        return this.targetActivityIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
