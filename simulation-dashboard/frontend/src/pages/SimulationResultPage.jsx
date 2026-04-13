import { useEffect, useState } from "react";
import { useSimulationResult } from "../hooks/useSimulationResult";
import { DEFAULT_RUN_ID, createSimulationRun, fetchRuns, fetchRunStatus } from "../services/api";
import { SummaryTable } from "../components/summary/SummaryTable";
import { ActivitiesTable } from "../components/tables/ActivitiesTable";
import { ResourcesTable } from "../components/tables/ResourcesTable";
import { ActivityCountChart } from "../components/charts/ActivityCountChart";
import { ActivityDurationChart } from "../components/charts/ActivityDurationChart";
import { ResourceBlocksChart } from "../components/charts/ResourceBlocksChart";
import { ProcessDurationHistogram } from "../components/charts/ProcessDurationHistogram";
import { Loader } from "../components/common/Loader";
import { ErrorState } from "../components/common/ErrorState";
import { EmptyState } from "../components/common/EmptyState";
import { PageSection } from "../components/common/PageSection";
import { formatRunStatus } from "../utils/formatters";

const EMPTY_CHARTS = {
  activityCounts: [],
  activityAvgDurations: [],
  resourceBlocks: [],
  processDurationHistogram: []
};

export function SimulationResultPage() {
  const [runId, setRunId] = useState(DEFAULT_RUN_ID);
  const [runs, setRuns] = useState([]);
  const [runStatus, setRunStatus] = useState("idle");
  const [newSpecPath, setNewSpecPath] = useState("");
  const [createError, setCreateError] = useState(null);
  const [isCreating, setIsCreating] = useState(false);

  const shouldFetchResult = runStatus === "completed";
  const { data, isLoading, error } = useSimulationResult(runId, shouldFetchResult);
  const hasResultData = Boolean(data);

  useEffect(() => {
    let active = true;

    fetchRuns()
      .then((items) => {
        if (!active) {
          return;
        }

        setRuns(items);

        if (items.length === 0) {
          setRunId("");
          setRunStatus("idle");
          return;
        }

        if (!items.some((item) => item.runId === runId)) {
          setRunId(items[0].runId);
        }
      })
      .catch(() => {
        if (active) {
          setRuns([]);
        }
      });

    return () => {
      active = false;
    };
  }, [runId]);

  useEffect(() => {
    if (!runId) {
      setRunStatus("idle");
      return;
    }

    let active = true;

    fetchRunStatus(runId)
      .then((result) => {
        if (active) {
          setRunStatus(result.status);
        }
      })
      .catch(() => {
        if (active) {
          setRunStatus("failed");
        }
      });

    return () => {
      active = false;
    };
  }, [runId]);

  useEffect(() => {
    if (!runId || !["queued", "running"].includes(runStatus)) {
      return;
    }

    let active = true;
    const timer = setInterval(async () => {
      try {
        const [statusResult, runItems] = await Promise.all([fetchRunStatus(runId), fetchRuns()]);

        if (!active) {
          return;
        }

        setRunStatus(statusResult.status);
        setRuns(runItems);
      } catch {
        if (active) {
          setRunStatus("failed");
        }
      }
    }, 2000);

    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [runId, runStatus]);

  async function handleCreateRun(event) {
    event.preventDefault();
    setIsCreating(true);
    setCreateError(null);

    try {
      const createdRun = await createSimulationRun(newSpecPath.trim());
      const items = await fetchRuns();
      setRuns(items);
      setRunId(createdRun.runId);
      setRunStatus(createdRun.status);
    } catch (err) {
      setCreateError(err.message);
    } finally {
      setIsCreating(false);
    }
  }

  if (isLoading && runId && shouldFetchResult) {
    return <Loader />;
  }

  if (error && runStatus === "completed") {
    return <ErrorState message={error.message} />;
  }

  const summary = data?.summary ?? {};
  const processStats = data?.processStats ?? {};
  const activities = data?.activities ?? [];
  const resources = data?.resources ?? [];
  const charts = data?.charts ?? EMPTY_CHARTS;

  const visibleRuns = (runs.length > 0 ? runs : runId ? [{ runId, status: runStatus, hasResult: true }] : []).filter(
    (run) => run.hasResult || run.runId === runId
  );

  const hiddenQueuedCount = runs.filter((run) => !run.hasResult && run.runId !== runId).length;

  return (
    <main className="page">
      <h1>Результаты симуляции</h1>

      <form className="toolbar toolbar-form" onSubmit={handleCreateRun}>
        <label htmlFor="spec-path">Путь к спецификации</label>
        <input
          id="spec-path"
          type="text"
          value={newSpecPath}
          onChange={(event) => setNewSpecPath(event.target.value)}
          placeholder="c:/path/to/process.bpmn"
        />
        <button type="submit" disabled={isCreating}>
          {isCreating ? "Создание..." : "Создать запуск"}
        </button>
      </form>

      {createError ? <p className="muted">{createError}</p> : null}

      <div className="toolbar">
        <label htmlFor="run-select">Запуск</label>
        <select
          id="run-select"
          value={runId}
          onChange={(event) => setRunId(event.target.value)}
          disabled={visibleRuns.length === 0}
        >
          {visibleRuns.length === 0 ? <option value="">Нет доступных запусков</option> : null}
          {visibleRuns.map((run) => (
            <option key={run.runId} value={run.runId}>
              {run.runId}
              {run.status ? ` (${formatRunStatus(run.status)})` : ""}
            </option>
          ))}
        </select>
      </div>

      {hiddenQueuedCount > 0 ? (
        <p className="muted">Скрыто запусков без результата: {hiddenQueuedCount}</p>
      ) : null}

      <p className="muted">Идентификатор запуска: {data?.runId ?? "—"}</p>
      <p className="muted">Статус: {formatRunStatus(runStatus)}</p>

      {!runId && !hasResultData ? (
        <EmptyState message="Запусков пока нет. Укажите путь к BPMN-файлу и создайте первый запуск." />
      ) : null}

      {runId && runStatus === "failed" ? (
        <ErrorState message="Запуск завершился с ошибкой. Проверьте логи backend для этого run." />
      ) : null}

      {runId && !hasResultData && runStatus !== "completed" ? (
        <EmptyState message="Результат для этого запуска пока недоступен. Дождитесь завершения симуляции." />
      ) : null}

      {hasResultData ? (
        <>
          <PageSection title="Сводка">
            <SummaryTable
              summary={summary}
              processStats={processStats}
              startedAt={data?.startedAt}
              finishedAt={data?.finishedAt}
            />
          </PageSection>

          <section className="grid two-cols">
            <PageSection title="Статистика по активностям">
              <ActivitiesTable rows={activities} />
            </PageSection>
            <PageSection title="Статистика по ресурсам">
              <ResourcesTable rows={resources} />
            </PageSection>
          </section>

          <section className="grid three-cols">
            <PageSection title="Количество выполнений активностей">
              <ActivityCountChart data={charts.activityCounts} />
            </PageSection>
            <PageSection title="Средняя длительность активностей">
              <ActivityDurationChart data={charts.activityAvgDurations} />
            </PageSection>
            <PageSection title="Блокировки ресурсов">
              <ResourceBlocksChart data={charts.resourceBlocks} />
            </PageSection>
          </section>

          <PageSection title="Распределение длительности процессов">
            <ProcessDurationHistogram data={charts.processDurationHistogram} />
          </PageSection>
        </>
      ) : null}
    </main>
  );
}
