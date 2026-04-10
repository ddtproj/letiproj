package com.example.simdashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.simdashboard.dto.ActivityStatsDto;
import com.example.simdashboard.dto.ChartPointDto;
import com.example.simdashboard.dto.ChartsDto;
import com.example.simdashboard.dto.ProcessStatsDto;
import com.example.simdashboard.dto.ResourceStatsDto;
import com.example.simdashboard.dto.SimulationResultDto;
import com.example.simdashboard.dto.SummaryDto;
import com.example.simdashboard.service.SimulationResultService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SimulationControllerApiContractTest {

    @Test
    void returnsAggregatedDtoPayloadForFrontendScreensAndCharts() throws Exception {
        String runId = "sim_api_001";
        SimulationResultDto dto = new SimulationResultDto(
                runId,
                "completed",
                "2026-04-10T10:00:00",
                "2026-04-10T10:20:00",
                new SummaryDto(
                        42,
                        "2026-04-10T10:00:00",
                        "2026-04-10T10:20:00",
                        1,
                        1200.0,
                        3,
                        3,
                        0,
                        6,
                        6,
                        2
                ),
                new ProcessStatsDto(
                        120.0,
                        240.0,
                        360.0,
                        List.of(120.0, 240.0, 360.0)
                ),
                List.of(
                        new ActivityStatsDto("Review", 3, 45.0, 30.0, 60.0),
                        new ActivityStatsDto("Approve", 2, 90.0, 80.0, 100.0)
                ),
                List.of(
                        new ResourceStatsDto("Manager", 2, 5, 180.0, 75.0),
                        new ResourceStatsDto("Analyst", 1, 4, 120.0, 50.0)
                ),
                new ChartsDto(
                        List.of(
                                new ChartPointDto("Review", 3.0, null, null, null, null),
                                new ChartPointDto("Approve", 2.0, null, null, null, null)
                        ),
                        List.of(
                                new ChartPointDto("Review", 45.0, null, null, null, null),
                                new ChartPointDto("Approve", 90.0, null, null, null, null)
                        ),
                        List.of(
                                new ChartPointDto("Manager", 2.0, null, null, null, null),
                                new ChartPointDto("Analyst", 1.0, null, null, null, null)
                        ),
                        List.of(
                                new ChartPointDto("0-120", 1.0, "0-120", 0.0, 120.0, 1),
                                new ChartPointDto("120-240", 2.0, "120-240", 120.0, 240.0, 2)
                        )
                )
        );

        SimulationResultService simulationResultService = new StubSimulationResultService(dto);
        SimulationController controller = new SimulationController(null, null, simulationResultService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/simulations/{runId}/result", runId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.runId").value(runId))
                .andExpect(jsonPath("$.summary.totalRecords").value(42))
                .andExpect(jsonPath("$.summary.processCount").value(3))
                .andExpect(jsonPath("$.summary.firstEventTime").value("2026-04-10T10:00:00"))
                .andExpect(jsonPath("$.summary.lastFinishTime").value("2026-04-10T10:20:00"))
                .andExpect(jsonPath("$.activities[0].name").value("Review"))
                .andExpect(jsonPath("$.activities[0].count").value(3))
                .andExpect(jsonPath("$.activities[0].avgDurationSec").value(45.0))
                .andExpect(jsonPath("$.resources[0].name").value("Manager"))
                .andExpect(jsonPath("$.resources[0].blockCount").value(2))
                .andExpect(jsonPath("$.resources[0].availableCount").value(5))
                .andExpect(jsonPath("$.charts.activityCounts[0].name").value("Review"))
                .andExpect(jsonPath("$.charts.activityCounts[0].value").value(3.0))
                .andExpect(jsonPath("$.charts.activityAvgDurations[0].name").value("Review"))
                .andExpect(jsonPath("$.charts.activityAvgDurations[0].value").value(45.0))
                .andExpect(jsonPath("$.charts.resourceBlocks[0].name").value("Manager"))
                .andExpect(jsonPath("$.charts.resourceBlocks[0].value").value(2.0))
                .andExpect(jsonPath("$.charts.processDurationHistogram[0].name").value("0-120"))
                .andExpect(jsonPath("$.charts.processDurationHistogram[0].value").value(1.0));
    }

    private static final class StubSimulationResultService extends SimulationResultService {

        private final SimulationResultDto dto;

        private StubSimulationResultService(SimulationResultDto dto) {
            super(null, null, null, null);
            this.dto = dto;
        }

        @Override
        public SimulationResultDto buildResult(String runId) {
            return dto;
        }
    }
}
