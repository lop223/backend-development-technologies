# Backend Template Scaffold (Single-Module)

Base project for the Development of High-Load Corporate Systems course.
This is your starting point — a single, flat Spring Boot module.

**Multi-module Maven is not introduced yet.** Later in the course, you'll
refactor this exact project into a parent/child multi-module structure as a
dedicated exercise — don't restructure it yourself ahead of time.

## What's included

- **Spring Boot 3.5.16**, single module (`pom.xml` at root, packaging `jar`)
- **Keycloak IAM**, pre-configured with a realm, a client, and two test users
  — no manual setup needed
- **Spring Boot Docker Compose integration** (`spring-boot-docker-compose`) —
  Keycloak starts and stops automatically with the app. No `docker compose up`
  required.

## Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop (or compatible) running locally

## Running it

From the repo root:

```bash
mvn spring-boot:run
```

On startup, Spring Boot detects `compose.yaml` and starts the Keycloak
container for you. Give Keycloak a few seconds to become ready on first run.

- App: http://localhost:8081
- Keycloak admin console: http://localhost:8080 (`admin` / `admin`)

Stopping the app (Ctrl+C) stops the Keycloak container too, by default.

## Pre-configured realm

Realm: `course-realm` — imported automatically from `keycloak/course-realm.json`.

| User  | Password | Roles        |
|-------|----------|--------------|
| alice | alice123 | USER, ADMIN  |
| bob   | bob123   | USER         |

Client: `backend-app` (public client, direct access grants enabled).

## Two separate security concerns

This scaffold splits security into **two `SecurityFilterChain` beans**:

1. **`apiSecurityFilterChain`** — `/api/**`, stateless, validates Bearer JWTs
   (resource server). This is what curl/Postman/the UI call once they hold a
   token.
2. **`uiSecurityFilterChain`** — everything else, session-based, handles the
   actual browser login redirect against Keycloak (OIDC client / `oauth2Login`).

## Try the login UI

1. Run the app (`mvn spring-boot:run`)
2. Open http://localhost:8081/ui/index.html in a browser
3. Since that path requires authentication, Spring Security redirects you
   through Keycloak's real login page — log in as `alice` / `alice123` or
   `bob` / `bob123`
4. You land back on the demo page, now with an active session
5. Click through the buttons: fetch your access token (obtained via the
   login you just did), then call the API endpoints with it

**Note (teaching shortcut, not a production pattern):** the UI fetches its
own raw access token via `/ui/token` and puts it in JS to call the API — a
real app wouldn't expose a token to client-side JS like this. It's done here
so the same simple page can demonstrate both chains working together without
a bigger client-side auth library.

## Manual testing without the UI (still works)

```bash
curl -X POST http://localhost:8080/realms/course-realm/protocol/openid-connect/token \
  -d "client_id=backend-app" \
  -d "grant_type=password" \
  -d "username=alice" \
  -d "password=alice123"
```

Copy the `access_token` from the response and call a secured endpoint:

```bash
curl http://localhost:8081/api/secure/whoami \
  -H "Authorization: Bearer <access_token>"
```

Public endpoint (no token needed):

```bash
curl http://localhost:8081/api/public/ping
```

## Notes

- `spring-boot-docker-compose` manages the container lifecycle automatically,
  but it does **not** auto-wire connection properties for Keycloak the way it
  does for services like Postgres or Redis (Keycloak isn't one of Spring
  Boot's built-in "known services"). That's why `issuer-uri` is still set
  explicitly in `application.yml`.
- Don't commit changes to `keycloak/course-realm.json` without discussing
  with your reviewer — this file is shared scaffolding.
