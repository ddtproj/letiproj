package com.example.simdashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.simdashboard.dto.SimulationResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SimulationResultSummaryTest {

    @TempDir
    Path tempDir;

    @Test
    void buildsSummaryDtoWithExperimentLevelStatistics() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RunStorageService storageService = new RunStorageService(tempDir.toString(), objectMapper);
        RunService runService = new RunService(storageService, new TestSimulationExecutionService(storageService));
        LogParserService logParserService = new LogParserService(objectMapper);
        MetricsCalculatorService metricsCalculatorService = new MetricsCalculatorService();
        SimulationResultService simulationResultService = new SimulationResultService(
                storageService,
                runService,
                logParserService,
                metricsCalculatorService
        );

        String runId = "sim_test_001";
        Path runDir = storageService.ensureRunDir(runId);
        Files.writeString(runDir.resolve("simulation.log"), String.join(System.lineSeparator(),
                "Process: 1 Completed: EVENT START (Start) id: s1 index: 1  duration: 0.0 - Process 1 at time 2026-04-10 10:00:00, idle for: 0.0s",
                "totally unknown line",
                "Process: 1 completed at time 2026-04-10 10:05:00",
                "Process: 2 Completed: EVENT START (Start) id: s2 index: 1  duration: 0.0 - Process 2 at time 2026-04-10 10:06:00, idle for: 0.0s",
                "Process: 2 completed at time 2026-04-10 10:10:00"
        ));

        SimulationResultDto result = simulationResultService.buildResult(runId);

        assertThat(result.summary().totalRecords()).isEqualTo(5);
        assertThat(result.summary().unknownLines()).isEqualTo(1);
        assertThat(result.summary().processCount()).isEqualTo(2);
        assertThat(result.summary().firstEventTime()).isEqualTo("2026-04-10T10:00");
        assertThat(result.summary().lastFinishTime()).isEqualTo("2026-04-10T10:10");
        assertThat(result.summary().simulationTimeSec()).isEqualTo(600.0);
    }
}
