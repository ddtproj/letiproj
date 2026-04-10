package com.example.simdashboard.controller;

import com.example.simdashboard.dto.RunInfoDto;
import com.example.simdashboard.dto.RunRequestDto;
import com.example.simdashboard.dto.RunStatusDto;
import com.example.simdashboard.dto.SimulationResultDto;
import com.example.simdashboard.service.RunService;
import com.example.simdashboard.service.RunStorageService;
import com.example.simdashboard.service.SimulationResultService;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final RunStorageService runStorageService;
    private final RunService runService;
    private final SimulationResultService simulationResultService;

    public SimulationController(
            RunStorageService runStorageService,
            RunService runService,
            SimulationResultService simulationResultService
    ) {
        this.runStorageService = runStorageService;
        this.runService = runService;
        this.simulationResultService = simulationResultService;
    }

    @GetMapping
    public List<RunInfoDto> getRuns() {
        return runStorageService.listRuns();
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    public RunStatusDto createRun(@RequestBody(required = false) RunRequestDto payload) {
        try {
            String runId = runService.createRun(payload != null ? payload.specificationPath() : null);
            return new RunStatusDto(runId, "queued");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create run", exception);
        }
    }

    @GetMapping("/{runId}/status")
    public RunStatusDto getRunStatus(@PathVariable String runId) {
        return runService.getRunStatus(runId)
                .map(status -> new RunStatusDto(runId, status))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    }

    @GetMapping("/{runId}/result")
    public SimulationResultDto getRunResult(@PathVariable String runId) {
        return simulationResultService.buildResult(runId);
    }
}
