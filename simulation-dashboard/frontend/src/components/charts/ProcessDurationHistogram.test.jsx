import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProcessDurationHistogram } from "./ProcessDurationHistogram";
import { formatDurationSeconds } from "../../utils/formatters";

describe("ProcessDurationHistogram", () => {
  it("renders histogram intervals on X and process counts on Y directly from backend aggregates", () => {
    const dto = [
      { binLabel: "0-120", from: 0, to: 120, count: 1 },
      { binLabel: "120-240", from: 120, to: 240, count: 3 }
    ];

    const { container } = render(<ProcessDurationHistogram data={dto} />);

    expect(container.querySelector(".chart-box")).toBeTruthy();
    expect(container.querySelector(".recharts-responsive-container")).toBeTruthy();
    expect(formatDurationSeconds(dto[1].from)).toBe("2 мин");
    expect(formatDurationSeconds(dto[1].to)).toBe("4 мин");
  });

  it("shows an empty-state message when histogram data is absent", () => {
    render(<ProcessDurationHistogram data={[]} />);

    expect(screen.getByText(/Недостаточно данных для построения распределения/)).toBeTruthy();
  });
});
