package logparser.dto;

import java.util.Map;

public class UnknownLogLineDto extends LogEntryDto {

    public Map<String, Object> toMap() {
        return baseMap();
    }
}
