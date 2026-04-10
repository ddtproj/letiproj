package logparser.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedLogDto {
    public String sourceFile;
    public LogSummaryDto summary = new LogSummaryDto();
    public List<ProcessEventDto> processEvents = new ArrayList<ProcessEventDto>();
    public List<ResourceEventDto> resourceEvents = new ArrayList<ResourceEventDto>();
    public List<ProcessStateDto> processStates = new ArrayList<ProcessStateDto>();
    public List<StatusMessageDto> statusMessages = new ArrayList<StatusMessageDto>();
    public List<ExitCodeDto> exitCodes = new ArrayList<ExitCodeDto>();
    public List<String> simulationMarkers = new ArrayList<String>();
    public List<UnknownLogLineDto> unknownLines = new ArrayList<UnknownLogLineDto>();
    public List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("sourceFile", sourceFile);
        map.put("summary", summary.toMap());
        map.put("entries", entries);
        map.put("processEvents", maps(processEvents));
        map.put("resourceEvents", maps(resourceEvents));
        map.put("processStates", maps(processStates));
        map.put("statusMessages", maps(statusMessages));
        map.put("exitCodes", maps(exitCodes));
        map.put("simulationMarkers", simulationMarkers);
        map.put("unknownLines", maps(unknownLines));
        return map;
    }

    private static List<Map<String, Object>> maps(List<?> items) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(items.size());
        for (Object item : items) {
            if (item instanceof ProcessEventDto) {
                result.add(((ProcessEventDto) item).toMap());
            } else if (item instanceof ResourceEventDto) {
                result.add(((ResourceEventDto) item).toMap());
            } else if (item instanceof ProcessStateDto) {
                result.add(((ProcessStateDto) item).toMap());
            } else if (item instanceof StatusMessageDto) {
                result.add(((StatusMessageDto) item).toMap());
            } else if (item instanceof ExitCodeDto) {
                result.add(((ExitCodeDto) item).toMap());
            } else if (item instanceof UnknownLogLineDto) {
                result.add(((UnknownLogLineDto) item).toMap());
            }
        }
        return result;
    }
}
