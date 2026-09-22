# Hall Wedding Build Script
$ErrorActionPreference = "Stop"

$ProjectRoot = "C:\Users\HP\Desktop\Hall_Wedding"
$JavaHome = "C:\Program Files\Java\jdk-25"
$JavaC = "$JavaHome\bin\javac.exe"
$Jar = "$JavaHome\bin\jar.exe"
$BuildDir = "$ProjectRoot\build"
$ClassesDir = "$BuildDir\classes"
$SrcDir = "$ProjectRoot\src"

# Classpath
$CP = @(
    "$ProjectRoot\controlsfx-11.2.1.jar",
    "C:\Users\HP\Downloads\eclipselink-4.0.2 (3).jar",
    "C:\Users\HP\Downloads\mysql-connector-java-8.0.17.jar",
    "C:\Program Files\Java\javafx-sdk-25.0.1\lib\javafx.base.jar",
    "C:\Program Files\Java\javafx-sdk-25.0.1\lib\javafx.controls.jar",
    "C:\Program Files\Java\javafx-sdk-25.0.1\lib\javafx.graphics.jar",
    "C:\Program Files\Java\javafx-sdk-25.0.1\lib\javafx.fxml.jar"
) -join ";"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Hall Wedding Build" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# Clean
Write-Host "`nCleaning..." -ForegroundColor Yellow
if (Test-Path $BuildDir) { Remove-Item -Path $BuildDir -Recurse -Force }

# Create directories
Write-Host "Creating directories..." -ForegroundColor Yellow
New-Item -Type Directory -Path "$ClassesDir\META-INF" -Force | Out-Null
New-Item -Type Directory -Path "$BuildDir\generated-sources\ap-source-output" -Force | Out-Null

# Copy resources
Write-Host "Copying resources..." -ForegroundColor Yellow
Copy-Item "$SrcDir\META-INF\persistence.xml" "$ClassesDir\META-INF\" -Force

# Get all Java files
$JavaFiles = @()
$JavaFiles += (Get-ChildItem -Path "$SrcDir\views" -Filter "*.java" | ForEach-Object { $_.FullName })
$JavaFiles += (Get-ChildItem -Path "$SrcDir\controllers" -Filter "*.java" | ForEach-Object { $_.FullName })
$JavaFiles += (Get-ChildItem -Path "$SrcDir\hall_wedding" -Filter "*.java" | ForEach-Object { $_.FullName })

Write-Host "Found $($JavaFiles.Count) Java files to compile" -ForegroundColor Cyan

# Compile
Write-Host "`nCompiling..." -ForegroundColor Yellow
$CompileArgs = @(
    "-cp", "$CP",
    "-d", "$ClassesDir",
    "-sourcepath", "$SrcDir",
    "-source", "24",
    "-target", "24",
    "-implicit:class",
    "-encoding", "UTF-8"
)

foreach ($file in $JavaFiles) {
    $CompileArgs += $file
}

Write-Host "javac $($CompileArgs.Count) arguments..." -ForegroundColor Gray
& $JavaC @CompileArgs 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Compilation successful!" -ForegroundColor Green
    
    # Create JAR
    Write-Host "`nCreating JAR..." -ForegroundColor Yellow
    $JarName = "$ProjectRoot\dist\Hall_Wedding.jar"
    New-Item -Type Directory -Path "$ProjectRoot\dist" -Force | Out-Null
    & $Jar -cvf "$JarName" -C "$ClassesDir" . 2>&1 | Out-Null
    Write-Host "✅ JAR created: $JarName" -ForegroundColor Green
} else {
    Write-Host "`n❌ Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Build Complete" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
