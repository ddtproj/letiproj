package logparser;

import logparser.dto.ExitCodeDto;
import logparser.dto.LogEntryType;
import logparser.dto.ParsedLogDto;
import logparser.dto.ProcessElementDto;
import logparser.dto.ProcessEventDto;
import logparser.dto.ProcessEventState;
import logparser.dto.ProcessStateDto;
import logparser.dto.ResourceDto;
import logparser.dto.ResourceEventDto;
import logparser.dto.StatusMessageDto;
import logparser.dto.UnknownLogLineDto;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogDtoMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParsedLogDto map(Path sourceFile, List<LogRecord> records) {
        ParsedLogDto dto = new ParsedLogDto();
        dto.sourceFile = sourceFile.toAbsolutePath().normalize().toString();

        Set<Integer> processIds = new LinkedHashSet<Integer>();
        LocalDateTime firstEventTime = null;
        LocalDateTime lastFinishTime = null;

        for (LogRecord record : records) {
            increment(dto.summary.countsByType, record.getType());

            Integer processId = record.getInt("processId");
            if (processId != null) {
                processIds.add(processId);
            }

            LocalDateTime eventTime = record.getDateTime("eventTime");
            if (eventTime != null && (firstEventTime == null || eventTime.isBefore(firstEventTime))) {
                firstEventTime = eventTime;
            }

            if ("process_finished".equals(record.getType()) && eventTime != null &&
                    (lastFinishTime == null || eventTime.isAfter(lastFinishTime))) {
                lastFinishTime = eventTime;
            }

            if ("process_event".equals(record.getType())) {
                ProcessEventDto processEvent = toProcessEvent(record);
                dto.processEvents.add(processEvent);
                dto.entries.add(processEvent.toMap());
            } else if ("resource_available".equals(record.getType()) || "resource_unavailable".equals(record.getType())) {
                ResourceEventDto resourceEvent = toResourceEvent(record);
                dto.resourceEvents.add(resourceEvent);
                dto.entries.add(resourceEvent.toMap());
            } else if ("process_finished".equals(record.getType()) ||
                    "process_started".equals(record.getType()) ||
                    "process_snapshot".equals(record.getType())) {
                ProcessStateDto processState = toProcessState(record);
                dto.processStates.add(processState);
                dto.entries.add(processState.toMap());
            } else if ("process_status_message".equals(record.getType())) {
                StatusMessageDto statusMessage = toStatusMessage(record);
                dto.statusMessages.add(statusMessage);
                dto.entries.add(statusMessage.toMap());
            } else if ("process_exit_code".equals(record.getType())) {
                ExitCodeDto exitCode = toExitCode(record);
                dto.exitCodes.add(exitCode);
                dto.entries.add(exitCode.toMap());
            } else if ("simulation_finished".equals(record.getType())) {
                dto.simulationMarkers.add(record.getRawLine());
                dto.entries.add(simulationEntry(record));
            } else if ("unknown".equals(record.getType())) {
                UnknownLogLineDto unknown = toUnknown(record);
                dto.unknownLines.add(unknown);
                dto.entries.add(unknown.toMap());
            }
        }

        dto.summary.totalRecords = records.size();
        dto.summary.distinctProcessIds = processIds.size();
        dto.summary.firstEventTime = format(firstEventTime);
        dto.summary.lastFinishTime = format(lastFinishTime);
        dto.summary.unknownLines = dto.unknownLines.size();

        return dto;
    }

    private ProcessEventDto toProcessEvent(LogRecord record) {
        ProcessEventDto dto = new ProcessEventDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = LogEntryType.PROCESS_EVENT;
        dto.processId = record.getInt("processId");
        dto.state = parseProcessEventState(stringField(record, "state"));
        dto.element = toElement(record);
        dto.eventTime = format(record.getDateTime("eventTime"));
        dto.idleSeconds = record.getDouble("idleSeconds");
        dto.completionTime = format(record.getDateTime("completionTime"));
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private ResourceEventDto toResourceEvent(LogRecord record) {
        ResourceEventDto dto = new ResourceEventDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = "resource_available".equals(record.getType())
                ? LogEntryType.RESOURCE_AVAILABLE
                : LogEntryType.RESOURCE_UNAVAILABLE;
        dto.processId = record.getInt("processId");
        dto.resource = toResource(record);
        dto.element = toElement(record);
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private ProcessStateDto toProcessState(LogRecord record) {
        ProcessStateDto dto = new ProcessStateDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = parseProcessStateType(record.getType());
        dto.processId = record.getInt("processId");
        dto.eventTime = format(record.getDateTime("eventTime"));
        dto.idleSeconds = record.getDouble("idleSeconds");
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private StatusMessageDto toStatusMessage(LogRecord record) {
        StatusMessageDto dto = new StatusMessageDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = LogEntryType.PROCESS_STATUS_MESSAGE;
        dto.processId = record.getInt("processId");
        dto.message = stringField(record, "message");
        dto.eventTime = format(record.getDateTime("eventTime"));
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private ExitCodeDto toExitCode(LogRecord record) {
        ExitCodeDto dto = new ExitCodeDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = LogEntryType.PROCESS_EXIT_CODE;
        dto.exitCode = record.getInt("exitCode");
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private UnknownLogLineDto toUnknown(LogRecord record) {
        UnknownLogLineDto dto = new UnknownLogLineDto();
        dto.lineNumber = record.getLineNumber();
        dto.entryType = LogEntryType.UNKNOWN;
        dto.rawLine = record.getRawLine();
        return dto;
    }

    private Map<String, Object> simulationEntry(LogRecord record) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("lineNumber", record.getLineNumber());
        map.put("entryType", LogEntryType.SIMULATION_FINISHED.name());
        map.put("processId", null);
        map.put("rawLine", record.getRawLine());
        return map;
    }

    private String stringField(LogRecord record, String fieldName) {
        Object value = record.getFields().get(fieldName);
        return value == null ? null : String.valueOf(value);
    }

    private ProcessElementDto toElement(LogRecord record) {
        ProcessElementDto dto = new ProcessElementDto();
        dto.type = stringField(record, "elementType");
        dto.name = stringField(record, "elementName");
        dto.id = stringField(record, "elementId");
        dto.index = record.getInt("elementIndex");
        dto.durationSeconds = record.getDouble("durationSeconds");
        return dto;
    }

    private ResourceDto toResource(LogRecord record) {
        ResourceDto dto = new ResourceDto();
        dto.availableCount = record.getInt("availableCount");
        dto.name = stringField(record, "resourceName");
        dto.id = stringField(record, "resourceId");
        dto.index = record.getInt("resourceIndex");
        return dto;
    }

    private ProcessEventState parseProcessEventState(String value) {
        if (value == null) {
            return null;
        }
        return ProcessEventState.valueOf(value.toUpperCase());
    }

    private LogEntryType parseProcessStateType(String value) {
        if ("process_finished".equals(value)) {
            return LogEntryType.PROCESS_FINISHED;
        }
        if ("process_started".equals(value)) {
            return LogEntryType.PROCESS_STARTED;
        }
        return LogEntryType.PROCESS_SNAPSHOT;
    }

    private String format(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private void increment(Map<String, Integer> counts, String type) {
        Integer current = counts.get(type);
        counts.put(type, current == null ? 1 : current + 1);
    }
}
