import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { SimulationResultPage } from "./SimulationResultPage";

vi.mock("../components/charts/ActivityCountChart", () => ({
  ActivityCountChart: ({ data = [] }) => (
    <div data-testid="activity-count-chart">
      {data.map((item) => `${item.name}:${item.value}`).join("|")}
    </div>
  )
}));

vi.mock("../components/charts/ActivityDurationChart", () => ({
  ActivityDurationChart: ({ data = [] }) => (
    <div data-testid="activity-duration-chart">
      {data.map((item) => `${item.name}:${item.value}`).join("|")}
    </div>
  )
}));

vi.mock("../components/charts/ResourceBlocksChart", () => ({
  ResourceBlocksChart: ({ data = [] }) => (
    <div data-testid="resource-blocks-chart">
      {data.map((item) => `${item.name}:${item.value}`).join("|")}
    </div>
  )
}));

vi.mock("../components/charts/ProcessDurationHistogram", () => ({
  ProcessDurationHistogram: ({ data = [] }) => (
    <div data-testid="process-duration-histogram">
      {data.map((item) => `${item.binLabel}:${item.count}`).join("|")}
    </div>
  )
}));

vi.mock("../services/api", () => ({
  DEFAULT_RUN_ID: "",
  fetchRuns: vi.fn(),
  fetchRunStatus: vi.fn(),
  fetchSimulationResult: vi.fn(),
  createSimulationRun: vi.fn()
}));

import {
  fetchRuns,
  fetchRunStatus,
  fetchSimulationResult,
  createSimulationRun
} from "../services/api";

function createSimulationDto(overrides = {}) {
  return {
    runId: "sim_analytics_001",
    status: "completed",
    startedAt: "2026-04-10T10:00:00Z",
    finishedAt: "2026-04-10T10:20:00Z",
    summary: {
      simulationTimeSec: 1200,
      processCount: 3000,
      completedProcessCount: 2941,
      failedProcessCount: 59,
      activityStartedCount: 3200,
      activityCompletedCount: 3150,
      blockedWaitCount: 15
    },
    processStats: {
      minDurationSec: 12,
      avgDurationSec: 87.4,
      maxDurationSec: 340,
      durations: [12, 15, 20]
    },
    activities: [
      {
        name: "Review",
        count: 3,
        avgDurationSec: 45,
        minDurationSec: 30,
        maxDurationSec: 60
      }
    ],
    resources: [
      {
        name: "Manager",
        blockCount: 2,
        availableCount: 5,
        observedWorkTimeSec: 180,
        utilizationPercent: 75
      }
    ],
    charts: {
      activityCounts: [{ name: "Review", value: 3 }],
      activityAvgDurations: [{ name: "Review", value: 45 }],
      resourceBlocks: [{ name: "Manager", value: 2 }],
      processDurationHistogram: [{ binLabel: "0-120", from: 0, to: 120, count: 1 }]
    },
    ...overrides
  };
}

describe("SimulationResultPage", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();

    createSimulationRun.mockResolvedValue({
      runId: "sim_analytics_001",
      status: "queued"
    });

    fetchRuns.mockResolvedValue([
      {
        runId: "sim_analytics_001",
        status: "completed",
        hasResult: true
      }
    ]);

    fetchRunStatus.mockResolvedValue({
      runId: "sim_analytics_001",
      status: "completed"
    });

    fetchSimulationResult.mockResolvedValue(createSimulationDto());
  });

  it("loads aggregated dto data and renders tables with all required charts", async () => {
    render(<SimulationResultPage />);

    await waitFor(() => {
      expect(fetchRuns).toHaveBeenCalled();
      expect(fetchRunStatus).toHaveBeenCalledWith("sim_analytics_001");
      expect(fetchSimulationResult).toHaveBeenCalledWith("sim_analytics_001");
    });

    expect(screen.getByRole("heading", { name: "Сводка" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: /sim_analytics_001/ })).toBeInTheDocument();
    expect(
      screen.getByText((content) => content.replace(/\s/g, "") === "3000")
    ).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Статистика по активностям" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Статистика по ресурсам" })).toBeInTheDocument();
    expect(screen.getByText("Review")).toBeInTheDocument();
    expect(screen.getByText("Manager")).toBeInTheDocument();
    expect(screen.getByTestId("activity-count-chart")).toHaveTextContent("Review:3");
    expect(screen.getByTestId("activity-duration-chart")).toHaveTextContent("Review:45");
    expect(screen.getByTestId("resource-blocks-chart")).toHaveTextContent("Manager:2");
    expect(screen.getByTestId("process-duration-histogram")).toHaveTextContent("0-120:1");
  });

  it("reflects changed dto values in tables and charts when backend data changes", async () => {
    fetchSimulationResult.mockResolvedValue(
      createSimulationDto({
        summary: {
          simulationTimeSec: 1800,
          processCount: 4500,
          completedProcessCount: 4400,
          failedProcessCount: 100,
          activityStartedCount: 4700,
          activityCompletedCount: 4600,
          blockedWaitCount: 20
        },
        activities: [
          {
            name: "Approve",
            count: 7,
            avgDurationSec: 120,
            minDurationSec: 60,
            maxDurationSec: 180
          }
        ],
        resources: [
          {
            name: "Analyst",
            blockCount: 4,
            availableCount: 6,
            observedWorkTimeSec: 300,
            utilizationPercent: 80
          }
        ],
        charts: {
          activityCounts: [{ name: "Approve", value: 7 }],
          activityAvgDurations: [{ name: "Approve", value: 120 }],
          resourceBlocks: [{ name: "Analyst", value: 4 }],
          processDurationHistogram: [{ binLabel: "120-240", from: 120, to: 240, count: 3 }]
        }
      })
    );

    render(<SimulationResultPage />);

    expect(await screen.findByText("Approve")).toBeInTheDocument();
    expect(screen.getByText("Analyst")).toBeInTheDocument();
    expect(
      screen.getByText((content) => content.replace(/\s/g, "") === "4500")
    ).toBeInTheDocument();
    expect(screen.getByTestId("activity-count-chart")).toHaveTextContent("Approve:7");
    expect(screen.getByTestId("activity-duration-chart")).toHaveTextContent("Approve:120");
    expect(screen.getByTestId("resource-blocks-chart")).toHaveTextContent("Analyst:4");
    expect(screen.getByTestId("process-duration-histogram")).toHaveTextContent("120-240:3");
  });
});
