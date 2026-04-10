package utils;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Class representing the post-condition table
 *
 *
 */
public class PostConditionTable {
    private Map<Integer, BitSet> map = new HashMap<>();

    public PostConditionTable() {
    }

    /**
     * Returns an array of outgoing flow indexes
     * @param eventId - element index
     */
    public int[] getFlowsByEventId(Integer eventId) {

        BitSet postIndexes = this.map.get(eventId);
        int[] postActivities;
        if (postIndexes == null) {
            postActivities = new int[0];
        } else {
            postActivities = new int[postIndexes.cardinality()];
            int n = 0;

            for(int i = postIndexes.nextSetBit(0); i >= 0; i = postIndexes.nextSetBit(i + 1)) {
                postActivities[n++] = i;
            }
        }

        return postActivities;
    }


    /**
     * Adds a flow to the table
     * @param fromIndex
     * @param tokenFlowIndex
     */
    public void add(Integer fromIndex, int tokenFlowIndex) {
        if (!this.map.containsKey(fromIndex)) {
            this.map.put(fromIndex, new BitSet());
        }

        ((BitSet)this.map.get(fromIndex)).set(tokenFlowIndex);
    }
}
