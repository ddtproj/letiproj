# Architecture

## Flow

1. Client triggers simulation run.
2. Backend creates `run_id`.
3. Backend starts Java simulation engine.
4. Backend stores input, stdout, stderr, log, and metadata.
5. Backend parses log and computes metrics.
6. Backend exposes JSON DTO.
7. Frontend fetches DTO and renders results.

## Main Components

- `simulation_runner`: starts external engine and stores artifacts
- `log_parser`: transforms raw log into normalized events
- `metrics_calculator`: computes summary and chart data
- `dto_builder`: maps domain metrics into frontend DTO
- `api`: serves `run`, `status`, and `result`
