export function SummaryCard({ label, value }) {
  return (
    <div className="card">
      <div className="muted">{label}</div>
      <div>{value ?? "—"}</div>
    </div>
  );
}
