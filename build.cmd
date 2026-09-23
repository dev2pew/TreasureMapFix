@echo off
setlocal
call gradlew.bat clean build
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
    echo.
    echo Build failed with exit code %EXIT_CODE%.
    exit /b %EXIT_CODE%
)

echo.
echo Build completed. Check build\libs\
endlocal
