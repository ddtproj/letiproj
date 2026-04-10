package com.example.simdashboard.service;

import com.example.simdashboard.model.Event;
import com.example.simdashboard.model.ParseLogResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class LogParserService {

    private static final Map<String, List<String>> FIELD_ALIASES = Map.of(
            "timestamp", List.of("timestamp", "time", "datetime", "date", "event_time"),
            "event_type", List.of("event_type", "event", "type", "kind"),
            "lifecycle", List.of("lifecycle", "transition", "state"),
            "process_id", List.of("process_id", "processinstance", "process_instance", "case_id", "trace_id", "instance_id"),
            "process_name", List.of("process_name", "process"),
            "activity_id", List.of("activity_id", "task_id", "element_id"),
            "activity_name", List.of("activity_name", "activity", "task", "element", "name"),
            "resource_id", List.of("resource_id", "worker_id"),
            "resource_name", List.of("resource_name", "resource", "worker", "role")
    );

    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)=([^\\s]+)");
    private static final Pattern NO_RESOURCE_PATTERN = Pattern.compile(
            "NO resources \\(Resource (?<resource>.+?) id: (?<resourceId>.+?) index: \\d+\\) available for "
                    + "(?<objectType>[A-Z ]+)\\s+\\((?<activity>.+?)\\) id: (?<activityId>.+?) index: \\d+\\s+duration: "
                    + "(?<duration>[0-9.]+) - Process (?<processId>\\d+)"
    );
    private static final Pattern RESOURCE_AVAILABLE_PATTERN = Pattern.compile(
            "(?<count>\\d+) resources \\(Resource (?<resource>.+?) id: (?<resourceId>.+?) index: \\d+\\) available for "
                    + "(?<objectType>[A-Z ]+)\\s+\\((?<activity>.+?)\\) id: (?<activityId>.+?) index: \\d+\\s+duration: "
                    + "(?<duration>[0-9.]+) - Process (?<processId>\\d+)"
    );
    private static final Pattern PROCESS_TRANSITION_PATTERN = Pattern.compile(
            "Process:\\s+(?<processId>\\d+)\\s+(?<state>Enabled|Completed):\\s+"
                    + "(?<objectType>[A-Z ]+)\\s+\\((?<activity>.+?)\\)\\s+id:\\s+(?<activityId>.+?)\\s+index:\\s+\\d+\\s+duration:\\s+"
                    + "(?<duration>[0-9.]+)\\s+-\\s+Process\\s+(?<sameProcessId>\\d+)\\s+at time\\s+(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})(?<tail>.*)$"
    );
    private static final Pattern PROCESS_CREATED_PATTERN = Pattern.compile(
            "- Process (?<processId>\\d+) at time (?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}), idle for: (?<idle>[0-9.]+)s"
    );
    private static final Pattern PROCESS_COMPLETED_PATTERN = Pattern.compile(
            "Process:\\s+(?<processId>\\d+)\\s+completed at time\\s+(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
    );

    private final ObjectMapper objectMapper;

    public LogParserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Event> parseLog(Path logPath) throws IOException {
        return parseLogResult(logPath).events();
    }

    public ParseLogResult parseLogResult(Path logPath) throws IOException {
        List<String> lines = Files.readAllLines(logPath);
        List<String> nonEmpty = lines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        if (nonEmpty.isEmpty()) {
            return new ParseLogResult(List.of(), 0, 0, null, null);
        }

        ParseLogResult result;
        String sample = nonEmpty.get(0);
        if (looksLikeQbpLog(sample)) {
            result = parseQbpLines(nonEmpty);
        } else if (sample.startsWith("{") && sample.endsWith("}")) {
            result = parseJsonLines(nonEmpty);
        } else if (looksLikeDelimited(sample)) {
            result = parseDelimited(nonEmpty);
        } else {
            result = parseKeyValueLines(nonEmpty);
        }

        return enrichResult(result.events(), nonEmpty.size(), result.unknownLines());
    }

    private ParseLogResult parseJsonLines(List<String> lines) {
        List<Event> events = new ArrayList<>();
        int unknownLines = 0;
        for (String line : lines) {
            try {
                Map<String, Object> payload = objectMapper.readValue(line, new TypeReference<>() {
                });
                events.add(eventFromMapping(payload));
            } catch (IOException ignored) {
                unknownLines++;
            }
        }
        return new ParseLogResult(events, lines.size(), unknownLines, null, null);
    }

    private ParseLogResult parseDelimited(List<String> lines) {
        List<Event> events = new ArrayList<>();
        String delimiter = detectDelimiter(lines.get(0));
        String[] headers = lines.get(0).split(Pattern.quote(delimiter), -1);
        for (int index = 1; index < lines.size(); index++) {
            String[] values = lines.get(index).split(Pattern.quote(delimiter), -1);
            Map<String, Object> mapping = new HashMap<>();
            for (int headerIndex = 0; headerIndex < headers.length; headerIndex++) {
                mapping.put(headers[headerIndex], headerIndex < values.length ? values[headerIndex] : "");
            }
            events.add(eventFromMapping(mapping));
        }
        return new ParseLogResult(events, lines.size(), 0, null, null);
    }

    private ParseLogResult parseKeyValueLines(List<String> lines) {
        List<Event> events = new ArrayList<>();
        int unknownLines = 0;
        for (String line : lines) {
            Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
            Map<String, Object> mapping = new HashMap<>();
            while (matcher.find()) {
                mapping.put(matcher.group(1), matcher.group(2));
            }
            if (!mapping.isEmpty()) {
                events.add(eventFromMapping(mapping));
            } else {
                unknownLines++;
            }
        }
        return new ParseLogResult(events, lines.size(), unknownLines, null, null);
    }

    private ParseLogResult parseQbpLines(List<String> lines) {
        List<Event> events = new ArrayList<>();
        int unknownLines = 0;
        for (String line : lines) {
            Optional<Event> event = parseQbpLine(line);
            if (event.isPresent()) {
                events.add(event.get());
            } else {
                unknownLines++;
            }
        }
        return new ParseLogResult(events, lines.size(), unknownLines, null, null);
    }

    private Optional<Event> parseQbpLine(String line) {
        String normalized = line.trim();

        Matcher noResource = NO_RESOURCE_PATTERN.matcher(normalized);
        if (noResource.matches()) {
            Event event = baseEvent(null, null, "RESOURCE_BLOCK", "BLOCKED");
            event.setProcessId(noResource.group("processId"));
            event.setActivityId(noResource.group("activityId"));
            event.setActivityName(noResource.group("activity"));
            event.setResourceId(noResource.group("resourceId"));
            event.setResourceName(noResource.group("resource"));
            event.getAttributes().put("rawLine", normalized);
            event.getAttributes().put("duration", noResource.group("duration"));
            event.getAttributes().put("objectType", noResource.group("objectType").trim());
            return Optional.of(event);
        }

        Matcher resourceAvailable = RESOURCE_AVAILABLE_PATTERN.matcher(normalized);
        if (resourceAvailable.matches()) {
            Event event = baseEvent(null, null, "RESOURCE_AVAILABLE", "AVAILABLE");
            event.setProcessId(resourceAvailable.group("processId"));
            event.setActivityId(resourceAvailable.group("activityId"));
            event.setActivityName(resourceAvailable.group("activity"));
            event.setResourceId(resourceAvailable.group("resourceId"));
            event.setResourceName(resourceAvailable.group("resource"));
            event.getAttributes().put("rawLine", normalized);
            event.getAttributes().put("availableCount", resourceAvailable.group("count"));
            event.getAttributes().put("duration", resourceAvailable.group("duration"));
            event.getAttributes().put("objectType", resourceAvailable.group("objectType").trim());
            return Optional.of(event);
        }

        Matcher transition = PROCESS_TRANSITION_PATTERN.matcher(normalized);
        if (transition.matches()) {
            String objectType = transition.group("objectType").trim();
            String state = transition.group("state").toUpperCase(Locale.ROOT);
            TransitionClassification classification = classifyQbpTransition(objectType, state);
            String rawTimestamp = transition.group("timestamp");
            Event event = baseEvent(parseTimestamp(rawTimestamp), rawTimestamp, classification.eventType(), classification.lifecycle());
            event.setProcessId(transition.group("processId"));
            event.setActivityId(transition.group("activityId"));
            event.setActivityName(transition.group("activity"));
            event.getAttributes().put("rawLine", normalized);
            event.getAttributes().put("duration", transition.group("duration"));
            event.getAttributes().put("objectType", objectType);
            addTailAttributes(transition.group("tail"), event);
            return Optional.of(event);
        }

        Matcher processCreated = PROCESS_CREATED_PATTERN.matcher(normalized);
        if (processCreated.matches()) {
            String rawTimestamp = processCreated.group("timestamp");
            Event event = baseEvent(parseTimestamp(rawTimestamp), rawTimestamp, "PROCESS_CREATED", "CREATED");
            event.setProcessId(processCreated.group("processId"));
            event.getAttributes().put("rawLine", normalized);
            event.getAttributes().put("idleForSec", processCreated.group("idle"));
            return Optional.of(event);
        }

        Matcher processCompleted = PROCESS_COMPLETED_PATTERN.matcher(normalized);
        if (processCompleted.matches()) {
            String rawTimestamp = processCompleted.group("timestamp");
            Event event = baseEvent(parseTimestamp(rawTimestamp), rawTimestamp, "PROCESS_END", "PROCESS_END");
            event.setProcessId(processCompleted.group("processId"));
            event.getAttributes().put("rawLine", normalized);
            return Optional.of(event);
        }

        return Optional.empty();
    }

    private Event eventFromMapping(Map<String, Object> rawMapping) {
        Map<String, String> mapping = new HashMap<>();
        rawMapping.forEach((key, value) -> mapping.put(key.trim().toLowerCase(Locale.ROOT), stringify(value)));

        String rawTimestamp = pick(mapping, "timestamp");
        Event event = baseEvent(parseTimestamp(rawTimestamp), rawTimestamp, pick(mapping, "event_type"), pick(mapping, "lifecycle"));
        event.setProcessId(pick(mapping, "process_id"));
        event.setProcessName(pick(mapping, "process_name"));
        event.setActivityId(pick(mapping, "activity_id"));
        event.setActivityName(pick(mapping, "activity_name"));
        event.setResourceId(pick(mapping, "resource_id"));
        event.setResourceName(pick(mapping, "resource_name"));

        Map<String, String> attributes = new HashMap<>();
        mapping.forEach((key, value) -> {
            if (!isKnownAlias(key)) {
                attributes.put(key, value);
            }
        });
        event.setAttributes(attributes);
        return event;
    }

    private boolean isKnownAlias(String key) {
        return FIELD_ALIASES.values().stream().anyMatch(list -> list.contains(key));
    }

    private String pick(Map<String, String> mapping, String canonicalName) {
        List<String> aliases = FIELD_ALIASES.getOrDefault(canonicalName, List.of());
        for (String alias : aliases) {
            String value = mapping.get(alias);
            if (value != null && !value.isBlank() && !List.of("null", "none", "-").contains(value.toLowerCase(Locale.ROOT))) {
                return value;
            }
        }
        return null;
    }

    private boolean looksLikeDelimited(String line) {
        return line.contains(",") || line.contains(";") || line.contains("\t");
    }

    private boolean looksLikeQbpLog(String line) {
        return line.startsWith("Process:")
                || line.startsWith("NO resources")
                || line.startsWith("- Process ")
                || line.contains(" available for ");
    }

    private String detectDelimiter(String line) {
        int commas = count(line, ',');
        int semicolons = count(line, ';');
        int tabs = count(line, '\t');
        if (semicolons >= commas && semicolons >= tabs) {
            return ";";
        }
        if (tabs >= commas) {
            return "\t";
        }
        return ",";
    }

    private int count(String line, char character) {
        int total = 0;
        for (char current : line.toCharArray()) {
            if (current == character) {
                total++;
            }
        }
        return total;
    }

    private Event baseEvent(LocalDateTime timestamp, String rawTimestamp, String eventType, String lifecycle) {
        Event event = new Event();
        event.setTimestamp(timestamp);
        event.setRawTimestamp(rawTimestamp);
        event.setEventType(eventType == null || eventType.isBlank() ? "unknown" : eventType);
        event.setLifecycle(lifecycle);
        return event;
    }

    private ParseLogResult enrichResult(List<Event> events, int totalRecords, int unknownLines) {
        LocalDateTime firstEventTime = events.stream()
                .map(Event::getTimestamp)
                .filter(value -> value != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastFinishTime = events.stream()
                .filter(event -> "PROCESS_END".equals(event.getEventType()) || "PROCESS_END".equals(event.getLifecycle()))
                .map(Event::getTimestamp)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new ParseLogResult(events, totalRecords, unknownLines, firstEventTime, lastFinishTime);
    }

    private void addTailAttributes(String tail, Event event) {
        Matcher completionTime = Pattern.compile("completion time:\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})").matcher(tail);
        Matcher idle = Pattern.compile("idle for:\\s+([0-9.]+)s").matcher(tail);
        if (completionTime.find()) {
            event.getAttributes().put("completionTime", completionTime.group(1));
        }
        if (idle.find()) {
            event.getAttributes().put("idleForSec", idle.group(1));
        }
    }

    private TransitionClassification classifyQbpTransition(String objectType, String state) {
        String normalizedType = objectType.trim().replaceAll("\\s+", " ");
        if ("EVENT START".equals(normalizedType) && "COMPLETED".equals(state)) {
            return new TransitionClassification("PROCESS_START", "PROCESS_START");
        }
        if ("EVENT END".equals(normalizedType) && "COMPLETED".equals(state)) {
            return new TransitionClassification("PROCESS_END_EVENT", "END");
        }
        if (normalizedType.startsWith("TASK")) {
            if ("ENABLED".equals(state)) {
                return new TransitionClassification("ACTIVITY_START", "START");
            }
            if ("COMPLETED".equals(state)) {
                return new TransitionClassification("ACTIVITY_END", "COMPLETE");
            }
        }
        if (normalizedType.startsWith("EVENT")) {
            if ("ENABLED".equals(state)) {
                return new TransitionClassification("ACTIVITY_START", "START");
            }
            if ("COMPLETED".equals(state)) {
                return new TransitionClassification("ACTIVITY_END", "COMPLETE");
            }
        }
        if (normalizedType.startsWith("GATEWAY")) {
            return new TransitionClassification("GATEWAY_" + state, state);
        }
        return new TransitionClassification(normalizedType.replace(' ', '_') + "_" + state, state);
    }

    private LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value.replace("Z", "+00:00"), DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String stringify(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record TransitionClassification(String eventType, String lifecycle) {
    }
}
