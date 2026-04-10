export function EmptyState({ message = "Нет данных" }) {
  return <div className="state">{message}</div>;
}
