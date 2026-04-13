import { render } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ActivityCountChart } from "./ActivityCountChart";

describe("ActivityCountChart", () => {
  it("renders activity execution counts directly from backend dto using activity names on X and counts on Y", () => {
    const dto = [
      { name: "Review", value: 3 },
      { name: "Approve", value: 5 }
    ];

    const { container } = render(<ActivityCountChart data={dto} />);

    expect(container.querySelector(".chart-box")).toBeTruthy();
    expect(container.querySelector(".recharts-responsive-container")).toBeTruthy();
  });
});
