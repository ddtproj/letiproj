# API Contract

## Endpoints

### `POST /api/simulations/run`

Starts a simulation run.

Response:

```json
{
  "runId": "sim_001",
  "status": "queued"
}
```

### `GET /api/simulations/{runId}/status`

Returns current run status.

### `GET /api/simulations/{runId}/result`

Returns computed simulation result DTO.
