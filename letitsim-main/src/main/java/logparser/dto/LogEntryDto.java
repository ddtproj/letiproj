package logparser.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class LogEntryDto {
    public int lineNumber;
    public LogEntryType entryType;
    public Integer processId;
    public String rawLine;

    protected Map<String, Object> baseMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("lineNumber", lineNumber);
        map.put("entryType", entryType == null ? null : entryType.name());
        map.put("processId", processId);
        map.put("rawLine", rawLine);
        return map;
    }

    public abstract Map<String, Object> toMap();
}
