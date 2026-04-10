package com.example.simdashboard.dto;

import java.util.List;

public record ChartsDto(
        List<ChartPointDto> activityCounts,
        List<ChartPointDto> activityAvgDurations,
        List<ChartPointDto> resourceBlocks,
        List<ChartPointDto> processDurationHistogram
) {
}
