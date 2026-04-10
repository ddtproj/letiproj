$ErrorActionPreference = 'Stop'

New-Item -ItemType Directory -Force -Path lib | Out-Null

$deps = @(
    @{ Url = 'https://repo1.maven.org/maven2/org/jdom/jdom2/2.0.6/jdom2-2.0.6.jar'; OutFile = 'lib/jdom2-2.0.6.jar' },
    @{ Url = 'https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar'; OutFile = 'lib/jaxb-api-2.3.1.jar' },
    @{ Url = 'https://repo1.maven.org/maven2/colt/colt/1.2.0/colt-1.2.0.jar'; OutFile = 'lib/colt-1.2.0.jar' },
    @{ Url = 'https://repo1.maven.org/maven2/concurrent/concurrent/1.3.4/concurrent-1.3.4.jar'; OutFile = 'lib/concurrent-1.3.4.jar' }
)

foreach ($dep in $deps) {
    Invoke-WebRequest -Uri $dep.Url -OutFile $dep.OutFile
}
