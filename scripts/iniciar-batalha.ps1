#Requires -Version 5.1
<#
.SYNOPSIS
    Compila o Sentinel e abre o Robocode ja com a batalha em andamento.

.DESCRIPTION
    Usa o Robocode da versao do pom.xml. Se o SDK nao estiver instalado,
    baixa o instalador e instala em .robocode na raiz do repositorio.
    Cinco robos do pacote sample (os padrao da SDK) entram na batalha
    junto com bots.Sentinel. Interactive e Interactive_v2 ficam de fora
    porque esperam teclado e travam a luta. O paint ja comeca ligado
    pela propriedade -DPAINTING, que vale para todo robo que desenha.
    O arco do radar tambem ja aparece: o Robocode so desenha isso
    com robocode.options.view.scanArcs=true na configuracao da SDK.
#>
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$RepoRoot = Split-Path -Parent $PSScriptRoot
$SdkDir = Join-Path $RepoRoot '.robocode'

function Get-RobocodeVersion {
    $pomPath = Join-Path $RepoRoot 'pom.xml'
    $pom = Get-Content -Path $pomPath -Raw -Encoding UTF8
    if ($pom -match '<robocode\.version>([^<]+)</robocode\.version>') {
        return $Matches[1].Trim()
    }
    throw "Nao foi possivel ler robocode.version em $pomPath"
}

function Test-RobocodeSdk {
    param([string]$Path)
    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $false
    }
    $sample = Join-Path $Path 'robots\sample'
    $libs = Join-Path $Path 'libs'
    if (-not (Test-Path -LiteralPath $sample) -or -not (Test-Path -LiteralPath $libs)) {
        return $false
    }
    $hasClass = @(Get-ChildItem -LiteralPath $sample -Filter '*.class' -File -ErrorAction SilentlyContinue)
    $hasJar = @(Get-ChildItem -LiteralPath $libs -Filter '*.jar' -File -ErrorAction SilentlyContinue)
    return ($hasClass.Count -gt 0 -and $hasJar.Count -gt 0)
}

function Find-RobocodeSdk {
    $candidates = @(
        $env:ROBOCODE_HOME,
        $SdkDir,
        (Join-Path $env:SystemDrive 'robocode'),
        (Join-Path $env:SystemDrive 'Robocode'),
        (Join-Path $env:USERPROFILE 'robocode'),
        (Join-Path $env:USERPROFILE 'Robocode')
    )
    foreach ($candidate in $candidates) {
        if (Test-RobocodeSdk $candidate) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    return $null
}

function Install-RobocodeSdk {
    param(
        [string]$Version,
        [string]$Destination
    )

    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java nao foi encontrado no PATH.'
    }

    $setupJar = Join-Path $env:TEMP "robocode-$Version-setup.jar"
    $url = "https://github.com/robo-code/robocode/releases/download/v$Version/robocode-$Version-setup.jar"
    Write-Host "Baixando Robocode $Version..."
    Invoke-WebRequest -Uri $url -OutFile $setupJar -UseBasicParsing

    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Write-Host "Instalando em $Destination..."
    $log = & java -jar $setupJar $Destination silent 2>&1 | Out-String
    if ($log -notmatch 'Installation successful' -or -not (Test-RobocodeSdk $Destination)) {
        throw "A instalacao do Robocode falhou.`n$log"
    }
}

function Get-JavaMajorVersion {
    # java -fullversion escreve no stderr. Sem cmd, o PowerShell trata isso como erro.
    $raw = cmd /c "java -fullversion 2>&1"
    if ($raw -notmatch '"([^"]+)"') {
        throw "Nao foi possivel ler a versao do Java: $raw"
    }
    $version = ($Matches[1] -split '\+')[0]
    $parts = $version.Split('.')
    if ($parts[0] -eq '1') {
        return [int]$parts[1]
    }
    return [int]$parts[0]
}

$version = Get-RobocodeVersion
$sdk = Find-RobocodeSdk
if (-not $sdk) {
    Install-RobocodeSdk -Version $version -Destination $SdkDir
    $sdk = (Resolve-Path -LiteralPath $SdkDir).Path
}
Write-Host "Robocode: $sdk"

Write-Host 'Compilando Sentinel...'
Push-Location $RepoRoot
try {
    & (Join-Path $RepoRoot 'mvnw.cmd') -q -DskipTests compile
    if ($LASTEXITCODE -ne 0) {
        throw "A compilacao do Sentinel falhou (codigo $LASTEXITCODE)."
    }
}
finally {
    Pop-Location
}

$classesDir = Join-Path $RepoRoot 'target\classes\bots'
$classFile = Join-Path $classesDir 'Sentinel.class'
if (-not (Test-Path -LiteralPath $classFile)) {
    throw "Classe compilada nao encontrada: $classFile"
}

$robotsRoot = Join-Path $sdk 'robots'
$robotDir = Join-Path $robotsRoot 'bots'
if (Test-Path -LiteralPath $robotDir) {
    Remove-Item -LiteralPath $robotDir -Recurse -Force
}
Copy-Item -LiteralPath $classesDir -Destination $robotsRoot -Recurse -Force
@"
#Robot Properties
robot.classname=bots.Sentinel
robot.name=Sentinel
robot.description=Goal-driven scan, aim, and strafe. Squares follow identified enemies.
robocode.version=$version
"@ | Set-Content -LiteralPath (Join-Path $robotDir 'Sentinel.properties') -Encoding ASCII

$available = @(
    Get-ChildItem -LiteralPath (Join-Path $sdk 'robots\sample') -Filter '*.class' -File |
        Where-Object { $_.BaseName -notmatch '\$' -and $_.BaseName -notlike 'Interactive*' } |
        ForEach-Object { "sample.$($_.BaseName)" } |
        Sort-Object -Unique
)
if ($available.Count -lt 5) {
    throw "A SDK tem $($available.Count) robos padrao em robots\sample; sao necessarios 5."
}

$opponents = @($available | Get-Random -Count 5)
# O Robocode marca com asterisco todo robo da pasta robots que nao
# esteja no pacote sample. Sem esse sufixo, bots.Sentinel nao e encontrado.
$selected = @('bots.Sentinel*') + $opponents
Write-Host ("Batalha: " + ($selected -join ', '))

$battleDir = Join-Path $sdk 'battles'
New-Item -ItemType Directory -Force -Path $battleDir | Out-Null
$battleFile = Join-Path $battleDir 'sentinel.battle'
@"
#Battle Properties
robocode.battleField.width=800
robocode.battleField.height=600
robocode.battle.numRounds=10
robocode.battle.gunCoolingRate=0.1
robocode.battle.rules.inactivityTime=450
robocode.battle.hideEnemyNames=false
robocode.battle.sentryBorderSize=100
robocode.battle.selectedRobots=$($selected -join ',')
"@ | Set-Content -LiteralPath $battleFile -Encoding ASCII

$javaMajor = Get-JavaMajorVersion
if ($javaMajor -gt 11 -and $javaMajor -lt 24) {
    if ([string]::IsNullOrWhiteSpace($env:_JAVA_OPTIONS)) {
        $env:_JAVA_OPTIONS = '-Djava.security.manager=allow'
    }
    elseif ($env:_JAVA_OPTIONS -notmatch 'java\.security\.manager=allow') {
        $env:_JAVA_OPTIONS = "$env:_JAVA_OPTIONS -Djava.security.manager=allow"
    }
}

# O arco do radar e uma opcao da interface, nao da batalha.
# Fica em config/robocode.properties, lido a partir da pasta da SDK.
$configDir = Join-Path $sdk 'config'
New-Item -ItemType Directory -Force -Path $configDir | Out-Null
$configFile = Join-Path $configDir 'robocode.properties'
$scanArcs = 'robocode.options.view.scanArcs=true'
$latin1 = [System.Text.Encoding]::GetEncoding('iso-8859-1')
if (Test-Path -LiteralPath $configFile) {
    $configText = [System.IO.File]::ReadAllText($configFile, $latin1)
    if ($configText -match '(?m)^robocode\.options\.view\.scanArcs\s*=') {
        $configText = [regex]::Replace(
            $configText,
            '(?m)^robocode\.options\.view\.scanArcs\s*=.*$',
            $scanArcs)
    }
    else {
        if ($configText.Length -gt 0 -and -not $configText.EndsWith("`n")) {
            $configText += "`r`n"
        }
        $configText += "$scanArcs`r`n"
    }
}
else {
    $configText = "$scanArcs`r`n"
}
[System.IO.File]::WriteAllText($configFile, $configText, $latin1)

Write-Host 'Abrindo a batalha...'
Push-Location $sdk
try {
    & java `
        '-cp' 'libs/*' `
        '-Xmx512M' `
        '-XX:+IgnoreUnrecognizedVMOptions' `
        '-DPAINTING=true' `
        '--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED' `
        '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED' `
        '--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED' `
        '--add-opens=java.desktop/sun.awt=ALL-UNNAMED' `
        'robocode.Robocode' `
        '-battle' $battleFile
    if ($LASTEXITCODE -ne 0) {
        throw "O Robocode encerrou com codigo $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
