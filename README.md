# BPMN Simulation Workspace

Репозиторий теперь состоит из трех рабочих частей:

- `simulation-dashboard/frontend` — React/Vite интерфейс для запуска симуляции и просмотра результатов
- `simulation-dashboard/backend-java` — основной Java backend на Spring Boot
- `letitsim-main` — Java-движок симуляции, который backend запускает для выполнения BPMN-модели

Исторические и справочные материалы вынесены в `archive/`.

## Актуальный поток работы

1. frontend отправляет запрос в Java backend
2. Java backend создает `runId`
3. backend запускает `letitsim-main`
4. движок пишет лог и JSON-артефакты
5. backend читает лог, считает метрики и отдает DTO фронтенду

## Основные рабочие папки

- `simulation-dashboard/frontend`
- `simulation-dashboard/backend-java`
- `simulation-dashboard/docs`
- `simulation-dashboard/shared`
- `letitsim-main`

## Архив

- `archive/legacy-bimp-ui` — старый TypeScript UI `bimp-ui`
- `archive/reference/BPSimpyLibrary-main` — сторонняя Python-библиотека BPSim
- `archive/tools/extract_docx_text.py` — вспомогательный утилитарный скрипт

## Быстрый старт

### 1. Подготовить движок

Для реального запуска симуляции `backend-java` ожидает, что у `letitsim-main` уже есть собранные классы в `letitsim-main/target/classes`.

См. инструкции в [letitsim-main/README.md](/c:/Users/Admin/Desktop/bimp-ui-master/letitsim-main/README.md).

### 2. Запустить Java backend

Нужны:

- Java 17+
- Maven 3.9+

Команды:

```bash
cd simulation-dashboard/backend-java
mvn spring-boot:run
```

Backend по умолчанию поднимается на `http://127.0.0.1:8000`.

### 3. Запустить frontend

Команды:

```bash
cd simulation-dashboard/frontend
npm install
npm run dev
```

Frontend по умолчанию ожидает backend на `http://127.0.0.1:8000`.

## Где что настраивается

- backend storage: [simulation-dashboard/backend-java/src/main/resources/application.properties](/c:/Users/Admin/Desktop/bimp-ui-master/simulation-dashboard/backend-java/src/main/resources/application.properties)
- frontend API base URL: `simulation-dashboard/frontend/src/services/api.ts`
- движок симуляции: [letitsim-main](/c:/Users/Admin/Desktop/bimp-ui-master/letitsim-main)
- архив старого UI: [archive/legacy-bimp-ui](/c:/Users/Admin/Desktop/bimp-ui-master/archive/legacy-bimp-ui)
- архив reference-материалов: [archive/reference](/c:/Users/Admin/Desktop/bimp-ui-master/archive/reference)
- архив утилит: [archive/tools](/c:/Users/Admin/Desktop/bimp-ui-master/archive/tools)

## Примечание

`simulation-dashboard/backend-java` сейчас запускает `letitsim-main` как отдельный Java-процесс. Поэтому удалять `letitsim-main/target` нельзя, пока не выполнена новая сборка движка.
