@echo off
setlocal
REM The bootstrap derives its paths from the location of vanted-boot.jar.
cd /d "%~dp0"
echo Checking System

REM Bundled runtime if present, otherwise the system Java.
if exist "%~dp0runtime\bin\java.exe" (
	set "JAVA_BIN=%~dp0runtime\bin\java.exe"
	echo using bundled runtime
) else (
	set "JAVA_BIN=java"
	echo using system java
)

REM Physical memory via PowerShell; WMIC is gone since Windows 11 24H2.
set PHYSMEM=
for /f "delims=" %%A in ('powershell -NoProfile -Command "(Get-CimInstance Win32_OperatingSystem).TotalVisibleMemorySize" 2^>nul') do set PHYSMEM=%%A

REM set /a without %% yields 0 for an empty value instead of a syntax error.
set /a PHYSMEM=PHYSMEM/1024
set /a VANTEDMEM=PHYSMEM/3
echo physical memory %PHYSMEM% MiB

if %VANTEDMEM% LEQ 0 (
	echo could not determine physical memory, using default
	set VANTEDMEM=1024
)

REM 32 or 64 bit JVM
set SYS32=%WINDIR%\system32
set VERFILE=%TEMP%\vanted-java-version.txt
"%JAVA_BIN%" -version 2>"%VERFILE%"
type "%VERFILE%"
for /f %%A in ('%SYS32%\findstr.exe /n -i "64-bit" "%VERFILE%"^|%SYS32%\find.exe /c ":"') do set IS64BIT=%%A
del "%VERFILE%"

if "%IS64BIT%"=="0" (
	set ARCH=x86
) else (
	set ARCH=x64
)
echo Java RT architecture is %ARCH%

REM adjust memory usage for 32 and 64 bit systems
REM on 64 bit don't ever use more than 8GiBi
if %VANTEDMEM% gtr 1000 (
	if "%ARCH%"=="x86" (
		set VANTEDMEM=1024
	) else (
		if %VANTEDMEM% gtr 8000 (
			set VANTEDMEM=8000
		)
	)
)
echo Vanted memory %VANTEDMEM%
"%JAVA_BIN%" -Dfile.encoding=UTF-8 -Xmx%VANTEDMEM%m -jar vanted-boot.jar
