param(
    [ValidateSet('ejecutar', 'probar', 'compilar')]
    [string]$Accion = 'ejecutar',
    [string]$Jdk = '',
    [string]$ArchivoDatos = 'datos/accesos.dat'
)

$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath $PSScriptRoot
try {
    if ($Jdk) {
        $binJava = Join-Path $Jdk 'bin'
    } elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) {
        $binJava = Join-Path $env:JAVA_HOME 'bin'
    } else {
        $compilador = Get-Command javac -ErrorAction SilentlyContinue
        if (-not $compilador) {
            throw 'No se encontro el JDK. Use -Jdk con su carpeta de instalacion, o configure JAVA_HOME.'
        }
        $binJava = Split-Path -Parent $compilador.Source
    }
    $javac = Join-Path $binJava 'javac.exe'
    $java = Join-Path $binJava 'java.exe'
    if (-not (Test-Path -LiteralPath $javac) -or -not (Test-Path -LiteralPath $java)) {
        throw 'La carpeta indicada debe contener bin/javac.exe y bin/java.exe.'
    }
    New-Item -ItemType Directory -Path 'build/clases' -Force | Out-Null
    $fuentes = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | Sort-Object FullName | ForEach-Object {
        '"' + $_.FullName.Replace('\', '/') + '"'
    })
    [System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build/fuentes.txt'), $fuentes,
        (New-Object System.Text.UTF8Encoding($false)))
    & $javac -encoding UTF-8 -Xlint:all -Werror -d build/clases '@build/fuentes.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion del programa.' }
    if ($Accion -eq 'probar') {
        New-Item -ItemType Directory -Path 'build/test' -Force | Out-Null
        $pruebas = @(Get-ChildItem -LiteralPath 'test' -Recurse -Filter '*.java' | Sort-Object FullName | ForEach-Object {
            '"' + $_.FullName.Replace('\', '/') + '"'
        })
        [System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build/fuentes-test.txt'), $pruebas,
            (New-Object System.Text.UTF8Encoding($false)))
        & $javac -encoding UTF-8 -Xlint:all -Werror -cp build/clases -d build/test '@build/fuentes-test.txt'
        if ($LASTEXITCODE -ne 0) { throw 'Fallo la compilacion de las pruebas.' }
        & $java -cp 'build/clases;build/test' sigma.pruebas.PruebasAccesos
        if ($LASTEXITCODE -ne 0) { throw 'Hay pruebas fallidas.' }
        & $java -cp 'build/clases;build/test' sigma.pruebas.PruebasConsola
        if ($LASTEXITCODE -ne 0) { throw 'Hay pruebas funcionales fallidas.' }
    } elseif ($Accion -eq 'ejecutar') {
        & $java -cp build/clases sigma.Main $ArchivoDatos
        if ($LASTEXITCODE -ne 0) { throw 'El programa no pudo completar la ejecucion.' }
    } else {
        Write-Output 'Compilacion correcta.'
    }
} finally {
    Pop-Location
}
