# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Herocraft is a full-stack database and deck builder for the Ivion card game. It consists of a Kotlin/Ktor backend with PostgreSQL and a SvelteKit frontend.

## Version Control

This repository uses [Jujutsu (jj)](https://github.com/martinvonz/jj) for version control, not Git directly. Jujutsu is a Git-compatible VCS that provides a more intuitive workflow.

While Git commands can still be used (jj maintains Git compatibility), prefer using jj commands:
- `jj status` - View working copy status
- `jj diff` - Show changes
- `jj log` - View commit history
- `jj commit -m "message"` - Create a new commit
- `jj describe -m "message"` - Update the current change description
- `jj new` - Create a new change
- `jj squash` - Squash changes into parent

Note: The repository has both `.jj/` and `.git/` directories. Jujutsu manages the `.jj/` directory and keeps `.git/` in sync.

## Development Commands

### Using mise (recommended)

The project uses [mise](https://mise.jdx.dev/) for task management. View available tasks:

```bash
mise tasks
```

Common tasks:
- `mise run install` - Install all dependencies (frontend + backend)
- `mise run dev-backend` - Run backend dev server
- `mise run dev-frontend` - Run frontend dev server
- `mise run build` - Build entire project
- `mise run test` - Run all tests
- `mise run test-backend` - Run backend tests only
- `mise run test-frontend` - Run frontend tests only
- `mise run lint-frontend` - Lint frontend code
- `mise run format-frontend` - Format frontend code
- `mise run docker-up` - Start Docker services (PostgreSQL, Redis, MinIO)
- `mise run docker-down` - Stop Docker services
- `mise run migrate` - Run database migrations

### Direct commands

Backend (from root):
- `./gradlew run` - Run backend
- `./gradlew test` - Run tests
- `./gradlew build` - Build backend
- `./gradlew flywayMigrate` - Run database migrations
- `./gradlew --continuous build` - Continuous build (watches for changes)

Frontend (from `src/webapp/`):
- `npm run dev` - Run dev server (with --host flag)
- `npm run build` - Production build
- `npm run check` - Type check
- `npm run lint` - Lint code
- `npm run format` - Format code

## Architecture

### Backend (Kotlin/Ktor)

Location: `src/main/kotlin/app/herocraft/`

**Entry point:** `Application.kt` - Configures Ktor server, sets up Koin DI, registers routes

**Directory structure:**
- `core/` - Foundational code shared across features
  - `api/` - DTOs for requests/responses
  - `extensions/` - Extension functions and base classes (notably `DataService.kt`)
  - `models/` - Domain models (`IvionCard`, `IvionDeck`, `User`, `Ruling`, etc.)
  - `security/` - Authentication/authorization (session management, user service, JWT)
- `features/` - Feature-based organization (domain-driven design)
  - `builder/` - Deck building (CRUD, likes, favorites)
  - `search/` - Card search with HQL query language (ANTLR-based parser)
  - `rulings/` - Card rulings management
  - `images/` - Image processing and S3 storage
  - `notifications/` - Email and console notifications
  - `metrics/` - Prometheus metrics
- `plugins/` - Ktor plugin configurations (routing, security, serialization, etc.)
- `di/` - Koin dependency injection modules

**Key patterns:**
- Repository pattern: Each feature has a `Repo` class extending `DataService`
- All database operations are suspend functions (coroutines)
- Exposed ORM v1 for type-safe SQL
- Table definitions are nested objects within repos (e.g., `DeckRepo.Deck`)
- Use `dbQuery {}` wrapper from `DataService` for database transactions
- Features register routes via extension functions called in `Application.kt`

**Database:**
- PostgreSQL 16/17 with Flyway migrations in `src/main/resources/db/migration/`
- Redis for session storage
- MinIO (S3-compatible) for image storage

### Frontend (SvelteKit)

Location: `src/webapp/src/`

**Directory structure:**
- `routes/` - File-based routing with route groups:
  - `(app)/` - Main authenticated app (cards, decks, account, admin)
  - `(home)/` - Landing page
  - `(login)/` - Authentication flows
- `lib/` - Reusable code
  - `components/` - Svelte components organized by feature
    - `ui/` - shadcn-svelte UI components
    - `deck-card/`, `deck-navigation/`, `deck-settings/` - Deck-related components
    - `CardText/` - Custom markdown renderer for card text
  - `routes.ts` - API URL constants mapping to backend endpoints
  - `utils.ts` - Utility functions
- `ambient.d.ts` - TypeScript types for domain models (must match backend)
- `app.d.ts` - SvelteKit app types

**Key patterns:**
- Svelte 5 with runes (`$props`, `$state`, `$derived`, `$effect`)
- Server/client split: `+page.server.ts` for server-side, `+page.ts` for client
- API URLs centralized in `lib/routes.ts`
- Form handling with sveltekit-superforms
- TailwindCSS 4 for styling

**Server hooks:**
- `hooks.server.ts` - Reads `user_session` cookie and sets `event.locals.user`

### Authentication Flow

1. Login/Register → Session created in Redis
2. Backend sets `user_session` cookie
3. Frontend `hooks.server.ts` reads cookie and populates `locals.user`
4. Protected backend routes use `authenticate("auth-session")`
5. Admin routes additionally check `userRepo.isAdmin()`

### Search Query Language (HQL)

Herocraft Query Language is an ANTLR-based search syntax:
- Grammar: `antlr/HQL.g4`
- Parser: `features/search/SearchVisitor.kt`
- Examples: `a:Artificer`, `f:Action`, `a:Artificer AND f:Action`
- Supports field queries, boolean operators (AND/OR/NOT), comparisons, quoted strings
- Converted to Exposed DSL queries

### Card Data Model

The `IvionCard` model supports complex card structures:
- Multi-face cards (front/back with different attributes)
- Linked parts (tokens, transformations)
- Variants (different art/versions)
- Card faces stored in `card_faces` table with per-face overrides
- Layout grouping (NORMAL, TRANSFORM, TOKEN) in `card_layout_groups`

## External Dependencies

Required services (configured in `docker-compose.yml`):
- PostgreSQL 16/17 on port 5432
- Redis on port 6379 (for sessions)
- MinIO on ports 9000/9001 (for S3-compatible image storage)
- SMTP server (optional, notifications can log to console)

## Important Files

Backend:
- `Application.kt` - App setup and configuration
- `core/extensions/DataService.kt` - Base repository class
- `di/DatabaseModule.kt`, `di/ServiceModule.kt` - Dependency injection setup
- `features/builder/DeckRepo.kt` - Deck database operations
- `features/search/CardRepo.kt` - Card database and search
- `core/security/SecurityRouter.kt` - Auth routes

Frontend:
- `routes/+layout.svelte` - Global layout
- `lib/routes.ts` - API URL constants
- `ambient.d.ts`, `app.d.ts` - Type definitions
- `routes/(app)/deck/[slug]/+page.svelte` - Example of complex route

Configuration:
- `build.gradle.kts` - Backend build configuration
- `src/webapp/package.json` - Frontend dependencies
- `docker-compose.yml` - Local development environment
- `.mise.toml` - Task runner configuration

## Code References

When referencing code, use the pattern `file_path:line_number` for easy navigation. Example: `src/main/kotlin/app/herocraft/Application.kt:42`

## Version Requirements

- Node.js: 22.16.0
- Java: 21
- PostgreSQL: 16.x or 17.x
- Kotlin: See `kotlin_version` in `gradle.properties`
