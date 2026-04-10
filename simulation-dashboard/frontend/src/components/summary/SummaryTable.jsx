import { SummaryCard } from "./SummaryCard";
import { formatDateTime, formatDurationSeconds, formatInteger } from "../../utils/formatters";

export function SummaryTable({ summary, processStats, startedAt, finishedAt }) {
  return (
    <div className="grid three-cols">
      <SummaryCard label="Время симуляции" value={formatDurationSeconds(summary?.simulationTimeSec)} />
      <SummaryCard label="Количество процессов" value={formatInteger(summary?.processCount)} />
      <SummaryCard label="Завершенные процессы" value={formatInteger(summary?.completedProcessCount)} />
      <SummaryCard label="Процессы с ошибкой" value={formatInteger(summary?.failedProcessCount)} />
      <SummaryCard label="Запуски активностей" value={formatInteger(summary?.activityStartedCount)} />
      <SummaryCard label="Завершения активностей" value={formatInteger(summary?.activityCompletedCount)} />
      <SummaryCard label="Ожидания из-за блокировки" value={formatInteger(summary?.blockedWaitCount)} />
      <SummaryCard label="Средняя длительность процесса" value={formatDurationSeconds(processStats?.avgDurationSec)} />
      <SummaryCard label="Минимальная длительность процесса" value={formatDurationSeconds(processStats?.minDurationSec)} />
      <SummaryCard label="Максимальная длительность процесса" value={formatDurationSeconds(processStats?.maxDurationSec)} />
      <SummaryCard label="Время начала" value={formatDateTime(startedAt)} />
      <SummaryCard label="Время завершения" value={formatDateTime(finishedAt)} />
    </div>
  );
}
