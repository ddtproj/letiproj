package model;


public class TokenFlow{
    private int index;
    private Integer targetActivityIndex;
    private double probability;


    public TokenFlow(Integer targetActivityIndex, int index) {
        this.index = index;
        this.targetActivityIndex = targetActivityIndex;
    }


    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public void setTargetActivityIndex(Integer targetActivityIndex) {
        this.targetActivityIndex = targetActivityIndex;
    }

    public Integer getTargetActivityIndex() {
        return this.targetActivityIndex;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getProbability() {
        return this.probability;
    }
}
