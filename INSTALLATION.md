# Инструкция по сборке и установке APK

## ⚠️ Важно
Для сборки APK требуется Android SDK. Если у вас не установлен Android Studio, используйте один из альтернативных методов ниже.

## Метод 1: Android Studio (Рекомендуется)

### Шаг 1: Установка Android Studio
1. Скачайте Android Studio: https://developer.android.com/studio
2. Установите и запустите Android Studio
3. При первом запуске установятся необходимые компоненты SDK

### Шаг 2: Открытие проекта
1. Откройте Android Studio
2. Выберите "Open" и укажите папку `GymApp`
3. Дождитесь синхронизации Gradle (может занять несколько минут)

### Шаг 3: Сборка APK
1. В меню выберите: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Дождитесь завершения сборки
3. Нажмите на уведомление "locate" или найдите APK в:
   ```
   GymApp\app\build\outputs\apk\debug\app-debug.apk
   ```

### Шаг 4: Установка на телефон
1. Скопируйте `app-debug.apk` на телефон
2. Откройте файл на телефоне
3. Разрешите установку из неизвестных источников (если требуется)
4. Установите приложение

---

## Метод 2: Командная строка (для опытных пользователей)

### Требования
- Установленный Android SDK
- Java JDK 17

### Команды
```bash
cd GymApp
gradlew assembleDebug
```

APK будет в: `app\build\outputs\apk\debug\app-debug.apk`

---

## Метод 3: Онлайн сборка (без установки Android Studio)

### Вариант A: AppGyver / Appetize.io
1. Загрузите проект на GitHub
2. Используйте онлайн-сервисы для сборки Android приложений

### Вариант B: GitHub Actions (автоматическая сборка)
1. Создайте репозиторий на GitHub
2. Загрузите папку GymApp
3. Создайте файл `.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
        working-directory: ./GymApp
        
      - name: Build with Gradle
        run: ./gradlew assembleDebug
        working-directory: ./GymApp
        
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: GymApp/app/build/outputs/apk/debug/app-debug.apk
```

4. После push APK будет доступен в разделе Actions → Artifacts

---

## Метод 4: Использование готового APK (если предоставлен)

Если вам предоставили готовый APK файл:

1. **Скопируйте APK на телефон** (через USB, email, облако и т.д.)
2. **Откройте файл** на телефоне
3. **Разрешите установку**:
   - Android 8+: Настройки → Безопасность → Установка из неизвестных источников
   - Android 10+: Разрешение запрашивается автоматически
4. **Нажмите "Установить"**

---

## Решение проблем

### Ошибка: "Android SDK not found"
- Установите Android Studio
- Или установите переменную окружения:
  ```
  ANDROID_HOME=C:\Users\ВашеИмя\AppData\Local\Android\Sdk
  ```

### Ошибка: "Gradle sync failed"
- Проверьте подключение к интернету
- Удалите папку `.gradle` и попробуйте снова
- Обновите Android Studio до последней версии

### Ошибка при установке на телефон
- Включите "Установка из неизвестных источников"
- Проверьте, что APK не поврежден
- Убедитесь, что версия Android 7.0 или выше

### Приложение не запускается
- Проверьте версию Android (минимум 7.0)
- Очистите кэш приложения
- Переустановите приложение

---

## Системные требования

### Для сборки:
- Windows 10/11, macOS, или Linux
- Android Studio Arctic Fox или новее
- Java JDK 17
- 8 GB RAM (рекомендуется)
- 10 GB свободного места

### Для установки на телефон:
- Android 7.0 (API 24) или выше
- 20 MB свободного места
- Разрешение на установку из неизвестных источников

---

## Контакты и поддержка

Если возникли проблемы:
1. Проверьте README.md в корне проекта
2. Убедитесь, что все файлы проекта на месте
3. Попробуйте пересобрать проект с нуля

Удачи! 💪
