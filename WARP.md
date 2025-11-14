# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Tooling and Commands

This is a Maven-based Java 17 web application packaged as a WAR.

### Build

- Full build (produces `target/central-bank-of-belize.war`):
  - `mvn -q clean package`

### Tests

> There are currently no automated tests configured in `pom.xml` or under `src/test/java`. If tests are added later, prefer running them via Maven:
- All tests:
  - `mvn -q test`
- Single test class (replace with actual FQCN when present):
  - `mvn -q -Dtest=fully.qualified.TestClassName test`

### Running the app

The project builds a WAR intended to be deployed to a Jakarta Servlet 6.0–compatible container (e.g., Tomcat, Jetty).

Typical flow:

1. Build the WAR:
   - `mvn -q clean package`
2. Deploy `target/central-bank-of-belize.war` to your servlet container according to that container's documented deployment mechanism.

### Database configuration

Database connectivity is centralized in `bz.gov.centralbank.config.DatabaseConfig` and uses environment variables with sensible defaults:

- `CBZ_DB_HOST` (default `localhost`)
- `CBZ_DB_PORT` (default `5432`)
- `CBZ_DB_NAME` (default `central_bank`)
- `CBZ_DB_USER` (default `central_bank_app`)
- `CBZ_DB_PASSWORD` (default `change_me`)
- `CBZ_DB_SSL` (default `false`)

When running locally, set these as needed in your shell or container configuration.

## High-Level Architecture

### Overview

This is a classic JSP/Servlet + JDBC application with a simple layered structure:

- **Servlet layer** (`bz.gov.centralbank.servlets`): HTTP endpoints and request handling.
- **DAO layer** (`bz.gov.centralbank.dao`): Database access and transactional logic using JDBC.
- **Model layer** (`bz.gov.centralbank.models`): Plain Java objects representing domain entities.
- **Infrastructure/configuration** (`bz.gov.centralbank.config`, `bz.gov.centralbank.filters`, `bz.gov.centralbank.security`): Cross-cutting concerns like DB configuration, authentication filters, and password utilities.
- **View layer** (`WEB-INF/jsp/...` in the webapp, not all files enumerated here): JSP templates rendered by servlets.

The application is packaged as a WAR and relies on the external servlet container for HTTP, session management, and lifecycle.

### Request flow and authentication

- Incoming requests are handled by Jakarta Servlets in `bz.gov.centralbank.servlets`.
- Authentication is performed via `AuthServlet`:
  - `doGet` renders the login page (`/WEB-INF/jsp/login.jsp`).
  - `doPost` validates credentials using `UserDao` and `PasswordUtil`.
  - On success, a `User` object is stored in the HTTP session under the `"user"` attribute and the client is redirected to `/dashboard`.
- A custom authentication filter (in `bz.gov.centralbank.filters.AuthFilter`) is responsible for enforcing that only authenticated users can access protected resources, redirecting unauthenticated users to the login endpoint.

### Dashboard and transfers

- `DashboardServlet`:
  - Expects an authenticated `User` in the session (`"user"` attribute).
  - Uses `AccountDao` to load accounts for the current user and `TransactionDao` to load recent transactions.
  - Forwards to `/WEB-INF/jsp/dashboard.jsp` with `accounts` and `transactions` request attributes.
- `TransferServlet`:
  - Expects an authenticated `User` in the session.
  - Reads `fromAccountId`, `toAccountId`, `amount`, and optional `description` from the request.
  - Delegates to `AccountDao.transfer(...)` to perform the transfer and persist corresponding transaction records.
  - On completion (success or failure), forwards back to the dashboard logic (via the `/dashboard` mapping) so the updated state is rendered.

### Data access and transactions

All DB work goes through DAO classes using `DatabaseConfig.getConnection()`:

- `AccountDao`:
  - `findByUserId(userId)`: loads all accounts for a specific user.
  - `transfer(fromAccountId, toAccountId, amount, description)`: performs a funds transfer in a single DB transaction, using `SELECT ... FOR UPDATE` row locking, explicit debit/credit updates, and inserts corresponding `transactions` records (DEBIT/CREDIT rows) before committing.
- `TransactionDao`:
  - `findRecentByUserId(userId, limit)`: joins `transactions` with `accounts` to fetch the most recent transactions for the user's accounts, mapping `created_at` into an `OffsetDateTime`.
- `UserDao`:
  - `findByUsername(username)`: looks up the user by username and returns a populated `User` or `null`.

DAO methods are designed to be called within servlet request handlers; they open and close their own JDBC connections and manage transactions explicitly where needed.

### Password handling

Password operations are abstracted into `bz.gov.centralbank.security.PasswordUtil` (not fully reproduced here):

- `AuthServlet` uses `PasswordUtil.verifyPassword(char[] password, String passwordHash)` to validate credentials.
- Any future password creation or rotation logic should go through `PasswordUtil` to keep hashing and verification centralized.

### Configuration and environment

- Java version and encoding are defined in `pom.xml` properties:
  - `maven.compiler.source` / `target`: `17`
  - `project.build.sourceEncoding`: `UTF-8`
- The project depends on:
  - `jakarta.servlet-api` (scope `provided`) — supplied by the servlet container.
  - `org.postgresql:postgresql` — JDBC driver.
  - `org.slf4j:slf4j-api` and `org.slf4j:slf4j-simple` — logging.

When adding new modules or entrypoints, follow the existing package structure and reuse `DatabaseConfig`, `PasswordUtil`, and the DAO pattern for DB access.
