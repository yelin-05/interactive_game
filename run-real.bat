@echo off
rem =====================================================================
rem  Launch profile: real LLM channel (H2 in-memory DB, zero config)
rem  This file is ASCII-only on purpose: cmd parses UTF-8 batch files with
rem  the console code page (GBK here), and Chinese echo lines can turn into
rem  stray separators like & or ] -> "']' is not recognized".
rem  Chinese docs: see README.md section "real LLM in 3 steps"
rem =====================================================================
cd /d "%~dp0"
echo ==========================================================
echo   MoShi - real LLM profile (H2 in-memory DB, zero config)
echo ==========================================================
echo.
echo   Get an API key : https://platform.deepseek.com/api_keys
echo   Other vendor   : set LLM_BASE_URL / LLM_MODEL first, then rerun
echo   Free option    : zhipu glm-4-flash
echo     set LLM_BASE_URL=https://open.bigmodel.cn/api/paas/v4/chat/completions
echo     set LLM_MODEL=glm-4-flash
echo.

if "%LLM_API_KEY%"=="" set /p LLM_API_KEY=Paste your API key and press Enter (sk-...): 

if "%LLM_API_KEY%"=="" (
  echo.
  echo [ERROR] No key. Without a key every generation falls back to mock questions.
  pause
  exit /b 1
)

set LLM_MODE=real
if "%LLM_MODEL%"=="" set LLM_MODEL=deepseek-chat
if "%LLM_BASE_URL%"=="" set LLM_BASE_URL=https://api.deepseek.com/v1/chat/completions
if "%PROFILE%"=="" set PROFILE=h2
echo.
echo [INFO] LLM_MODE=real
echo [INFO] LLM_MODEL=%LLM_MODEL%
echo [INFO] LLM_BASE_URL=%LLM_BASE_URL%
echo [INFO] SPRING_PROFILE=%PROFILE%   (set PROFILE=mysql to use your MySQL DB)
echo [INFO] Open http://localhost:8080/llm - top right must show "real"
echo [INFO] Health check: http://localhost:8080/api/llm/status
echo [INFO] If port 8080 is busy, close the running instance first (taskkill /F /PID ^<pid^>)
echo.

call mvnw.cmd -B spring-boot:run -Dspring-boot.run.profiles=%PROFILE%
echo.
echo [INFO] app exited, code=%ERRORLEVEL%
pause

