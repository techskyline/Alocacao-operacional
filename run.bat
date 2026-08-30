@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
  echo [ERRO] Java JDK nao encontrado no PATH.
  echo Instale o JDK 21 e reabra o terminal.
  pause
  exit /b 1
)

if not exist out mkdir out

echo Compilando ALOC.OP Java Web...
javac -encoding UTF-8 -d out src\br\com\alocacao\*.java
if errorlevel 1 (
  echo.
  echo [ERRO] A compilacao falhou. Veja as mensagens acima.
  pause
  exit /b 1
)

echo.
echo Aplicacao iniciada em http://localhost:8080
start "" http://localhost:8080
java -cp out br.com.alocacao.Main
endlocal
