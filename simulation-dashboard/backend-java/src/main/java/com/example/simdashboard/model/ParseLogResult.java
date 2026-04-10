package com.example.simdashboard.model;

import java.time.LocalDateTime;
import java.util.List;

public record ParseLogResult(
        List<Event> events,
        int totalRecords,
        int unknownLines,
        LocalDateTime firstEventTime,
        LocalDateTime lastFinishTime
) {
}
