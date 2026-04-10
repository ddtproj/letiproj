package com.example.simdashboard.service;

import com.example.simdashboard.dto.ChartsDto;
import com.example.simdashboard.dto.ProcessStatsDto;
import com.example.simdashboard.dto.SimulationResultDto;
import com.example.simdashboard.dto.SummaryDto;
import com.example.simdashboard.model.MetricsResult;
import com.example.simdashboard.model.ParseLogResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SimulationResultService {

    private final RunStorageService runStorageService;
    private final RunService runService;
    private final LogParserService logParserService;
    private final MetricsCalculatorService metricsCalculatorService;

    public SimulationResultService(
            RunStorageService runStorageService,
            RunService runService,
            LogParserService logParserService,
            MetricsCalculatorService metricsCalculatorService
    ) {
        this.runStorageService = runStorageService;
        this.runService = runService;
        this.logParserService = logParserService;
        this.metricsCalculatorService = metricsCalculatorService;
    }

    public SimulationResultDto buildResult(String runId) {
        Optional<Path> logPath = runStorageService.getRunLogPath(runId);
        if (logPath.isPresent()) {
            try {
                ParseLogResult parseResult = logParserService.parseLogResult(logPath.get());
                MetricsResult metrics = metricsCalculatorService.calculateMetrics(parseResult.events());
                return fromMetrics(runId, metrics, parseResult);
            } catch (IOException exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read log", exception);
            }
        }

        if (runStorageService.readRunMeta(runId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Result is not available yet");
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found");
    }

    private SimulationResultDto fromMetrics(String runId, MetricsResult metrics, ParseLogResult parseResult) {
        return new SimulationResultDto(
                runId,
                runService.getRunStatus(runId).orElse("completed"),
                metrics.startedAt(),
                metrics.finishedAt(),
                new SummaryDto(
                        parseResult.totalRecords(),
                        parseResult.firstEventTime() != null ? parseResult.firstEventTime().toString() : null,
                        parseResult.lastFinishTime() != null ? parseResult.lastFinishTime().toString() : null,
                        parseResult.unknownLines(),
                        metrics.summary().simulationTimeSec(),
                        metrics.summary().processCount(),
                        metrics.summary().completedProcessCount(),
                        metrics.summary().failedProcessCount(),
                        metrics.summary().activityStartedCount(),
                        metrics.summary().activityCompletedCount(),
                        metrics.summary().blockedWaitCount()
                ),
                new ProcessStatsDto(
                        metrics.processStats().minDurationSec(),
                        metrics.processStats().avgDurationSec(),
                        metrics.processStats().maxDurationSec(),
                        metrics.processStats().durations()
                ),
                metrics.activities(),
                metrics.resources(),
                new ChartsDto(
                        metrics.activityCounts(),
                        metrics.activityAvgDurations(),
                        metrics.resourceBlocks(),
                        metrics.processDurationHistogram()
                )
        );
    }
}
