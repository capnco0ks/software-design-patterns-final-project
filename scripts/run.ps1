$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $repoRoot 'build/classes'
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
$sources = @(Get-ChildItem -Path (Join-Path $repoRoot 'src/main/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
if ($sources.Count -eq 0) { throw 'Java source files not found' }
& javac --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d $outputDir $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java --add-modules jdk.httpserver -cp $outputDir com.eventstudio.App
exit $LASTEXITCODE
