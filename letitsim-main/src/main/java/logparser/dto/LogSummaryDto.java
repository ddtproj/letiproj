package logparser.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class LogSummaryDto {
    public int totalRecords;
    public int distinctProcessIds;
    public String firstEventTime;
    public String lastFinishTime;
    public int unknownLines;
    public Map<String, Integer> countsByType = new LinkedHashMap<String, Integer>();

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("totalRecords", totalRecords);
        map.put("distinctProcessIds", distinctProcessIds);
        map.put("firstEventTime", firstEventTime);
        map.put("lastFinishTime", lastFinishTime);
        map.put("unknownLines", unknownLines);
        map.put("countsByType", countsByType);
        return map;
    }
}
