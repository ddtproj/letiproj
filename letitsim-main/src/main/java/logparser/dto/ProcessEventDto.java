package logparser.dto;

import java.util.Map;

public class ProcessEventDto extends LogEntryDto {
    public ProcessEventState state;
    public ProcessElementDto element;
    public String eventTime;
    public Double idleSeconds;
    public String completionTime;

    public Map<String, Object> toMap() {
        Map<String, Object> map = baseMap();
        map.put("state", state == null ? null : state.name());
        map.put("element", element == null ? null : element.toMap());
        map.put("eventTime", eventTime);
        map.put("idleSeconds", idleSeconds);
        map.put("completionTime", completionTime);
        return map;
    }
}
