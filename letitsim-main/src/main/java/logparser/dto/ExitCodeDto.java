package logparser.dto;

import java.util.Map;

public class ExitCodeDto extends LogEntryDto {
    public Integer exitCode;

    public Map<String, Object> toMap() {
        Map<String, Object> map = baseMap();
        map.put("exitCode", exitCode);
        return map;
    }
}
