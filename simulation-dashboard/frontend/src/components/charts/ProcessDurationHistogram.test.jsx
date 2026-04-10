import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ProcessDurationHistogram } from "./ProcessDurationHistogram";

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

describe("ProcessDurationHistogram", () => {
  it("renders histogram intervals on X and process counts on Y directly from backend aggregates", () => {
    const dto = [
      { binLabel: "0-120", from: 0, to: 120, count: 1 },
      { binLabel: "120-240", from: 120, to: 240, count: 3 }
    ];

    render(<ProcessDurationHistogram data={dto} />);

    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
    expect(capturedProps.barChart.data).toBe(dto);
    expect(capturedProps.xAxis.dataKey).toBe("binLabel");
    expect(capturedProps.bar.dataKey).toBe("count");
    expect(typeof capturedProps.yAxis.tickFormatter).toBe("function");
    expect(capturedProps.tooltip.labelFormatter(null, [{ payload: dto[1] }])).toBe("2 мин - 4 мин");
  });

  it("shows an empty-state message when histogram data is absent", () => {
    render(<ProcessDurationHistogram data={[]} />);

    expect(
      screen.getByText(/Недостаточно данных для построения распределения/)
    ).toBeInTheDocument();
  });
});
