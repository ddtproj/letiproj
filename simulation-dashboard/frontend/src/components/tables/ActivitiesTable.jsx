import { formatDurationSeconds, formatInteger, translateLabel } from "../../utils/formatters";

export function ActivitiesTable({ rows = [] }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Активность</th>
            <th>Количество</th>
            <th>Средняя длительность</th>
            <th>Минимум</th>
            <th>Максимум</th>
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
                <td>{formatInteger(row.count)}</td>
                <td>{formatDurationSeconds(row.avgDurationSec)}</td>
                <td>{formatDurationSeconds(row.minDurationSec)}</td>
                <td>{formatDurationSeconds(row.maxDurationSec)}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
