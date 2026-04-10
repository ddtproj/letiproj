package logparser.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceDto {
    public Integer availableCount;
    public String name;
    public String id;
    public Integer index;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("availableCount", availableCount);
        map.put("name", name);
        map.put("id", id);
        map.put("index", index);
        return map;
    }
}
