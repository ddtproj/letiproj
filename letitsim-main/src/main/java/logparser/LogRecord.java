package logparser;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogRecord {
    private final int lineNumber;
    private final String type;
    private final String rawLine;
    private final Map<String, Object> fields;

    public LogRecord(int lineNumber, String type, String rawLine, Map<String, Object> fields) {
        this.lineNumber = lineNumber;
        this.type = type;
        this.rawLine = rawLine;
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(fields));
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getType() {
        return type;
    }

    public String getRawLine() {
        return rawLine;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Integer getInt(String key) {
        Object value = fields.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }

    public Double getDouble(String key) {
        Object value = fields.get(key);
        return value instanceof Double ? (Double) value : null;
    }

    public LocalDateTime getDateTime(String key) {
        Object value = fields.get(key);
        return value instanceof LocalDateTime ? (LocalDateTime) value : null;
    }
}
