import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ActivityCountChart } from "./ActivityCountChart";

const capturedProps = {
  barChart: null,
  xAxis: null,
  yAxis: null,
  bar: null
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
  Tooltip: () => <div data-testid="tooltip" />
}));

describe("ActivityCountChart", () => {
  it("renders activity execution counts directly from backend dto using activity names on X and counts on Y", () => {
    const dto = [
      { name: "Review", value: 3 },
      { name: "Approve", value: 5 }
    ];

    render(<ActivityCountChart data={dto} />);

    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
    expect(capturedProps.barChart.data).toBe(dto);
    expect(capturedProps.xAxis.dataKey).toBe("name");
    expect(capturedProps.bar.dataKey).toBe("value");
    expect(typeof capturedProps.yAxis.tickFormatter).toBe("function");
  });
});
