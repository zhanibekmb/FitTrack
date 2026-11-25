param(
    [Parameter(Mandatory = $true)][string]$Adb,
    [string]$Serial = 'emulator-5554'
)
$ErrorActionPreference = 'Stop'
$package = 'com.example.fittrack'
$runner = "$package.test/androidx.test.runner.AndroidJUnitRunner"
$probeName = 'Restart check ' + [guid]::NewGuid().ToString('N')

function Invoke-Probe([string]$Phase) {
    $result = & $Adb -s $Serial shell am instrument -w -r -e class com.example.fittrack.WorkoutProcessRestartTest -e persistencePhase $Phase -e persistenceName "'$probeName'" $runner
    $result | Write-Output
    if ($LASTEXITCODE -ne 0 -or ($result -join "`n") -notmatch 'OK \(1 test\)') {
        throw "Persistence $Phase phase failed"
    }
}

Invoke-Probe 'prepare'
& $Adb -s $Serial shell am force-stop $package
if ($LASTEXITCODE -ne 0) { throw 'Could not stop the app process' }
Invoke-Probe 'verify'
Write-Output 'PASS: the plan and completed sets survived an app process restart.'
