package com.example.simdashboard.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RunService {

    private final RunStorageService runStorageService;
    private final SimulationExecutionService simulationExecutionService;

    public RunService(RunStorageService runStorageService, SimulationExecutionService simulationExecutionService) {
        this.runStorageService = runStorageService;
        this.simulationExecutionService = simulationExecutionService;
    }

    public String createRun(String specificationPath) throws IOException {
        String runId = "sim_" + OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSSSSS"));

        Map<String, Object> meta = new HashMap<>();
        meta.put("runId", runId);
        meta.put("status", "queued");
        meta.put("specificationPath", specificationPath);
        meta.put("createdAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
        runStorageService.writeRunMeta(runId, meta);
        simulationExecutionService.startSimulation(runId, specificationPath);
        return runId;
    }

    public Optional<String> getRunStatus(String runId) {
        Optional<Map<String, Object>> meta = runStorageService.readRunMeta(runId);
        if (meta.isPresent()) {
            Object status = meta.get().get("status");
            if (status instanceof String statusValue) {
                return Optional.of(statusValue);
            }
        }

        if (runStorageService.getRunLogPath(runId).isPresent()) {
            return Optional.of("completed");
        }

        return Optional.empty();
    }
}
