@echo off
cd /d %~dp0
echo "Create XML plugin file lists..."
echo Vanted Plugins...
dir src\main\java\*.xml /s /b > src\main\resources\plugins.txt 2> nul
echo "READY"
