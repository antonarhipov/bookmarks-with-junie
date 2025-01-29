# Technical Context

## Technology Stack

### Backend
1. Core Framework
   - Spring Boot (latest stable version)
   - Java 17
   - Spring MVC for REST API
   - Spring Data JDBC for persistence

2. Database
   - H2 Database (embedded)
   - JDBC for database access
   - SQL for schema management

3. Build Tools
   - Gradle (Kotlin DSL)
   - JUnit 5 for testing
   - Mockito for mocking

### Frontend
1. Core Technologies
   - TypeScript
   - HTML5
   - Tailwind CSS

2. Development Tools
   - Node.js
   - npm/yarn
   - TypeScript compiler

## Development Setup

### Prerequisites
1. JDK 17 or higher
2. Gradle 7.x or higher
3. Node.js and npm
4. IDE with Spring Boot support (IntelliJ IDEA recommended)

### Build Configuration
1. Backend (build.gradle.kts)
   ```kotlin
   dependencies {
       implementation("org.springframework.boot:spring-boot-starter-web")
       implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
       implementation("com.h2database:h2")
       testImplementation("org.springframework.boot:spring-boot-starter-test")
   }
   ```

2. Frontend
   ```json
   {
     "dependencies": {
       "typescript": "^4.x",
       "tailwindcss": "^3.x"
     }
   }
   ```

## Technical Constraints

### Performance Requirements
1. Lazy Loading
   - Maximum page size: 20 items
   - Smooth scrolling experience
   - Efficient memory usage

2. Response Times
   - API responses < 200ms
   - UI interactions < 100ms
   - Search results < 300ms

### Security Requirements
1. Input Validation
   - URL validation for bookmarks
   - XSS prevention
   - SQL injection prevention

2. Data Protection
   - CSRF protection
   - Secure communication (HTTPS ready)

### Browser Support
- Modern browsers (Chrome, Firefox, Safari, Edge)
- Mobile browser compatibility
- Responsive design support

## Development Guidelines

### Code Style
1. Java
   - Google Java Style Guide
   - Clear method and variable naming
   - Comprehensive documentation

2. TypeScript
   - Airbnb TypeScript Style Guide
   - Strong typing
   - Component-based architecture

### Testing Requirements
1. Backend
   - Unit tests for services
   - Integration tests for controllers
   - Repository tests
   - Minimum 80% code coverage

2. Frontend
   - Component tests
   - Integration tests
   - E2E tests for critical paths

### Version Control
- Feature branch workflow
- Meaningful commit messages
- Code review process