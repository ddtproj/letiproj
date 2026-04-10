package logparser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonWriter {
    public String write(Object value) {
        StringBuilder builder = new StringBuilder();
        appendValue(builder, value, 0);
        return builder.toString();
    }

    private void appendValue(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String) {
            appendString(builder, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map) {
            appendMap(builder, (Map<?, ?>) value, depth);
        } else if (value instanceof List) {
            appendList(builder, (List<?>) value, depth);
        } else {
            appendString(builder, String.valueOf(value));
        }
    }

    private void appendMap(StringBuilder builder, Map<?, ?> map, int depth) {
        builder.append("{");
        if (!map.isEmpty()) {
            builder.append("\n");
            Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                indent(builder, depth + 1);
                appendString(builder, String.valueOf(entry.getKey()));
                builder.append(": ");
                appendValue(builder, entry.getValue(), depth + 1);
                if (iterator.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            indent(builder, depth);
        }
        builder.append("}");
    }

    private void appendList(StringBuilder builder, List<?> list, int depth) {
        builder.append("[");
        if (!list.isEmpty()) {
            builder.append("\n");
            for (int i = 0; i < list.size(); i++) {
                indent(builder, depth + 1);
                appendValue(builder, list.get(i), depth + 1);
                if (i < list.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            indent(builder, depth);
        }
        builder.append("]");
    }

    private void appendString(StringBuilder builder, String value) {
        builder.append("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
            }
        }
        builder.append("\"");
    }

    private void indent(StringBuilder builder, int depth) {
        for (int i = 0; i < depth; i++) {
            builder.append("  ");
        }
    }
}
