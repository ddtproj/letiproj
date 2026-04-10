import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ResourceBlocksChart } from "./ResourceBlocksChart";

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

describe("ResourceBlocksChart", () => {
  it("renders a separate bar for each resource and uses backend dto block counts", () => {
    const dto = [
      { name: "Manager", value: 2 },
      { name: "Analyst", value: 5 }
    ];

    render(<ResourceBlocksChart data={dto} />);

    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
    expect(capturedProps.barChart.data).toBe(dto);
    expect(capturedProps.barChart.data).toHaveLength(2);
    expect(capturedProps.xAxis.dataKey).toBe("name");
    expect(capturedProps.bar.dataKey).toBe("value");
    expect(capturedProps.tooltip.formatter(5)).toBe("5");
  });

  it("works correctly with a single resource", () => {
    const dto = [{ name: "Manager", value: 1 }];

    render(<ResourceBlocksChart data={dto} />);

    expect(capturedProps.barChart.data).toBe(dto);
    expect(capturedProps.barChart.data).toHaveLength(1);
    expect(capturedProps.barChart.data[0]).toEqual({ name: "Manager", value: 1 });
  });
});
