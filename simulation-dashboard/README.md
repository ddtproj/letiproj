# Simulation Dashboard

Актуальная продуктовая часть репозитория.

Состав:

- `backend-java/` — основной backend на Spring Boot
- `frontend/` — пользовательский интерфейс на React + Vite
- `shared/` — контракт DTO
- `docs/` — архитектурные и интеграционные заметки

## Что делает система

- принимает путь к BPMN-файлу
- создает запуск симуляции
- вызывает Java-движок из `../letitsim-main`
- сохраняет артефакты запуска
- парсит лог событий
- считает summary, activity, resource и histogram метрики
- отдает готовый DTO для frontend

## Текущий стек

- Backend: Java 17 + Spring Boot
- Frontend: React 18 + Vite
- Engine: Java simulator из `letitsim-main`
- Transport: JSON over HTTP

## Структура

- [backend-java](/c:/Users/Admin/Desktop/bimp-ui-master/simulation-dashboard/backend-java)
- [frontend](/c:/Users/Admin/Desktop/bimp-ui-master/simulation-dashboard/frontend)
- [shared](/c:/Users/Admin/Desktop/bimp-ui-master/simulation-dashboard/shared)
- [docs](/c:/Users/Admin/Desktop/bimp-ui-master/simulation-dashboard/docs)

## Запуск

### Backend

```bash
cd backend-java
mvn spring-boot:run
```

По умолчанию сервис поднимается на `http://127.0.0.1:8000`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Важно

Для реального запуска симуляции backend ожидает, что `letitsim-main` уже собран и содержит `target/classes`.
Если движок еще не собран, сначала собери его по инструкции в [letitsim-main/README.md](/c:/Users/Admin/Desktop/bimp-ui-master/letitsim-main/README.md).
