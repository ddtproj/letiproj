package com.example.simdashboard.service;

class TestSimulationExecutionService extends SimulationExecutionService {

    TestSimulationExecutionService(RunStorageService runStorageService) {
        super(runStorageService, ".", "Main", "");
    }

    @Override
    public void startSimulation(String runId, String specificationPath) {
        // no-op for unit tests
    }
}
