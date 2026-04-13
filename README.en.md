# LetiProj

Java-based BPMN simulation workspace with:

- `simulation-dashboard/frontend` - React/Vite analytics UI
- `simulation-dashboard/backend-java` - Spring Boot backend API
- `letitsim-main` - Java simulation engine launched by the backend

The system reads simulation logs, parses events, calculates aggregated metrics, and serves ready-to-render DTOs for tables and charts.

## What the project does

1. The frontend creates a simulation run for a BPMN file.
2. The Java backend launches the simulation engine.
3. The engine produces execution artifacts and a log.
4. The backend parses the log and calculates summary, activity, resource, and histogram metrics.
5. The frontend renders analytics screens from aggregated DTOs instead of raw events.

## Repository structure

### Active parts

- `simulation-dashboard/frontend`
- `simulation-dashboard/backend-java`
- `simulation-dashboard/docs`
- `simulation-dashboard/shared`
- `letitsim-main`

### Archived materials

- `archive/legacy-bimp-ui` - previous UI implementation
- `archive/reference/BPSimpyLibrary-main` - reference Python BPSim materials
- `archive/tools` - one-off helper scripts

## Quick Start

### 1. Build the engine

The backend expects compiled classes inside `letitsim-main/target/classes`.

```bash
cd letitsim-main
mvn package
```

If your engine build differs, see `letitsim-main/README.md`.

### 2. Run the backend

Requirements:

- Java 17+
- Maven 3.9+

```bash
cd simulation-dashboard/backend-java
mvn spring-boot:run
```

Default backend URL:

- `http://127.0.0.1:8000`

### 3. Run the frontend

Requirements:

- Node.js 18+

```bash
cd simulation-dashboard/frontend
npm install
npm run dev
```

Default frontend URL:

- `http://localhost:5173`

### 4. Create a simulation run

Example BPMN path on Windows:

```text
c:\Users\Admin\Desktop\bimp-ui-master\letitsim-main\credit_card_application.bpmn
```

## Testing

### Backend

```bash
cd simulation-dashboard/backend-java
mvn test
```

### Frontend

```bash
cd simulation-dashboard/frontend
npm test
```

## Key features

- Java-only backend runtime
- aggregated DTO API for summary, activity, resource, and histogram analytics
- frontend tables and charts rendered from backend DTOs
- backend and frontend automated tests for analytics flows
- archived legacy materials separated from the active product

## Notes

- `simulation-dashboard/backend-java` currently launches `letitsim-main` as a separate Java process
- do not remove `letitsim-main/target` unless you plan to rebuild the engine
- the backend stores runtime runs under `simulation-dashboard/backend-java/storage/runs`

## License

See `LICENSE`.
