import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { formatDurationSeconds, translateLabel, truncateLabel } from "../../utils/formatters";

export function ActivityDurationChart({ data = [] }) {
  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data}>
          <XAxis dataKey="name" tickFormatter={(value) => truncateLabel(translateLabel(value))} interval={0} angle={-20} textAnchor="end" height={60} />
          <YAxis tickFormatter={(value) => formatDurationSeconds(value)} width={80} />
          <Tooltip formatter={(value) => formatDurationSeconds(value)} labelFormatter={(value) => translateLabel(value)} />
          <Bar dataKey="value" fill="#2f855a" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
