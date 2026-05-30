# HYPER Android - Инструкция по сборке APK

## QR-вход (уже реализован)
Функциональность QR-входа уже встроена в ProfileFragment.kt:
- Кнопка "Войти по QR-коду" в профиле открывает камеру
- Сканирует QR с ПК-версии HYPER (/login → вкладка QR-код)
- Отправляет POST /api/qr-confirm на hyper-messenger.ru

## Как собрать APK

### Вариант 1 — Android Studio (рекомендуется)
1. Установите Android Studio: https://developer.android.com/studio
2. Откройте папку hyper-android как проект
3. Дождитесь синхронизации Gradle
4. Build → Generate Signed Bundle/APK → APK → Debug (для теста)
5. APK будет в app/build/outputs/apk/debug/app-debug.apk

### Вариант 2 — Командная строка
Требования: JDK 17+, Android SDK (ANDROID_HOME настроен)

APK: app/build/outputs/apk/debug/app-debug.apk

### Вариант 3 — GitHub Actions (CI/CD)
Добавьте .github/workflows/build.yml — автоматическая сборка на каждый push.

## Подключение к серверу
Файл: app/src/main/java/ru/hyper/messenger/api/RetrofitClient.kt
BASE_URL = "https://hyper-messenger.ru/"
Замените на адрес вашего сервера при необходимости.
