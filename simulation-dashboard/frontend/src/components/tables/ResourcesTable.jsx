import { formatDurationSeconds, formatInteger, formatPercent, translateLabel } from "../../utils/formatters";

export function ResourcesTable({ rows = [] }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Ресурс</th>
            <th>Количество блокировок</th>
            <th>Сигналы доступности</th>
            <th>Наблюдаемое время работы</th>
            <th>Утилизация, %</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan="5">Нет данных</td>
            </tr>
          ) : (
            rows.map((row) => (
              <tr key={row.name}>
                <td>{translateLabel(row.name)}</td>
                <td>{formatInteger(row.blockCount)}</td>
                <td>{formatInteger(row.availableCount)}</td>
                <td>{formatDurationSeconds(row.observedWorkTimeSec)}</td>
                <td>{formatPercent(row.utilizationPercent)}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
