import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityDurationChart } from "./ActivityDurationChart";
import { formatDurationSeconds } from "../../utils/formatters";

describe("ActivityDurationChart", () => {
  it("renders average activity durations from a dedicated dto and formats values as time", () => {
    const dto = [
      { name: "Review", value: 45 },
      { name: "Approve", value: 120 }
    ];

    const { container } = render(<ActivityDurationChart data={dto} />);

    expect(container.querySelector(".chart-box")).toBeTruthy();
    expect(container.querySelector(".recharts-responsive-container")).toBeTruthy();
    expect(formatDurationSeconds(45)).toBe("45 сек");
    expect(formatDurationSeconds(120)).toBe("2 мин");
  });
});
