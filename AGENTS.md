# Repository Guidelines

likeadmin_java is a multi-module Spring Boot 2.7 + Vue 3 admin platform with a Nuxt PC front-end and a uni-app mobile client.

## Project Structure & Module Organization
- `server/` — Java 8 Maven backend (parent `pom.xml`).
  - `ke-admin/` boots the admin API (`com.mdd.admin`), `ke-front/` the client API, `ke-common/` shared code, `ke-generator/` code-generation tooling.
  - Java sources live in `src/main/java/com/mdd/<module>/{controller,service,vo,validate,config,...}`; resources in `src/main/resources`.
- `admin/` — Vue 3 + Vite + Element Plus back-office (sources in `admin/src`, entry `main.ts`).
- `pc/` — Nuxt 3 PC front-end (`pages/`, `components/`, `stores/`, `nuxt.config.ts`).
- `uniapp/` — uni-app mobile client (`src/`, platform scripts in `scripts/`).
- `sql/install.sql` seeds the database; `docker/` holds compose files; `public/` stores runtime assets.

## Build, Test, and Development Commands
- Backend dev: `cd server && mvn spring-boot:run -pl ke-admin` (or `-pl ke-front`).
- Backend package: `cd server && mvn -DskipTests clean package` — jars land in each module's `target/`.
- Admin UI: `cd admin && npm install && npm run dev`; `npm run build`, `npm run type-check`, `npm run lint`.
- PC UI: `cd pc && npm install && npm run dev`; static `npm run build`, SSR `npm run build:ssr`.
- Mobile: `cd uniapp && npm install && npm run dev:h5` (or `dev:mp-weixin`, `dev:app`); release via `npm run build:h5`.
- One-shot demo: `docker run -it -p 20222:20222 -p 20223:20223 likeshop/likeadmin_java:1.5.0`.

## Coding Style & Naming Conventions
- Java: 4-space indent, UTF-8, package `com.mdd.*`; classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE`; Lombok is available.
- Front-ends: 4-space indent, TypeScript preferred; components `PascalCase.vue`, composables `useXxx.ts`, Pinia stores `useXxxStore`.
- Run `npm run lint` (ESLint + Prettier) before committing UI changes.

## JSON Field Naming (Backend ↔ Frontend Contract)
**This is a recurring source of bugs; read this before touching any admin API or view.**

- `ke-admin` has **no** global Jackson naming strategy: responses are serialized in **camelCase** by default. `ke-front` (C-end) uses `SNAKE_CASE`.
- **Admin Vue reads**: use camelCase keys to match the response (e.g. `formData.vipExpired`, `row.userMoney`). Existing list pages (`<el-table-column prop="...">`) already follow this convention — mirror them.
- **Admin Vue writes** (request bodies, `field` values in generic edit endpoints): follow whatever the backend `switch(field)` / validator expects. That is usually `snake_case` (e.g. `real_name`, `user_id`, `batch_key`). Check the target controller before assuming.
- **Backend Validate objects** (request DTOs) with `snake_case` JSON fields **must** annotate each field with `@JsonProperty("snake_case")`. Do not rely on a global strategy — there isn't one. Search existing `validate/**` files for examples.
- **Backend VO objects** (response DTOs) should stay camelCase; only add `@JsonProperty("snake_case")` when a specific field must be delivered as snake_case for legacy compatibility.
- Never enable a project-wide `SNAKE_CASE` in `ke-admin`: dozens of existing VOs/validates were authored under the camelCase-by-default assumption and would silently break.

## Testing Guidelines
- Backend tests live in `server/<module>/src/test/java`; run `mvn test -pl ke-admin -Dmaven.test.skip=false` (parent skips tests by default).
- No JS test runner is configured; rely on `npm run type-check` and manual checks against the demo (`admin` / `123456`).

## Commit & Pull Request Guidelines
- Follow the existing prefix style: `feat …`, `fix …`, `debug …`, or concise Chinese summaries (e.g. `调整README信息`). Keep subjects imperative and under ~60 chars.
- Target the `develop` branch; PRs should state scope, list impacted modules (`server`, `admin`, `pc`, `uniapp`), link issues, attach UI screenshots or API samples, and call out any new `sql/` migrations.

## Security & Configuration Tips
- Override `server/ke-admin/src/main/resources/application-*.yml` locally; never commit real DB, Redis, OSS, or WeChat secrets. Rotate the default `admin/123456` credentials before any shared deployment.
