# Simulation Dashboard

Product layer of the repository that combines:

- `backend-java/` - Spring Boot backend API
- `frontend/` - React + Vite user interface
- `shared/` - shared DTO examples and schema
- `docs/` - architecture and integration notes

## Responsibilities

- accept a BPMN specification path
- create and track simulation runs
- launch the Java simulation engine from `../letitsim-main`
- store run artifacts
- parse event logs
- calculate summary, activity, resource, and histogram metrics
- return ready-to-render DTOs for the frontend

## Current stack

- Backend: Java 17 + Spring Boot
- Frontend: React 18 + Vite
- Engine: Java simulator from `letitsim-main`
- Transport: JSON over HTTP

## Run locally

### Backend

```bash
cd backend-java
mvn spring-boot:run
```

Default backend URL:

- `http://127.0.0.1:8000`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Tests

### Backend

```bash
cd backend-java
mvn test
```

### Frontend

```bash
cd frontend
npm test
```

## Important runtime note

For real simulation runs, the backend expects the engine project `letitsim-main` to be compiled and to contain `target/classes`.
