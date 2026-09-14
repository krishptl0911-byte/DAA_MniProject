@echo off
setlocal
if not exist out mkdir out
javac -d out src\*.java
if errorlevel 1 (
  echo Compilation failed. Please check the Java installation.
  pause
  exit /b 1
)
java -cp out Main
