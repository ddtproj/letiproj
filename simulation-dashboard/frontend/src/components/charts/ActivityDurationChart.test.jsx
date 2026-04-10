import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ActivityDurationChart } from "./ActivityDurationChart";

const capturedProps = {
  barChart: null,
  xAxis: null,
  yAxis: null,
  bar: null,
  tooltip: null
};

vi.mock("recharts", () => ({
  ResponsiveContainer: ({ children }) => <div data-testid="responsive-container">{children}</div>,
  BarChart: ({ data, children }) => {
    capturedProps.barChart = { data };
    return <div data-testid="bar-chart">{children}</div>;
  },
  Bar: (props) => {
    capturedProps.bar = props;
    return <div data-testid="bar-series" />;
  },
  XAxis: (props) => {
    capturedProps.xAxis = props;
    return <div data-testid="x-axis" />;
  },
  YAxis: (props) => {
    capturedProps.yAxis = props;
    return <div data-testid="y-axis" />;
  },
  Tooltip: (props) => {
    capturedProps.tooltip = props;
    return <div data-testid="tooltip" />;
  }
}));

describe("ActivityDurationChart", () => {
  it("renders average activity durations from a dedicated dto and formats values as time", () => {
    const dto = [
      { name: "Review", value: 45 },
      { name: "Approve", value: 120 }
    ];

    render(<ActivityDurationChart data={dto} />);

    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
    expect(capturedProps.barChart.data).toBe(dto);
    expect(capturedProps.xAxis.dataKey).toBe("name");
    expect(capturedProps.bar.dataKey).toBe("value");
    expect(capturedProps.yAxis.tickFormatter(45)).toBe("45 сек");
    expect(capturedProps.yAxis.tickFormatter(120)).toBe("2 мин");
    expect(capturedProps.tooltip.formatter(120)).toBe("2 мин");
  });
});
