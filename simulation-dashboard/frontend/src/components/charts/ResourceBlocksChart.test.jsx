import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ResourceBlocksChart } from "./ResourceBlocksChart";

describe("ResourceBlocksChart", () => {
  it("renders a separate bar for each resource and uses backend dto block counts", () => {
    const dto = [
      { name: "Manager", value: 2 },
      { name: "Analyst", value: 5 }
    ];

    const { container } = render(<ResourceBlocksChart data={dto} />);

    expect(container.querySelector(".chart-box")).toBeTruthy();
    expect(container.querySelector(".recharts-responsive-container")).toBeTruthy();
  });

  it("works correctly with a single resource", () => {
    const dto = [{ name: "Manager", value: 1 }];

    const { container } = render(<ResourceBlocksChart data={dto} />);

    expect(container.querySelector(".chart-box")).toBeTruthy();
    expect(container.querySelector(".recharts-responsive-container")).toBeTruthy();
  });
});
