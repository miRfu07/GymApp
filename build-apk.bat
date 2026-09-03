@echo off
echo ========================================
echo Gym Tracker - Сборка APK
echo ========================================
echo.

REM Проверка наличия Android SDK
if not defined ANDROID_HOME (
    if not defined ANDROID_SDK_ROOT (
        echo [ОШИБКА] Android SDK не найден!
        echo.
        echo Пожалуйста, установите Android Studio:
        echo https://developer.android.com/studio
        echo.
        echo Или установите переменную окружения ANDROID_HOME
        echo Например: set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
        echo.
        pause
        exit /b 1
    )
    set ANDROID_HOME=%ANDROID_SDK_ROOT%
)

echo Android SDK найден: %ANDROID_HOME%
echo.

REM Создание local.properties
echo sdk.dir=%ANDROID_HOME:\=\\% > local.properties
echo Создан файл local.properties
echo.

REM Проверка наличия gradlew
if not exist gradlew.bat (
    echo [ОШИБКА] gradlew.bat не найден!
    echo Запустите Android Studio и откройте проект для инициализации Gradle Wrapper
    pause
    exit /b 1
)

echo Начинаем сборку APK...
echo.

REM Сборка debug APK
call gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Сборка успешно завершена!
    echo ========================================
    echo.
    echo APK файл находится в:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Скопируйте этот файл на Android устройство и установите.
    echo.
) else (
    echo.
    echo ========================================
    echo Ошибка при сборке!
    echo ========================================
    echo.
    echo Проверьте логи выше для деталей.
    echo.
)

pause
