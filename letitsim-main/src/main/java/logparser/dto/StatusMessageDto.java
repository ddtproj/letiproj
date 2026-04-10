package logparser.dto;

import java.util.Map;

public class StatusMessageDto extends LogEntryDto {
    public String message;
    public String eventTime;

    public Map<String, Object> toMap() {
        Map<String, Object> map = baseMap();
        map.put("message", message);
        map.put("eventTime", eventTime);
        return map;
    }
}
