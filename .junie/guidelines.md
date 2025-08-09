# Herocraft Project Guidelines

## Project Overview

Herocraft is an unofficial database and deck builder for the Ivion Deck Building game by Luminary Games. The application is designed as a full-stack solution with a Kotlin/Ktor backend and a SvelteKit frontend, containerized with Docker for easy deployment.

## Project Structure

```
D:\Programming\Herocrafter\
├── src\
│   ├── main\                    # Kotlin backend source code
│   └── webapp\                  # SvelteKit frontend application
├── antlr\                       # ANTLR grammar files for parsing
├── build.gradle.kts             # Kotlin backend build configuration
├── Dockerfile                   # Container configuration
├── docker-compose.yml           # Local development environment
├── deploy-docker-compose.yml    # Production deployment
└── README.md                    # Project documentation
```

### Backend (Kotlin/Ktor)
- **Framework**: Ktor server with Netty engine
- **Database**: PostgreSQL with Exposed ORM and Flyway migrations
- **Caching**: Redis/Valkey for session storage
- **Storage**: MinIO/S3-compatible object storage for images
- **Authentication**: JWT-based auth system
- **Dependencies**: Koin for DI, Arrow for functional programming, ANTLR for parsing

### Frontend (SvelteKit)
- **Framework**: SvelteKit with TypeScript
- **Styling**: TailwindCSS with custom components
- **UI Libraries**: bits-ui, formsnap, superforms for forms
- **Data**: TanStack Table for data display
- **Build**: Vite with proper linting and formatting

## Development Workflow

### Backend Development
1. Use `./gradlew run` for local development
2. Database migrations are handled by Flyway
3. ANTLR grammars are auto-generated during build
4. Configuration files are in `src/main/resources/`

### Frontend Development
1. Navigate to `src/webapp/`
2. Use `npm run dev` for development server
3. Use `npm run build` for production builds
4. Linting: `npm run lint`, Formatting: `npm run format`

## Testing Approach

### Backend Testing
- Run backend tests with: `./gradlew test`
- Tests are located in `src/test/`
- Uses Kotlin Test and Ktor test utilities

### Frontend Testing
- Run frontend tests with: `npm run test` (from `src/webapp/`)
- Uses Vitest for testing framework
- Component tests should be co-located with components

**Junie should run both backend and frontend tests when making changes to verify correctness.**

## Build and Deployment

### Local Development
- Use `docker-compose up` to start all services (PostgreSQL, Redis, MinIO)
- Backend: `./gradlew run`
- Frontend: `cd src/webapp && npm run dev`

### Production Build
1. Backend: `./gradlew shadowJar` creates fat JAR
2. Frontend: `cd src/webapp && npm run build`
3. Docker: Uses pre-built releases from GitHub

**Junie should build the project using `./gradlew build` for backend and `npm run build` for frontend before submitting results to ensure compilation success.**

## Code Style Guidelines

### Backend (Kotlin)
- Follow Kotlin coding conventions
- Use detekt for static analysis (configured in `detekt.yml`)
- Prefer functional programming patterns with Arrow
- Use dependency injection with Koin
- Database operations should use Exposed DSL

### Frontend (TypeScript/Svelte)
- Use TypeScript for type safety
- Follow Prettier formatting rules
- Use ESLint for code quality
- Component naming: PascalCase for files, camelCase for props
- Use TailwindCSS classes, avoid custom CSS when possible
- Form handling should use superforms + Zod validation

### General
- Write meaningful commit messages
- Include tests for new functionality
- Update documentation when adding features
- Use semantic versioning for releases

## External Dependencies

### Required Services
- **PostgreSQL 16.x/17.x**: Primary database
- **Redis/Valkey**: Session storage (can be disabled with code changes)
- **MinIO/S3**: Object storage for card images
- **SMTP Server**: Email notifications (can be disabled)

### Development Tools
- **Java 21**: Required for backend
- **Node.js**: Required for frontend development
- **Docker**: For containerized development environment
