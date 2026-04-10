package com.example.simdashboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SimulationResultDtoContractTest {

    @Test
    void exposesDedicatedDtoForExperimentLevelData() {
        RecordComponent[] components = SummaryDto.class.getRecordComponents();

        assertThat(componentNames(components)).containsExactly(
                "totalRecords",
                "firstEventTime",
                "lastFinishTime",
                "unknownLines",
                "simulationTimeSec",
                "processCount",
                "completedProcessCount",
                "failedProcessCount",
                "activityStartedCount",
                "activityCompletedCount",
                "blockedWaitCount"
        );

        assertThat(componentNames(ProcessStatsDto.class.getRecordComponents())).containsExactly(
                "minDurationSec",
                "avgDurationSec",
                "maxDurationSec",
                "durations"
        );
    }

    @Test
    void exposesDedicatedDtoForActivityStatistics() {
        assertThat(componentNames(ActivityStatsDto.class.getRecordComponents())).containsExactly(
                "name",
                "count",
                "avgDurationSec",
                "minDurationSec",
                "maxDurationSec"
        );
    }

    @Test
    void exposesDedicatedDtosForResourceStatisticsAndCharts() {
        assertThat(componentNames(ResourceStatsDto.class.getRecordComponents())).containsExactly(
                "name",
                "blockCount",
                "availableCount",
                "observedWorkTimeSec",
                "utilizationPercent"
        );

        assertThat(componentNames(ChartsDto.class.getRecordComponents())).containsExactly(
                "activityCounts",
                "activityAvgDurations",
                "resourceBlocks",
                "processDurationHistogram"
        );

        assertThat(componentNames(ChartPointDto.class.getRecordComponents())).containsExactly(
                "name",
                "value",
                "binLabel",
                "from",
                "to",
                "count"
        );
    }

    @Test
    void simulationResultDtoComposesDedicatedDtosInsteadOfUniversalPayload() {
        RecordComponent[] components = SimulationResultDto.class.getRecordComponents();

        assertThat(componentNames(components)).containsExactly(
                "runId",
                "status",
                "startedAt",
                "finishedAt",
                "summary",
                "processStats",
                "activities",
                "resources",
                "charts"
        );

        assertThat(components[4].getType()).isEqualTo(SummaryDto.class);
        assertThat(components[5].getType()).isEqualTo(ProcessStatsDto.class);
        assertThat(components[8].getType()).isEqualTo(ChartsDto.class);
    }

    private String[] componentNames(RecordComponent[] components) {
        return Arrays.stream(components)
                .map(RecordComponent::getName)
                .toArray(String[]::new);
    }
}
