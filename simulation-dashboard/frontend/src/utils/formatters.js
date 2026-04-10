function isMissing(value) {
  return value === null || value === undefined || Number.isNaN(value);
}

const labelTranslations = {
  "Assess application": "Оценить заявку",
  "Check credit history": "Проверить кредитную историю",
  "Check income sources": "Проверить источники дохода",
  "Credit application processed": "Заявка обработана",
  "Make credit offer": "Подготовить кредитное предложение",
  "Notify rejection": "Отправить отказ",
  "Receive customer feedback": "Получить ответ клиента",
  Clerk: "Клерк",
  "Credit Officer": "Кредитный специалист"
};

const runStatusTranslations = {
  idle: "ожидание запуска",
  queued: "в очереди",
  running: "выполняется",
  completed: "завершен",
  failed: "ошибка"
};

export function formatNumber(value, maximumFractionDigits = 2) {
  if (isMissing(value)) {
    return "—";
  }

  return new Intl.NumberFormat("ru-RU", {
    maximumFractionDigits
  }).format(value);
}

export function formatInteger(value) {
  if (isMissing(value)) {
    return "—";
  }

  return new Intl.NumberFormat("ru-RU", {
    maximumFractionDigits: 0
  }).format(value);
}

export function formatPercent(value) {
  if (isMissing(value)) {
    return "—";
  }

  return `${formatNumber(value)}%`;
}

export function formatDateTime(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ru-RU", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  }).format(date);
}

export function formatDurationSeconds(value) {
  if (isMissing(value)) {
    return "—";
  }

  const totalSeconds = Number(value);
  if (totalSeconds < 60) {
    return `${formatNumber(totalSeconds)} сек`;
  }

  const totalMinutes = totalSeconds / 60;
  if (totalMinutes < 60) {
    return `${formatNumber(totalMinutes)} мин`;
  }

  const totalHours = totalMinutes / 60;
  if (totalHours < 24) {
    return `${formatNumber(totalHours)} ч`;
  }

  const totalDays = totalHours / 24;
  return `${formatNumber(totalDays)} д`;
}

export function truncateLabel(value, maxLength = 16) {
  if (!value) {
    return "—";
  }

  return value.length > maxLength ? `${value.slice(0, maxLength - 1)}…` : value;
}

export function translateLabel(value) {
  if (!value) {
    return "—";
  }

  return labelTranslations[value] || value;
}

export function formatRunStatus(value) {
  if (!value) {
    return "—";
  }

  return runStatusTranslations[value] || value;
}
