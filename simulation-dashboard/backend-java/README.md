# Java Backend

Spring Boot backend, который полностью закрывает API dashboard на Java.
Он хранит свои запуски в `backend-java/storage/runs`, умеет создавать `run`,
запускать Java-движок из `letitsim-main`, читать логи и отдавать DTO для frontend.

## Что уже перенесено

- `GET /health`
- `GET /api/simulations`
- `POST /api/simulations/run`
- `GET /api/simulations/{runId}/status`
- `GET /api/simulations/{runId}/result`
- CORS для React frontend
- parser логов
- расчёт метрик
- чтение `meta.json` и логов из storage
- запуск симуляции через `letitsim-main`
- собственный storage в `backend-java/storage/runs`

## Рекомендуемое окружение

- Java 17
- Maven 3.9+

## Локальный Maven repository

Проект настроен так, чтобы Maven по умолчанию использовал локальный repository
внутри `backend-java/.mvn/repository`, а не глобальный `%USERPROFILE%\\.m2`.
Это помогает избежать проблем с правами доступа в окружениях, где глобальный `.m2`
недоступен или read-only.

## Как запускать

```bash
cd backend-java
mvn spring-boot:run
```

Для тестов и сборки отдельно:

```bash
cd backend-java
mvn test
mvn package
```

По умолчанию backend стартует на `http://127.0.0.1:8000`.

## Что нужно для реального запуска симуляции

- Java, доступная как `java` или через `JAVA_HOME`
- скомпилированный `letitsim-main/target/classes`
- зависимости `letitsim-main/lib/*.jar` или соответствующие jar в локальном `.m2`

Если `POST /api/simulations/run` получает валидный `specification_path`,
backend создаёт папку запуска, переводит статус в `running`, запускает `Main`
из `letitsim-main` и после завершения сохраняет:

- `simulation.log`
- `parsed-log-dto.json`
- `stdout.log`
- `stderr.log`
- `meta.json`

## Maven recovery on Windows

If `mvn test` or `mvn package` fails with `AccessDeniedException` for a jar inside
`backend-java/.mvn/repository`, the local cache is usually locked or corrupted.

Recommended recovery steps:

```cmd
cd /d c:\Users\Admin\Desktop\bimp-ui-master\simulation-dashboard\backend-java
rmdir /s /q .mvn\repository\com\fasterxml\jackson\module\jackson-module-parameter-names\2.17.2
mvn test
```

If the problem affects multiple dependencies, remove the whole local project cache
and let Maven download it again:

```cmd
cd /d c:\Users\Admin\Desktop\bimp-ui-master\simulation-dashboard\backend-java
rmdir /s /q .mvn\repository
mvn test
```
