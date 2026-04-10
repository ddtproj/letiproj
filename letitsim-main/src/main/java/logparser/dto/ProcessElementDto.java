package logparser.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessElementDto {
    public String type;
    public String name;
    public String id;
    public Integer index;
    public Double durationSeconds;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("type", type);
        map.put("name", name);
        map.put("id", id);
        map.put("index", index);
        map.put("durationSeconds", durationSeconds);
        return map;
    }
}
