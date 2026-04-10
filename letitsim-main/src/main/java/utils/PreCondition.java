package utils;

import model.Activity;

import java.util.BitSet;

public class PreCondition {
    private Activity activity;
    private BitSet condition;

    /**
     * Precondition of process activity
     * @param activity
     * @param condition
     */
    public PreCondition(Activity activity, BitSet condition) {
        this.activity = activity;
        this.condition = condition;
    }

    public PreCondition(Activity activity) {
        this(activity, new BitSet());
    }

    public Activity getActivity() {
        return this.activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public BitSet getCondition() {
        return this.condition;
    }

    public void setConditions(BitSet conditions) {
        this.condition = conditions;
    }
}
