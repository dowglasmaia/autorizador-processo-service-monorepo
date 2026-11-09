@echo off
rem Sobe tudo com um comando (usa bypass so neste processo; nao altera a politica do sistema).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0subir.ps1" %*
