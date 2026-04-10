# Log Format Notes

The backend parser is designed to normalize different raw log formats into one
internal event model.

## Internal Event Model

Each parsed event is mapped to:

- `timestamp`
- `raw_timestamp`
- `event_type`
- `lifecycle`
- `process_id`
- `process_name`
- `activity_id`
- `activity_name`
- `resource_id`
- `resource_name`
- `attributes`

## Raw Formats Supported in MVP

### 1. JSON lines

One JSON object per line.

Example:

```json
{"timestamp":"2026-03-31T10:00:01Z","event":"ACTIVITY_START","case_id":"p1","activity":"A_SUBMITTED","resource":"role1"}
```

### 2. CSV / semicolon / tab separated

Header-based delimited file.

Example:

```text
timestamp,event_type,process_id,activity_name,resource_name
2026-03-31T10:00:01Z,ACTIVITY_START,p1,A_SUBMITTED,role1
```

### 3. Key-value lines

Space-separated `key=value`.

Example:

```text
timestamp=2026-03-31T10:00:01Z event=ACTIVITY_START case_id=p1 activity=A_SUBMITTED resource=role1
```

## Field Alias Strategy

The parser maps common aliases into canonical fields. For example:

- `case_id`, `trace_id`, `instance_id` -> `process_id`
- `activity`, `task`, `element` -> `activity_name`
- `resource`, `worker`, `role` -> `resource_name`
- `event`, `type`, `kind` -> `event_type`

To finalize the parser, we still need:

- one real log sample
- event types
- timestamp format
- process instance identifier
- activity identifier/name
- resource identifier/name
