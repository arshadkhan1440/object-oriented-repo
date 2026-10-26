$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$classes = Join-Path $projectRoot 'target\classes'
$connector = Join-Path $env:USERPROFILE '.m2\repository\com\mysql\mysql-connector-j\9.4.0\mysql-connector-j-9.4.0.jar'
$sources = Get-ChildItem (Join-Path $projectRoot 'src\main\java') -Filter '*.java' -Recurse |
    ForEach-Object { $_.FullName }

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'Java JDK 17 or newer is required. Install a JDK, then run this launcher again.'
}

New-Item -ItemType Directory -Force $classes | Out-Null
if (-not (Test-Path -LiteralPath $connector)) {
    throw "MySQL Connector/J was not found at $connector. Run 'mvn compile' once to download it."
}

$dbUrl = Read-Host 'MySQL database URL [jdbc:mysql://localhost:3306/food_ordering]'
if ([string]::IsNullOrWhiteSpace($dbUrl)) { $dbUrl = 'jdbc:mysql://localhost:3306/food_ordering' }
$dbUser = Read-Host 'MySQL username [root]'
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }
$securePassword = Read-Host 'MySQL password' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$env:FOOD_DB_URL = $dbUrl.Trim()
$env:FOOD_DB_USER = $dbUser.Trim()
$env:FOOD_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)

if ([string]::IsNullOrWhiteSpace($env:FOOD_DB_USER)) {
    throw 'MySQL username cannot be blank. Run Start FoodOrder.bat again and enter your MySQL username.'
}
Push-Location $projectRoot
try {
    Write-Host 'Compiling FoodOrder...'
    & javac --release 17 -encoding UTF-8 -d $classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. Review the Java errors above.' }

    Write-Host ''
    Write-Host 'Starting FoodOrder...'
    Write-Host "Connecting to $env:FOOD_DB_URL as $env:FOOD_DB_USER"
    & java -cp "$classes;$connector" com.foodordering.Main
    if ($LASTEXITCODE -ne 0) { throw "FoodOrder exited with code $LASTEXITCODE." }
}
finally {
    Pop-Location
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    Remove-Item Env:FOOD_DB_URL -ErrorAction SilentlyContinue
    Remove-Item Env:FOOD_DB_USER -ErrorAction SilentlyContinue
    Remove-Item Env:FOOD_DB_PASSWORD -ErrorAction SilentlyContinue
}
