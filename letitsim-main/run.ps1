$ErrorActionPreference = 'Stop'

param(
    [string]$BpmnPath = ".\credit_card_application.bpmn",
    [string]$LogPath = ".\log.txt",
    [string]$JsonPath = ".\parsed-log-dto.json"
)

function Resolve-JavaExe {
    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += (Join-Path $env:JAVA_HOME 'bin\java.exe')
    }

    $candidates += @(
        'C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot\bin\java.exe',
        'C:\Program Files\Eclipse Adoptium\jdk-8*\bin\java.exe',
        'C:\Program Files\Java\jdk1.8*\bin\java.exe',
        'C:\Program Files\Java\jdk-8*\bin\java.exe'
    )

    foreach ($candidate in $candidates) {
        $resolved = Get-ChildItem -Path $candidate -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved) {
            return $resolved.FullName
        }
    }

    $pathJava = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($pathJava) {
        return $pathJava.Source
    }

    throw "Java не найдена. Установите JDK 8 или задайте JAVA_HOME."
}

function Assert-FileExists([string]$PathToCheck, [string]$Hint) {
    if (-not (Test-Path -LiteralPath $PathToCheck)) {
        throw "$Hint`nНе найдено: $PathToCheck"
    }
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaExe = Resolve-JavaExe
$classesDir = Join-Path $projectRoot 'target\classes'

Assert-FileExists $classesDir "Скомпилированные классы отсутствуют. Сначала соберите проект."
Assert-FileExists $BpmnPath "BPMN-файл не найден."

$jarPaths = @(
    (Join-Path $projectRoot 'lib\jdom2-2.0.6.jar'),
    (Join-Path $projectRoot 'lib\jaxb-api-2.3.1.jar'),
    (Join-Path $projectRoot 'lib\colt-1.2.0.jar'),
    (Join-Path $projectRoot 'lib\concurrent-1.3.4.jar')
)

if ($jarPaths | Where-Object { -not (Test-Path -LiteralPath $_) }) {
    $jarPaths = @(
        (Join-Path $env:USERPROFILE '.m2\repository\org\jdom\jdom2\2.0.6\jdom2-2.0.6.jar'),
        (Join-Path $env:USERPROFILE '.m2\repository\javax\xml\bind\jaxb-api\2.3.1\jaxb-api-2.3.1.jar'),
        (Join-Path $env:USERPROFILE '.m2\repository\colt\colt\1.2.0\colt-1.2.0.jar'),
        (Join-Path $env:USERPROFILE '.m2\repository\concurrent\concurrent\1.3.4\concurrent-1.3.4.jar')
    )
}

foreach ($jarPath in $jarPaths) {
    Assert-FileExists $jarPath "Не хватает зависимостей: положите jar в .\lib или локальный репозиторий .m2."
}

$classpath = @($classesDir) + $jarPaths -join ';'

Write-Host "Java:      $javaExe"
Write-Host "BPMN:      $(Resolve-Path $BpmnPath)"
Write-Host "Log:       $LogPath"
Write-Host "JSON:      $JsonPath"
Write-Host ""

& $javaExe -cp $classpath Main $BpmnPath $LogPath $JsonPath
