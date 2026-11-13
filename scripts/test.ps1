$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $repoRoot 'build/test-classes'
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
$mainSources = @(Get-ChildItem -Path (Join-Path $repoRoot 'src/main/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
$testSources = @(Get-ChildItem -Path (Join-Path $repoRoot 'src/test/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
& javac --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d $outputDir ($mainSources + $testSources)
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java --add-modules jdk.httpserver -cp $outputDir com.eventstudio.application.EventCatalogTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java --add-modules jdk.httpserver -cp $outputDir com.eventstudio.application.EventPlanningFacadeTest
exit $LASTEXITCODE
