# Helpdesk — система з трьох сервісів

Проєкт поділено на незалежні Spring Boot застосунки:

| Сервіс | Відповідальність | Порт | Сховище |
|---|---|---:|---|
| `helpdesk-system` | Категорії, заявки, вебінтерфейс і планувальник | 8081 | окрема PostgreSQL `helpdesk_db` |
| `user-service` | Облікові записи та ролі | 8082 | окрема PostgreSQL `user_db` |
| `notification-service` | Асинхронні сповіщення про заявки | 8083 | не потребує БД |

Сервіси не мають Maven-залежностей один від одного. `helpdesk-system` звертається до `user-service` для перевірки користувачів, а сповіщення надсилає до `notification-service`. Keycloak використовується як окремий провайдер входу й ролей.

## Запуск локально

1. Створіть `.env` на основі `.env.example` і задайте унікальні значення для паролів БД, адміністратора Keycloak, трьох початкових користувачів та внутрішніх API-токенів.
2. Запустіть стек з кореня репозиторію:

   ```powershell
   docker compose up --build -d
   docker compose ps
   ```

   Перший запуск збирає сервіси, створює дві незалежні БД, застосовує міграції та додає `admin`, `agent1` і `user1` до БД користувачів і realm Keycloak. Паролі початкових користувачів зберігаються тільки у `.env`; імпорт користувачів ідемпотентний.
3. Відкрийте `http://localhost:8081` і перейдіть до заявок. Для входу використовуйте `admin` з `HELPDESK_ADMIN_PASSWORD`, `agent1` з `HELPDESK_AGENT_PASSWORD` або `user1` з `HELPDESK_USER_PASSWORD`.

`USER` може створювати заявки; `AGENT` і `ADMIN` можуть їх редагувати та змінювати статус; `ADMIN` має адміністративні API. Список і форма заявки отримують категорії та користувачів із відповідних сервісів. Заявки можна створювати й редагувати у вебінтерфейсі `/ui/tickets`.

Порти прив’язані до `localhost`; ця конфігурація призначена для локальної розробки, а не для публічного розгортання. Дані зберігаються у Docker volumes і переживають перезапуск контейнерів. Щоб зупинити сервіси без видалення даних, виконайте `docker compose down`; **не** додавайте `-v`, якщо не хочете видалити БД і Keycloak.

## Окремий запуск сервісів

Кожен модуль можна зібрати й запустити незалежно, задавши його змінні середовища та власну БД:

```powershell
mvn -pl user-service spring-boot:run
mvn -pl notification-service spring-boot:run
mvn -pl helpdesk-system spring-boot:run
```

Для `user-service` потрібні `USER_DB_URL`, `USER_DB_USERNAME`, `USER_DB_PASSWORD`, `USER_SERVICE_API_TOKEN`, `KEYCLOAK_ISSUER_URI` і `KEYCLOAK_JWK_SET_URI`. Для `notification-service` потрібен `NOTIFICATION_API_TOKEN`. `helpdesk-system` потребує власної PostgreSQL, доступного `user-service`, а також `USER_SERVICE_API_TOKEN` і `NOTIFICATION_API_TOKEN`; Keycloak має бути доступним для входу. У Docker Compose ці адреси та залежності вже налаштовані.

Сервіс користувачів надає CRUD API за `/api/users` для адміністратора з JWT-токеном Keycloak та внутрішні read-only маршрути `/internal/users`, захищені `X-Internal-Token`. Сервіс заявок має REST API `/api/tickets` і `/api/categories`. Внутрішні токени не замінюють автентифікацію користувачів.

## Перевірка

```powershell
mvn verify
docker compose logs --tail 100 helpdesk-system user-service notification-service
```

Якщо змінювали код після попередньої збірки, перезберіть контейнери через `docker compose up --build -d`.
