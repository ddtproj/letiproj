package com.example.simdashboard.dto;

public record SummaryDto(
        Integer totalRecords,
        String firstEventTime,
        String lastFinishTime,
        Integer unknownLines,
        Double simulationTimeSec,
        Integer processCount,
        Integer completedProcessCount,
        Integer failedProcessCount,
        Integer activityStartedCount,
        Integer activityCompletedCount,
        Integer blockedWaitCount
) {
}
