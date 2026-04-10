# letitsim

Java-движок симуляции BPMN-процессов, который используется основным backend из `simulation-dashboard/backend-java`.

## Что делает проект

Движок:

- загружает BPMN-модель
- выполняет симуляцию
- пишет текстовый лог
- строит структурированный JSON по результатам лога

В текущей архитектуре этот проект обычно не используется напрямую пользователем, а запускается из Java backend.

## Что нужно для работы

- Java 8 JDK
- собранные классы в `target/classes`
- jar-зависимости либо в `lib/`, либо в локальном `.m2`

## Основные выходные файлы

При прямом запуске `Main` можно получить:

- `log.txt`
- `parsed-log-dto.json`

Когда проект запускается через `backend-java`, пути к этим файлам передаются backend-ом в папку конкретного run.

## Сборка

Пример для PowerShell:

```powershell
$cp = @(
  "$env:USERPROFILE\.m2\repository\org\jdom\jdom2\2.0.6\jdom2-2.0.6.jar",
  "$env:USERPROFILE\.m2\repository\javax\xml\bind\jaxb-api\2.3.1\jaxb-api-2.3.1.jar",
  "$env:USERPROFILE\.m2\repository\colt\colt\1.2.0\colt-1.2.0.jar",
  "$env:USERPROFILE\.m2\repository\concurrent\concurrent\1.3.4\concurrent-1.3.4.jar"
) -join ';'

Remove-Item -Recurse -Force .\target\classes -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path .\target\classes | Out-Null
$sources = Get-ChildItem -Recurse .\src\main\java\*.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp $cp -d .\target\classes $sources
```

## Быстрый прямой запуск

Если `target/classes` уже существует:

```cmd
run.cmd
```

Или с аргументами:

```cmd
run.cmd credit_card_application.bpmn my-log.txt my-result.json
```

## Как это связано с backend-java

`simulation-dashboard/backend-java` запускает:

- класс `Main`
- с аргументами:
  - путь к BPMN
  - путь к выходному логу
  - путь к выходному JSON

Поэтому для рабочего runtime важно:

- не удалять `target/classes`, пока движок не собран заново
- не удалять нужные jar-зависимости из `lib/`

## Основные файлы

- `src/main/java/Main.java` — вход в движок
- `src/main/java/logparser` — парсер лога
- `src/main/java/logger` — логирование
- `credit_card_application.bpmn` — пример BPMN-модели
- `jsoninfo.txt` — описание структуры JSON
