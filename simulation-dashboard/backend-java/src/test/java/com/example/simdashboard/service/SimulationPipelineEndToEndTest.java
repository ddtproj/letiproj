package com.example.simdashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.simdashboard.dto.ActivityStatsDto;
import com.example.simdashboard.dto.ResourceStatsDto;
import com.example.simdashboard.dto.SimulationResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SimulationPipelineEndToEndTest {

    @TempDir
    Path tempDir;

    @Test
    void transformsTestLogIntoFrontendReadyDtosForSummaryActivitiesResourcesAndCharts() throws Exception {
        SimulationResultService simulationResultService = createSimulationResultService(tempDir);

        writeLog(tempDir, "sim_pipeline_001", String.join(System.lineSeparator(),
                "Process: 1 Completed: EVENT START (Start) id: s1 index: 1  duration: 0.0 - Process 1 at time 2026-04-10 10:00:00, idle for: 0.0s",
                "NO resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1",
                "1 resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1",
                "Process: 1 Enabled: TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1 at time 2026-04-10 10:01:00",
                "Process: 1 Completed: TASK (Review) id: review1 index: 1 duration: 120.0 - Process 1 at time 2026-04-10 10:03:00",
                "Process: 1 completed at time 2026-04-10 10:05:00",
                "Process: 2 Completed: EVENT START (Start) id: s2 index: 1  duration: 0.0 - Process 2 at time 2026-04-10 10:10:00, idle for: 0.0s",
                "1 resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 2",
                "Process: 2 Enabled: TASK (Review) id: review1 index: 1 duration: 0.0 - Process 2 at time 2026-04-10 10:11:00",
                "Process: 2 Completed: TASK (Review) id: review1 index: 1 duration: 180.0 - Process 2 at time 2026-04-10 10:14:00",
                "Process: 2 completed at time 2026-04-10 10:16:00"
        ));

        SimulationResultDto result = simulationResultService.buildResult("sim_pipeline_001");

        assertThat(result.summary().processCount()).isEqualTo(2);
        assertThat(result.summary().completedProcessCount()).isEqualTo(2);

        ActivityStatsDto review = result.activities().stream()
                .filter(item -> item.name().equals("Review"))
                .findFirst()
                .orElseThrow();
        assertThat(review.count()).isEqualTo(2);
        assertThat(review.avgDurationSec()).isEqualTo(150.0);
        assertThat(review.minDurationSec()).isEqualTo(120.0);
        assertThat(review.maxDurationSec()).isEqualTo(180.0);

        ResourceStatsDto manager = result.resources().stream()
                .filter(item -> item.name().equals("Manager"))
                .findFirst()
                .orElseThrow();
        assertThat(manager.blockCount()).isEqualTo(1);
        assertThat(manager.availableCount()).isEqualTo(2);

        assertThat(result.charts().activityCounts())
                .extracting(point -> point.name(), point -> point.value().intValue())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Review", 2));
        assertThat(result.charts().resourceBlocks())
                .extracting(point -> point.name(), point -> point.value().intValue())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("Manager", 1));
        assertThat(result.charts().processDurationHistogram())
                .extracting(point -> point.binLabel(), point -> point.count())
                .contains(
                        org.assertj.core.groups.Tuple.tuple("300-310", 1),
                        org.assertj.core.groups.Tuple.tuple("350-360", 1)
                );
    }

    @Test
    void changingInputLogChangesNumbersInDtosTablesAndCharts() throws Exception {
        SimulationResultService simulationResultService = createSimulationResultService(tempDir);

        writeLog(tempDir, "sim_pipeline_small", String.join(System.lineSeparator(),
                "Process: 1 Completed: EVENT START (Start) id: s1 index: 1  duration: 0.0 - Process 1 at time 2026-04-10 10:00:00, idle for: 0.0s",
                "1 resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1",
                "Process: 1 Enabled: TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1 at time 2026-04-10 10:01:00",
                "Process: 1 Completed: TASK (Review) id: review1 index: 1 duration: 60.0 - Process 1 at time 2026-04-10 10:02:00",
                "Process: 1 completed at time 2026-04-10 10:03:00"
        ));

        writeLog(tempDir, "sim_pipeline_large", String.join(System.lineSeparator(),
                "Process: 1 Completed: EVENT START (Start) id: s1 index: 1  duration: 0.0 - Process 1 at time 2026-04-10 10:00:00, idle for: 0.0s",
                "NO resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1",
                "1 resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1",
                "Process: 1 Enabled: TASK (Review) id: review1 index: 1 duration: 0.0 - Process 1 at time 2026-04-10 10:01:00",
                "Process: 1 Completed: TASK (Review) id: review1 index: 1 duration: 120.0 - Process 1 at time 2026-04-10 10:03:00",
                "Process: 1 completed at time 2026-04-10 10:05:00",
                "Process: 2 Completed: EVENT START (Start) id: s2 index: 1  duration: 0.0 - Process 2 at time 2026-04-10 10:10:00, idle for: 0.0s",
                "1 resources (Resource Manager id: res1 index: 1) available for TASK (Review) id: review1 index: 1 duration: 0.0 - Process 2",
                "Process: 2 Enabled: TASK (Review) id: review1 index: 1 duration: 0.0 - Process 2 at time 2026-04-10 10:11:00",
                "Process: 2 Completed: TASK (Review) id: review1 index: 1 duration: 180.0 - Process 2 at time 2026-04-10 10:14:00",
                "Process: 2 completed at time 2026-04-10 10:16:00",
                "Process: 3 Completed: EVENT START (Start) id: s3 index: 1  duration: 0.0 - Process 3 at time 2026-04-10 10:20:00, idle for: 0.0s",
                "1 resources (Resource Analyst id: res2 index: 1) available for TASK (Approve) id: approve1 index: 1 duration: 0.0 - Process 3",
                "Process: 3 Enabled: TASK (Approve) id: approve1 index: 1 duration: 0.0 - Process 3 at time 2026-04-10 10:21:00",
                "Process: 3 Completed: TASK (Approve) id: approve1 index: 1 duration: 240.0 - Process 3 at time 2026-04-10 10:25:00",
                "Process: 3 completed at time 2026-04-10 10:26:00"
        ));

        SimulationResultDto small = simulationResultService.buildResult("sim_pipeline_small");
        SimulationResultDto large = simulationResultService.buildResult("sim_pipeline_large");

        assertThat(large.summary().processCount()).isGreaterThan(small.summary().processCount());
        assertThat(large.activities().size()).isGreaterThan(small.activities().size());
        assertThat(large.resources().size()).isGreaterThan(small.resources().size());
        assertThat(large.charts().activityCounts().size()).isGreaterThan(small.charts().activityCounts().size());
        assertThat(large.charts().processDurationHistogram())
                .extracting(point -> point.count())
                .isNotEqualTo(small.charts().processDurationHistogram().stream().map(point -> point.count()).toList());
    }

    private static SimulationResultService createSimulationResultService(Path tempDir) {
        ObjectMapper objectMapper = new ObjectMapper();
        RunStorageService storageService = new RunStorageService(tempDir.toString(), objectMapper);
        RunService runService = new RunService(storageService, new TestSimulationExecutionService(storageService));
        LogParserService logParserService = new LogParserService(objectMapper);
        MetricsCalculatorService metricsCalculatorService = new MetricsCalculatorService();
        return new SimulationResultService(storageService, runService, logParserService, metricsCalculatorService);
    }

    private static void writeLog(Path tempDir, String runId, String content) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RunStorageService storageService = new RunStorageService(tempDir.toString(), objectMapper);
        Path runDir = storageService.ensureRunDir(runId);
        Files.writeString(runDir.resolve("simulation.log"), content);
    }
}
