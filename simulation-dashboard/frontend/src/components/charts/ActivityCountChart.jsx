import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { formatInteger, translateLabel, truncateLabel } from "../../utils/formatters";

export function ActivityCountChart({ data = [] }) {
  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data}>
          <XAxis dataKey="name" tickFormatter={(value) => truncateLabel(translateLabel(value))} interval={0} angle={-20} textAnchor="end" height={60} />
          <YAxis tickFormatter={(value) => formatInteger(value)} />
          <Tooltip formatter={(value) => formatInteger(value)} labelFormatter={(value) => translateLabel(value)} />
          <Bar dataKey="value" fill="#2b6cb0" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
