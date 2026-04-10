import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { formatDurationSeconds, formatInteger } from "../../utils/formatters";

export function ProcessDurationHistogram({ data = [] }) {
  if (!data.length) {
    return <div>Недостаточно данных для построения распределения</div>;
  }

  return (
    <div className="chart-box">
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={data}>
          <XAxis dataKey="binLabel" />
          <YAxis tickFormatter={(value) => formatInteger(value)} />
          <Tooltip
            formatter={(value) => formatInteger(value)}
            labelFormatter={(_, payload) => {
              const point = payload?.[0]?.payload;
              if (!point) {
                return "Интервал";
              }
              return `${formatDurationSeconds(point.from)} - ${formatDurationSeconds(point.to)}`;
            }}
          />
          <Bar dataKey="count" fill="#805ad5" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
