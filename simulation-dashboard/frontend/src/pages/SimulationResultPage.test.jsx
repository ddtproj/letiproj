import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

function createSimulationDto(overrides = {}) {
  return {
    runId: "sim_analytics_001",
    startedAt: "2026-04-13T10:00:00Z",
    finishedAt: "2026-04-13T10:30:00Z",
    summary: {
      processCount: 3,
      completedProcessCount: 3,
      failedProcessCount: 0,
      activityStartedCount: 6,
      activityCompletedCount: 6,
      blockedWaitCount: 1,
      simulationTimeSec: 1800
    },
    processStats: {
      avgDurationSec: 600,
      minDurationSec: 300,
      maxDurationSec: 900
    },
    activities: [
      {
        name: "Review",
        count: 2,
        avgDurationSec: 120,
        minDurationSec: 60,
        maxDurationSec: 180
      }
    ],
    resources: [
      {
        name: "Manager",
        blockCount: 1,
        availableCount: 2,
        observedWorkTimeSec: 240,
        utilizationPercent: 40
      }
    ],
    charts: {
      activityCounts: [{ name: "Review", value: 2 }],
      activityAvgDurations: [{ name: "Review", value: 120 }],
      resourceBlocks: [{ name: "Manager", value: 1 }],
      processDurationHistogram: [
        {
          name: "0-5 min",
          value: 1,
          binLabel: "0-5 min",
          from: 0,
          to: 300,
          count: 1
        }
      ]
    },
    ...overrides
  };
}

async function renderPage({
  runs = [],
  status = { runId: "", status: "idle" },
  hookResult = { data: null, isLoading: false, error: null },
  defaultRunId = ""
} = {}) {
  vi.resetModules();

  vi.doMock("../hooks/useSimulationResult", () => ({
    useSimulationResult: vi.fn(() => hookResult)
  }));

  vi.doMock("../services/api", () => ({
    DEFAULT_RUN_ID: defaultRunId,
    fetchRuns: vi.fn().mockResolvedValue(runs),
    fetchRunStatus: vi.fn().mockResolvedValue(status),
    createSimulationRun: vi.fn()
  }));

  const { SimulationResultPage } = await import("./SimulationResultPage");
  return render(<SimulationResultPage />);
}

describe("SimulationResultPage", () => {
  afterEach(() => {
    cleanup();
    vi.resetModules();
    vi.clearAllMocks();
    vi.unmock("../hooks/useSimulationResult");
    vi.unmock("../services/api");
  });

  it("renders the empty state when there are no runs yet", async () => {
    await renderPage();

    await waitFor(() => {
      expect(screen.getByText("Результаты симуляции")).toBeTruthy();
      expect(screen.getByText("Запусков пока нет. Укажите путь к BPMN-файлу и создайте первый запуск.")).toBeTruthy();
    });
  });

  it("renders aggregated DTO data when a completed run is selected", async () => {
    await renderPage({
      runs: [
        {
          runId: "sim_analytics_001",
          status: "completed",
          hasResult: true
        }
      ],
      status: {
        runId: "sim_analytics_001",
        status: "completed"
      },
      hookResult: {
        data: createSimulationDto(),
        isLoading: false,
        error: null
      },
      defaultRunId: "sim_analytics_001"
    });

    await waitFor(() => {
      expect(screen.getByText("Результаты симуляции")).toBeTruthy();
      expect(screen.getByText("Путь к спецификации")).toBeTruthy();
      expect(screen.getByText("Создать запуск")).toBeTruthy();
      expect(screen.getByText("Запуск")).toBeTruthy();
    });
  });
});
