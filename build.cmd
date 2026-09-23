@echo off
setlocal
set "JAVA_HOME=C:\Users\lucky\files\programs\x64\jdk\amazon\corretto\21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

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
