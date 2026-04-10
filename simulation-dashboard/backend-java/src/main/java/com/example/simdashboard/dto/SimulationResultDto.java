package com.example.simdashboard.dto;

import java.util.List;

public record SimulationResultDto(
        String runId,
        String status,
        String startedAt,
        String finishedAt,
        SummaryDto summary,
        ProcessStatsDto processStats,
        List<ActivityStatsDto> activities,
        List<ResourceStatsDto> resources,
        ChartsDto charts
) {
}
