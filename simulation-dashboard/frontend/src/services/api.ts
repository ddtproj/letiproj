import mock from "../mocks/simulationResult.mock.json";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://127.0.0.1:8000";
const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === "true";
export const DEFAULT_RUN_ID = "";

export async function fetchRuns() {
  if (USE_MOCKS) {
    return Promise.resolve([
      { runId: "real_run_001", status: "completed", hasResult: true },
      { runId: "sim_demo_001", status: "completed", hasResult: true }
    ]);
  }

  const response = await fetch(`${API_BASE_URL}/api/simulations`);

  if (!response.ok) {
    throw new Error(`Runs request failed with status ${response.status}`);
  }

  return response.json();
}

export async function fetchSimulationResult(runId = DEFAULT_RUN_ID) {
  if (USE_MOCKS) {
    return Promise.resolve(mock);
  }

  if (!runId) {
    return null;
  }

  const response = await fetch(`${API_BASE_URL}/api/simulations/${runId}/result`);

  if (!response.ok) {
    throw new Error(`Backend request failed with status ${response.status}`);
  }

  return response.json();
}

export async function fetchRunStatus(runId = DEFAULT_RUN_ID) {
  if (USE_MOCKS) {
    return Promise.resolve({ runId, status: "completed" });
  }

  if (!runId) {
    return { runId: "", status: "idle" };
  }

  const response = await fetch(`${API_BASE_URL}/api/simulations/${runId}/status`);

  if (!response.ok) {
    throw new Error(`Status request failed with status ${response.status}`);
  }

  return response.json();
}

export async function createSimulationRun(specificationPath) {
  if (USE_MOCKS) {
    return Promise.resolve({
      runId: `sim_mock_${Date.now()}`,
      status: "queued"
    });
  }

  const response = await fetch(`${API_BASE_URL}/api/simulations/run`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      specification_path: specificationPath || null
    })
  });

  if (!response.ok) {
    throw new Error(`Run creation failed with status ${response.status}`);
  }

  return response.json();
}
