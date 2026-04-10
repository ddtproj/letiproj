package com.example.simdashboard.model;

import com.example.simdashboard.dto.ActivityStatsDto;
import com.example.simdashboard.dto.ChartPointDto;
import com.example.simdashboard.dto.ResourceStatsDto;
import java.util.List;

public record MetricsResult(
        String startedAt,
        String finishedAt,
        Summary summary,
        ProcessStats processStats,
        List<ActivityStatsDto> activities,
        List<ResourceStatsDto> resources,
        List<ChartPointDto> activityCounts,
        List<ChartPointDto> activityAvgDurations,
        List<ChartPointDto> resourceBlocks,
        List<ChartPointDto> processDurationHistogram
) {

    public record Summary(
            Double simulationTimeSec,
            Integer processCount,
            Integer completedProcessCount,
            Integer failedProcessCount,
            Integer activityStartedCount,
            Integer activityCompletedCount,
            Integer blockedWaitCount
    ) {
    }

    public record ProcessStats(
            Double minDurationSec,
            Double avgDurationSec,
            Double maxDurationSec,
            List<Double> durations
    ) {
    }
}
