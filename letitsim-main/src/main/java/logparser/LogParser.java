package logparser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<PatternDefinition> patternDefinitions;

    public LogParser() {
        this.patternDefinitions = buildPatterns();
    }

    public List<LogRecord> parse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<LogRecord> result = new ArrayList<LogRecord>(lines.size());

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            result.add(parseLine(trimmed, i + 1));
        }

        return result;
    }

    public LogRecord parseLine(String line, int lineNumber) {
        for (PatternDefinition definition : patternDefinitions) {
            Matcher matcher = definition.pattern.matcher(line);
            if (matcher.matches()) {
                return new LogRecord(lineNumber, definition.type, line, definition.extractor.extract(matcher));
            }
        }

        return new LogRecord(lineNumber, "unknown", line, Collections.<String, Object>emptyMap());
    }

    private List<PatternDefinition> buildPatterns() {
        List<PatternDefinition> patterns = new ArrayList<PatternDefinition>();

        patterns.add(new PatternDefinition(
                "process_event",
                Pattern.compile("^Process:\\s+(\\d+)\\s+(Completed|Enabled):\\s+(.+?)(?:\\s+\\((.*?)\\))?\\s+id:\\s+(\\S+)\\s+index:\\s+(\\d+)\\s+duration:\\s+([\\d.]+)\\s+-\\s+Process\\s+(\\d+)\\s+at time\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})(?:,\\s+idle for:\\s+([\\d.]+)s|\\s+completion time:\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}))$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("processId", parseInt(matcher.group(1)));
                        fields.put("state", matcher.group(2));
                        fields.put("elementType", matcher.group(3));
                        fields.put("elementName", matcher.group(4));
                        fields.put("elementId", matcher.group(5));
                        fields.put("elementIndex", parseInt(matcher.group(6)));
                        fields.put("durationSeconds", parseDouble(matcher.group(7)));
                        fields.put("eventTime", parseDateTime(matcher.group(9)));
                        fields.put("idleSeconds", parseDoubleNullable(matcher.group(10)));
                        fields.put("completionTime", parseDateTimeNullable(matcher.group(11)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "resource_unavailable",
                Pattern.compile("^NO resources\\s+\\((.+?)\\s+id:\\s+(\\S+)\\s+index:\\s+(\\d+)\\)\\s+available for\\s+(.+?)(?:\\s+\\((.*?)\\))?\\s+id:\\s+(\\S+)\\s+index:\\s+(\\d+)\\s+duration:\\s+([\\d.]+)\\s+-\\s+Process\\s+(\\d+)$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("resourceName", matcher.group(1));
                        fields.put("resourceId", matcher.group(2));
                        fields.put("resourceIndex", parseInt(matcher.group(3)));
                        fields.put("elementType", matcher.group(4));
                        fields.put("elementName", matcher.group(5));
                        fields.put("elementId", matcher.group(6));
                        fields.put("elementIndex", parseInt(matcher.group(7)));
                        fields.put("durationSeconds", parseDouble(matcher.group(8)));
                        fields.put("processId", parseInt(matcher.group(9)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "resource_available",
                Pattern.compile("^(\\d+)\\s+resources?\\s+\\((.+?)\\s+id:\\s+(\\S+)\\s+index:\\s+(\\d+)\\)\\s+available for\\s+(.+?)(?:\\s+\\((.*?)\\))?\\s+id:\\s+(\\S+)\\s+index:\\s+(\\d+)\\s+duration:\\s+([\\d.]+)\\s+-\\s+Process\\s+(\\d+)$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("availableCount", parseInt(matcher.group(1)));
                        fields.put("resourceName", matcher.group(2));
                        fields.put("resourceId", matcher.group(3));
                        fields.put("resourceIndex", parseInt(matcher.group(4)));
                        fields.put("elementType", matcher.group(5));
                        fields.put("elementName", matcher.group(6));
                        fields.put("elementId", matcher.group(7));
                        fields.put("elementIndex", parseInt(matcher.group(8)));
                        fields.put("durationSeconds", parseDouble(matcher.group(9)));
                        fields.put("processId", parseInt(matcher.group(10)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "process_finished",
                Pattern.compile("^Process:\\s+(\\d+)\\s+completed at time\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("processId", parseInt(matcher.group(1)));
                        fields.put("eventTime", parseDateTime(matcher.group(2)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "process_started",
                Pattern.compile("^Process:\\s+(\\d+)\\s+started at time\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("processId", parseInt(matcher.group(1)));
                        fields.put("eventTime", parseDateTime(matcher.group(2)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "process_snapshot",
                Pattern.compile("^-\\s+Process\\s+(\\d+)\\s+at time\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}),\\s+idle for:\\s+([\\d.]+)s$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("processId", parseInt(matcher.group(1)));
                        fields.put("eventTime", parseDateTime(matcher.group(2)));
                        fields.put("idleSeconds", parseDouble(matcher.group(3)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "process_status_message",
                Pattern.compile("^Process:\\s+(\\d+)\\s+(.+?)at time\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("processId", parseInt(matcher.group(1)));
                        fields.put("message", matcher.group(2).trim());
                        fields.put("eventTime", parseDateTime(matcher.group(3)));
                        return fields;
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "simulation_finished",
                Pattern.compile("^Simulation finished$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        return Collections.emptyMap();
                    }
                }
        ));

        patterns.add(new PatternDefinition(
                "process_exit_code",
                Pattern.compile("^Process finished with exit code\\s+(-?\\d+)$"),
                new MatchExtractor() {
                    public Map<String, Object> extract(Matcher matcher) {
                        Map<String, Object> fields = new LinkedHashMap<String, Object>();
                        fields.put("exitCode", parseInt(matcher.group(1)));
                        return fields;
                    }
                }
        ));

        return patterns;
    }

    private static Integer parseInt(String value) {
        return Integer.valueOf(value);
    }

    private static Double parseDouble(String value) {
        return Double.valueOf(value);
    }

    private static Double parseDoubleNullable(String value) {
        return value == null || value.isEmpty() ? null : Double.valueOf(value);
    }

    private static LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    private static LocalDateTime parseDateTimeNullable(String value) {
        return value == null || value.isEmpty() ? null : parseDateTime(value);
    }

    private interface MatchExtractor {
        Map<String, Object> extract(Matcher matcher);
    }

    private static final class PatternDefinition {
        private final String type;
        private final Pattern pattern;
        private final MatchExtractor extractor;

        private PatternDefinition(String type, Pattern pattern, MatchExtractor extractor) {
            this.type = type;
            this.pattern = pattern;
            this.extractor = extractor;
        }
    }
}
