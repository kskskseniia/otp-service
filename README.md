# OTP Service

Backend-приложение для защиты пользовательских операций с помощью временных OTP-кодов.

Сервис позволяет регистрировать пользователей, выполнять вход в систему, генерировать одноразовые коды подтверждения, отправлять их через разные каналы доставки и проверять введённые пользователем коды.

Проект разработан в рамках учебного задания по backend-разработке с акцентом на работу с HTTP API, PostgreSQL, JDBC, токенной аутентификацией, ролями пользователей, слоями приложения и интеграцией с внешними сервисами доставки сообщений.

* * *

## Описание

Приложение позволяет:

* регистрировать пользователей с ролями `USER` и `ADMIN`;
* запрещать регистрацию второго администратора;
* выполнять вход пользователя в систему;
* выдавать JWT-токен с ограниченным сроком действия;
* разграничивать доступ к API по ролям;
* настраивать длину и время жизни OTP-кода;
* генерировать OTP-код для конкретной операции;
* сохранять OTP-код в базе данных со статусом `ACTIVE`;
* автоматически переводить просроченные коды в статус `EXPIRED`;
* проверять OTP-код и переводить его в статус `USED`;
* отправлять OTP-код через файл, email, Telegram и SMS через SMPP-эмулятор;
* логировать HTTP-запросы и основные действия приложения.

OTP-код привязывается к пользователю и идентификатору операции. Это позволяет защищать разные операции отдельно, например оплату, перевод средств или изменение данных.

* * *

## Требования

* Java 20 или выше
* Maven 3.8+
* PostgreSQL 17
* Postman или другой HTTP-клиент для проверки API
* Для SMS-канала: SMPP-эмулятор
* Для Telegram-канала: Telegram-бот
* Для Email-канала: почтовый ящик с SMTP-доступом

* * *

## Используемые технологии

* Java
* Maven
* PostgreSQL
* JDBC
* com.sun.net.httpserver
* Jackson
* JWT
* BCrypt
* SLF4J
* Logback
* Jakarta Mail / Angus Mail
* jSMPP
* Telegram Bot API

* * *

## Получение проекта

Клонируйте репозиторий и перейдите в каталог проекта:

    git clone https://github.com/kskskseniia/otp-service.git
    cd otp-service

* * *

## Сборка проекта

Проект использует Maven, все зависимости загружаются автоматически:

    mvn clean package

После успешной сборки исполняемый JAR-файл будет находиться в папке:

    target/

* * *

## Подготовка базы данных

Проект использует PostgreSQL.

Создайте базу данных:

    psql -U postgres -p 5433

Внутри PostgreSQL выполните:

    CREATE DATABASE otp_service_db;

Выйдите из консоли PostgreSQL:

    \q

После этого выполните SQL-скрипт создания таблиц:

    psql -U postgres -p 5432 -d otp_service_db -f src/main/resources/db/init.sql


* * *

## Структура базы данных

В проекте используются три основные таблицы.

### users

Таблица пользователей.

Поля:

* `id` — идентификатор пользователя;
* `username` — логин пользователя;
* `password_hash` — хеш пароля;
* `role` — роль пользователя: `USER` или `ADMIN`;
* `created_at` — дата регистрации.

### otp_config

Таблица конфигурации OTP-кодов.

Поля:

* `id` — идентификатор конфигурации;
* `code_length` — длина OTP-кода;
* `ttl_seconds` — время жизни OTP-кода в секундах.

В таблице должна быть только одна запись.

### otp_codes

Таблица OTP-кодов.

Поля:

* `id` — идентификатор OTP-кода;
* `user_id` — идентификатор пользователя;
* `operation_id` — идентификатор защищаемой операции;
* `code` — сгенерированный OTP-код;
* `status` — статус кода: `ACTIVE`, `EXPIRED`, `USED`;
* `created_at` — дата создания;
* `expires_at` — дата истечения срока действия.

При удалении пользователя связанные с ним OTP-коды удаляются автоматически.

* * *

## Конфигурация приложения

Локальные конфигурационные файлы не добавляются в GitHub.

В репозитории находятся только файлы-примеры:

    src/main/resources/application.properties.example
    src/main/resources/email.properties.example
    src/main/resources/telegram.properties.example
    src/main/resources/sms.properties.example

Перед запуском приложения необходимо создать локальные файлы конфигурации.

* * *

### application.properties

Создайте файл:

    src/main/resources/application.properties

Пример содержимого:

    server.port=8080

    db.url=jdbc:postgresql://localhost:5432/otp_service_db
    db.username=postgres
    db.password=your_database_password

    jwt.secret=your_secret_key
    jwt.expiration.minutes=60

    otp.expiration.check.interval.seconds=30

* * *

### email.properties

Создайте файл:

    src/main/resources/email.properties

Пример для Gmail:

    email.username=your_email@gmail.com
    email.password=your_app_password
    email.from=your_email@gmail.com

    mail.smtp.host=smtp.gmail.com
    mail.smtp.port=587
    mail.smtp.auth=true
    mail.smtp.ssl.enable=false
    mail.smtp.starttls.enable=true
    mail.smtp.starttls.required=true
    mail.smtp.user=your_email@gmail.com

    mail.smtp.connectiontimeout=10000
    mail.smtp.timeout=10000
    mail.smtp.writetimeout=10000

    mail.debug=false

* * *

### telegram.properties

Создайте файл:

    src/main/resources/telegram.properties

Пример содержимого:

    telegram.bot.token=YOUR_BOT_TOKEN
    telegram.chat.id=YOUR_CHAT_ID
    telegram.api.url=https://api.telegram.org/bot


* * *

### sms.properties

Создайте файл:

    src/main/resources/sms.properties

Пример содержимого:

    smpp.host=localhost
    smpp.port=2775
    smpp.system_id=smppclient1
    smpp.password=password
    smpp.system_type=OTP
    smpp.source_addr=OTPService

Для проверки SMS-канала необходимо запустить SMPP-эмулятор на порту `2775`.

* * *

## Запуск приложения

### Запуск из IDE

Точка входа в приложение:

    src/main/java/org/example/Main.java

Запустите класс `Main`.

После запуска в консоли должно появиться сообщение:

    OTP Service started on port 8080

* * *

### Запуск из JAR

Соберите проект:

    mvn clean package

Запустите приложение:

    java -jar target/otp-service-1.0-SNAPSHOT.jar

* * *

## API

Все запросы, кроме регистрации и логина, требуют JWT-токен.

Токен передаётся в заголовке:

    Authorization: Bearer YOUR_TOKEN

* * *

## Аутентификация

### Регистрация пользователя

    POST /api/register

Пример запроса:

    {
      "username": "user1",
      "password": "12345",
      "role": "USER"
    }

Пример ответа:

    {
      "id": 1,
      "username": "user1",
      "role": "USER"
    }

* * *

### Регистрация администратора

    POST /api/register

Пример запроса:

    {
      "username": "admin",
      "password": "admin123",
      "role": "ADMIN"
    }

В системе может быть только один администратор. Попытка создать второго администратора завершится ошибкой.

* * *

### Авторизация пользователя

    POST /api/login

Пример запроса:

    {
      "username": "user1",
      "password": "12345"
    }

Пример ответа:

    {
      "role": "USER",
      "userId": 1,
      "username": "user1",
      "token": "JWT_TOKEN"
    }

Полученный токен используется для доступа к защищённым API.

* * *

## API администратора

Админские запросы доступны только пользователю с ролью `ADMIN`.

* * *

### Получить конфигурацию OTP

    GET /api/admin/config

Заголовок:

    Authorization: Bearer ADMIN_TOKEN

Пример ответа:

    {
      "codeLength": 6,
      "ttlSeconds": 300
    }

* * *

### Изменить конфигурацию OTP

    PUT /api/admin/config

Заголовок:

    Authorization: Bearer ADMIN_TOKEN

Пример запроса:

    {
      "codeLength": 6,
      "ttlSeconds": 120
    }

Пример ответа:

    {
      "codeLength": 6,
      "ttlSeconds": 120
    }

* * *

### Получить список пользователей

    GET /api/admin/users

Заголовок:

    Authorization: Bearer ADMIN_TOKEN

Метод возвращает всех пользователей, кроме администраторов.

* * *

### Удалить пользователя

    DELETE /api/admin/users/{id}

Пример:

    DELETE /api/admin/users/2

Заголовок:

    Authorization: Bearer ADMIN_TOKEN

Пример ответа:

    {
      "message": "User deleted successfully"
    }

При удалении пользователя его OTP-коды удаляются автоматически.

* * *

## API пользователя

Пользовательские запросы доступны только пользователю с ролью `USER`.

* * *

### Генерация OTP-кода

    POST /api/otp/generate

Заголовок:

    Authorization: Bearer USER_TOKEN

Пример запроса:

    {
      "operationId": "PAYMENT-1001",
      "channel": "FILE",
      "destination": "local-file"
    }

Пример ответа:

    {
      "message": "OTP code generated successfully",
      "operationId": "PAYMENT-1001",
      "status": "ACTIVE",
      "expiresAt": "2026-05-01T12:00:00"
    }

Поля запроса:

* `operationId` — идентификатор операции;
* `channel` — канал доставки;
* `destination` — получатель кода.

Доступные каналы:

* `FILE`;
* `EMAIL`;
* `TELEGRAM`;
* `SMS`.

* * *

### Валидация OTP-кода

    POST /api/otp/validate

Заголовок:

    Authorization: Bearer USER_TOKEN

Пример запроса:

    {
      "operationId": "PAYMENT-1001",
      "code": "123456"
    }

Пример ответа:

    {
      "message": "OTP code validated successfully"
    }

После успешной проверки код получает статус `USED`.

Если срок действия кода истёк, код получает статус `EXPIRED`.

* * *

## Каналы доставки OTP

### FILE

Канал `FILE` сохраняет OTP-код в файл:

    otp_codes.txt

Пример запроса:

    {
      "operationId": "PAYMENT-FILE-1001",
      "channel": "FILE",
      "destination": "local-file"
    }

Файл `otp_codes.txt` создаётся в корне проекта и не добавляется в GitHub.

* * *

### EMAIL

Канал `EMAIL` отправляет OTP-код на электронную почту через SMTP.

Пример запроса:

    {
      "operationId": "PAYMENT-EMAIL-1001",
      "channel": "EMAIL",
      "destination": "user@example.com"
    }

Перед использованием необходимо настроить файл:

    src/main/resources/email.properties

* * *

### TELEGRAM

Канал `TELEGRAM` отправляет OTP-код через Telegram Bot API.

Пример запроса:

    {
      "operationId": "PAYMENT-TELEGRAM-1001",
      "channel": "TELEGRAM",
      "destination": "Telegram"
    }

Перед использованием необходимо настроить файл:

    src/main/resources/telegram.properties

* * *

### SMS

Канал `SMS` отправляет OTP-код в SMPP-эмулятор.

Пример запроса:

    {
      "operationId": "PAYMENT-SMS-1001",
      "channel": "SMS",
      "destination": "79990000000"
    }

Перед использованием необходимо запустить SMPP-эмулятор и настроить файл:

    src/main/resources/sms.properties

* * *

## Статусы OTP-кодов

OTP-код может иметь один из трёх статусов:

* `ACTIVE` — код активен и может быть использован;
* `EXPIRED` — срок действия кода истёк;
* `USED` — код был успешно проверен и больше не может быть использован.

Просроченные коды автоматически переводятся в статус `EXPIRED` с помощью фонового планировщика.

* * *

## Логирование

В приложении настроено логирование через SLF4J и Logback.

Логи выводятся:

* в консоль;
* в файл:

      logs/otp-service.%d{yyyy-MM-dd}.log

Логируются:

* входящие HTTP-запросы;
* успешное выполнение запросов;
* ошибки выполнения запросов;
* регистрация и вход пользователей;
* генерация и проверка OTP-кодов;
* изменение конфигурации OTP;
* удаление пользователей;
* автоматическое истечение OTP-кодов.

Файлы логов не добавляются в GitHub.

* * *

## Архитектура проекта

Проект реализован с разделением на логические слои.

### Структура проекта

    src/main/java/org/example
    ├── api            — обработчики HTTP-запросов
    ├── config         — загрузка конфигурации и подключение к БД
    ├── dao            — работа с PostgreSQL через JDBC
    ├── model          — модели и перечисления
    ├── notification   — каналы доставки OTP-кодов
    ├── security       — JWT, проверка токенов и хеширование паролей
    ├── service        — бизнес-логика приложения
    ├── util           — вспомогательные классы
    └── Main.java      — точка входа в приложение

* * *

## Описание слоёв и компонентов

### API Layer

Содержит обработчики HTTP-запросов на основе `com.sun.net.httpserver`.

* `AuthHandler` — регистрация и логин;
* `AdminHandler` — API администратора;
* `OtpHandler` — генерация и проверка OTP-кодов.

API-слой не содержит бизнес-логику и обращается к сервисному слою.

* * *

### Service Layer

Содержит основную бизнес-логику приложения.

* `AuthService` — регистрация, логин, проверка паролей;
* `AdminService` — управление пользователями и конфигурацией OTP;
* `OtpService` — генерация, сохранение, отправка и проверка OTP-кодов;
* `ExpiredOtpScheduler` — автоматическое обновление статусов просроченных кодов.

* * *

### DAO Layer

Отвечает за выполнение SQL-запросов к PostgreSQL.

* `UserDao` — работа с пользователями;
* `OtpConfigDao` — работа с конфигурацией OTP;
* `OtpCodeDao` — работа с OTP-кодами.

* * *

### Model Layer

Содержит модели данных и перечисления.

* `User` — пользователь системы;
* `Role` — роль пользователя;
* `OtpConfig` — конфигурация OTP;
* `OtpCode` — OTP-код;
* `OtpStatus` — статус OTP-кода.

* * *

### Notification Layer

Содержит реализации каналов доставки OTP-кодов.

* `NotificationService` — общий интерфейс доставки;
* `FileNotificationService` — сохранение кода в файл;
* `EmailNotificationService` — отправка кода по email;
* `TelegramNotificationService` — отправка кода через Telegram;
* `SmsNotificationService` — отправка кода через SMPP-эмулятор.

* * *

### Security Layer

Содержит компоненты безопасности.

* `PasswordHasher` — хеширование и проверка паролей через BCrypt;
* `JwtService` — генерация и проверка JWT-токенов;
* `AuthMiddleware` — извлечение токена из заголовка и проверка роли;
* `AuthContext` — данные пользователя из JWT.

* * *

### Config Layer

Отвечает за загрузку конфигурации.

* `AppConfig` — загрузка `application.properties`;
* `DatabaseConfig` — создание JDBC-подключения к PostgreSQL.

* * *

### Util Layer

Содержит вспомогательные компоненты.

* `HttpUtils` — чтение JSON-запросов и отправка JSON-ответов.

* * *

## Принципы проектирования

В проекте применяются следующие принципы:

* разделение ответственности между слоями;
* отделение API от бизнес-логики;
* отделение бизнес-логики от работы с базой данных;
* хранение конфигурации вне кода;
* защита секретов через `.gitignore`;
* единый интерфейс для разных каналов доставки OTP;
* использование JWT для аутентификации и авторизации.

* * *

## Проверка приложения

Для проверки приложения можно использовать Postman.

Рекомендуемый сценарий проверки:

1. Создать администратора через `/api/register`.
2. Выполнить логин администратора через `/api/login`.
3. Получить и изменить конфигурацию OTP через Admin API.
4. Создать обычного пользователя через `/api/register`.
5. Выполнить логин обычного пользователя.
6. Проверить список пользователей через Admin API.
7. Проверить удаление пользователя через Admin API. 
8. Проверить, что запросы без токена отклоняются. 
9. Сгенерировать OTP-код через канал `FILE`. 
10. Проверить OTP-код через `/api/otp/validate`. 
11. Проверить повторную валидацию использованного кода. 
12. Проверить истечение срока действия OTP-кода. 
13. Проверить отправку OTP через `EMAIL`. 
14. Проверить отправку OTP через `TELEGRAM`. 
15. Проверить отправку OTP через `SMS` и SMPP-эмулятор.


.