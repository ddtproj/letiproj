package logparser.dto;

import java.util.Map;

public class ProcessStateDto extends LogEntryDto {
    public String eventTime;
    public Double idleSeconds;

    public Map<String, Object> toMap() {
        Map<String, Object> map = baseMap();
        map.put("eventTime", eventTime);
        map.put("idleSeconds", idleSeconds);
        return map;
    }
}
