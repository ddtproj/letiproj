package logparser.dto;

import java.util.Map;

public class ResourceEventDto extends LogEntryDto {
    public ResourceDto resource;
    public ProcessElementDto element;

    public Map<String, Object> toMap() {
        Map<String, Object> map = baseMap();
        map.put("resource", resource == null ? null : resource.toMap());
        map.put("element", element == null ? null : element.toMap());
        return map;
    }
}
