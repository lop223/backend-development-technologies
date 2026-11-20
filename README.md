# Лабораторна робота 8: асинхронні події заявок через RabbitMQ

## Мета роботи

Реалізувати подієву взаємодію між сервісами Helpdesk: після змін заявки сервіс заявок публікує подію в RabbitMQ, а сервіс сповіщень отримує її та зберігає у власній базі даних.

## Структура проєкту

Кореневий Maven-проєкт збирає три незалежні модулі:

- `helpdesk-system` — заявки та категорії, порт `8081`;
- `user-service` — користувачі, порт `8082`;
- `notification-service` — споживання та збереження подій заявок, порт `8083`.

Сервіси не підключають код один одного як Maven-залежність. Дані користувачів, заявок і подій зберігаються в окремих PostgreSQL-базах. Helpdesk отримує дані користувачів через внутрішнє API `user-service`; події заявок надходять у `notification-service` через RabbitMQ.

## Потік подій

1. `TicketServiceImpl` створює, оновлює або видаляє заявку. Для створення, зміни полів, зміни статусу й видалення формуються події `TICKET_CREATED`, `TICKET_UPDATED`, `TICKET_STATUS_CHANGED` і `TICKET_DELETED`.
2. `TicketEventPublisher` реєструє публікацію після успішного commit транзакції заявки. Подія передається як JSON через довговічний direct exchange `helpdesk.ticket.exchange` з routing key `ticket.event`.
3. `notification-service` прив’язує до exchange довговічну чергу `helpdesk.ticket.events` і слухає її через `TicketEventConsumer`.
4. Consumer записує подію в таблицю `ticket_event_records` окремої PostgreSQL-бази сповіщень. Зберігаються ID події, тип дії, ID заявки, опис, час виникнення та час отримання.
5. Повторне доставлення з тим самим `eventId` розпізнається і не створює повторний запис. У БД `event_id` також має обмеження унікальності.

Публікація виконується після commit, а не в межах спільної транзакції з RabbitMQ; у проєкті не використовується transactional outbox. Тому вона не відкочує вже збережену заявку в разі проблеми з брокером.

## Запуск

Потрібні Docker Compose, JDK 21 і Maven (для локального запуску тестів). З кореня проєкту створіть `.env` на основі прикладу та замініть усі значення `replace-with-...` на власні паролі й токени:

```powershell
Copy-Item .env.example .env
```

Обов’язкові змінні середовища:

- `HELPDESK_DB_PASSWORD`, `USER_DB_PASSWORD`, `NOTIFICATION_DB_PASSWORD`;
- `RABBITMQ_PASSWORD`;
- `USER_SERVICE_API_TOKEN`, `NOTIFICATION_API_TOKEN`;
- `KEYCLOAK_ADMIN_PASSWORD`, `HELPDESK_ADMIN_PASSWORD`, `HELPDESK_AGENT_PASSWORD`, `HELPDESK_USER_PASSWORD`.

Файл `.env` містить секрети — не додавайте його до Git. Після налаштування запустіть усі сервіси:

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f helpdesk-system notification-service rabbitmq
```

Compose запускає RabbitMQ, Keycloak, три PostgreSQL-бази та три сервіси. RabbitMQ доступний локально на портах `5672` (AMQP) і `15672` (панель керування); сервіси — на портах `8081`–`8083`. Перед запуском конфігурацію можна перевірити командою `docker compose config -q` після заповнення `.env`.

`user-service` і внутрішні маршрути `notification-service` захищені токеном `X-Internal-Token`. Автентифікація користувачів і ролі API налаштовані через Keycloak.

## Тестування

Запустити тести всіх Maven-модулів з кореня проєкту:

```powershell
mvn test
```

Повна перевірка зі збиранням:

```powershell
mvn clean verify
```

Тести перевіряють логіку сервісу заявок, повторну доставку подій та API сервісу сповіщень.

## Висновок

У лабораторній роботі додано асинхронний канал RabbitMQ між `helpdesk-system` і `notification-service`. Події змін заявок публікуються після commit, проходять через довговічну чергу та зберігаються окремо від даних заявок; перевірка `eventId` запобігає повторному запису при повторному доставленні.