# LetiProj

`LetiProj` — это Java-ориентированный проект для BPMN-симуляции и аналитики результатов.

Основные части проекта:

- `simulation-dashboard/frontend` — React/Vite интерфейс
- `simulation-dashboard/backend-java` — Spring Boot backend API
- `letitsim-main` — Java-движок симуляции, который запускает backend

Система запускает симуляцию по BPMN-файлу, сохраняет лог, парсит его, считает агрегированные метрики и отдает фронту готовые DTO для таблиц и графиков.

Английская версия README: [README.en.md](README.en.md)

## Что умеет система

1. Пользователь указывает путь к BPMN-файлу во frontend.
2. Java backend создает `run` и запускает движок `letitsim-main`.
3. Движок выполняет симуляцию и сохраняет артефакты запуска.
4. Backend парсит лог и считает summary, activity, resource и histogram метрики.
5. Frontend отображает аналитику не из сырых событий, а из готовых агрегированных DTO.

## Структура репозитория

### Активные части

- `simulation-dashboard/frontend`
- `simulation-dashboard/backend-java`
- `simulation-dashboard/docs`
- `simulation-dashboard/shared`
- `letitsim-main`

### Архивные материалы

- `archive/legacy-bimp-ui` — старая UI-реализация
- `archive/reference/BPSimpyLibrary-main` — reference-материалы по BPSim
- `archive/tools` — разовые вспомогательные скрипты

## Быстрый запуск

В корне проекта есть готовые bat-файлы:

- [`run-backend.bat`](run-backend.bat)
- [`run-frontend.bat`](run-frontend.bat)

### Вариант 1. Через bat-файлы

Сначала backend:

```cmd
run-backend.bat
```

Потом frontend:

```cmd
run-frontend.bat
```

### Вариант 2. Ручной запуск

#### Backend

Требования:

- Java 17+
- Maven 3.9+

```cmd
cd /d simulation-dashboard\backend-java
mvn spring-boot:run
```

Backend по умолчанию стартует на:

- `http://127.0.0.1:8000`

#### Frontend

Требования:

- Node.js 18+

```cmd
cd /d simulation-dashboard\frontend
npm install
npm run dev
```

Frontend по умолчанию стартует на:

- `http://localhost:5173`

## Как запустить симуляцию

1. Открой frontend в браузере.
2. Вставь путь к BPMN-файлу.
3. Нажми `Создать запуск`.
4. Выбери созданный `run` в списке.

Пример пути к BPMN на Windows:

```text
c:\Users\Admin\Desktop\bimp-ui-master\letitsim-main\credit_card_application.bpmn
```

## Где лежат логи симуляции

Для каждого запуска backend создает отдельную папку в:

`simulation-dashboard/backend-java/storage/runs/<runId>/`

Там лежат:

- `simulation.log` — лог симуляции
- `parsed-log-dto.json` — распарсенный JSON
- `stdout.log` — стандартный вывод процесса
- `stderr.log` — ошибки процесса
- `meta.json` — статус запуска и служебные данные

## Тесты

### Backend

```cmd
cd /d simulation-dashboard\backend-java
mvn test
```

### Frontend

```cmd
cd /d simulation-dashboard\frontend
npm test
```

## Ключевые особенности

- backend runtime полностью на Java
- frontend получает готовые DTO, а не сырые события
- есть отдельные DTO для summary, activity, resource и histogram аналитики
- frontend строит таблицы и графики прямо из server-side DTO
- legacy-материалы отделены от активного продукта

## Важные замечания

- `simulation-dashboard/backend-java` запускает `letitsim-main` как отдельный Java-процесс
- после изменений в движке нужно пересобирать `letitsim-main`
- не удаляй `letitsim-main/target/classes`, если не собираешь движок заново

## Полезные файлы

- [`simulation-dashboard/README.md`](simulation-dashboard/README.md)
- [`simulation-dashboard/backend-java/README.md`](simulation-dashboard/backend-java/README.md)
- [`README.en.md`](README.en.md)

## Лицензия

См. [LICENSE](LICENSE).
