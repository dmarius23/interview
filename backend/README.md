# Car Rental Management System

A comprehensive Spring Boot application for managing car rental operations, featuring companies, locations, vehicles, bookings, and payments.

## Features

- **Fleet Management**: Manage rental companies, locations, car models, and vehicles
- **Booking System**: Create and manage car reservations with availability checking
- **Payment Processing**: Integrated payment workflow with saga pattern
- **Availability Search**: Advanced search for available cars by location, model, and time period
- **Audit Trail**: Complete audit logging with correlation IDs
- **Event-Driven Architecture**: Outbox pattern for reliable event publishing

## Technology Stack

- **Framework**: Spring Boot 2.2.1
- **Database**: H2 (development), JPA/Hibernate
- **Architecture**: Domain-Driven Design with clean architecture principles
- **Mapping**: MapStruct for DTO transformations
- **Validation**: Bean Validation (JSR-303)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build**: Maven
- **Java**: 11

## Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd backend

# Build the application
mvn clean package

# Run the application
java -jar target/interview-1.0-SNAPSHOT.jar

# Or run directly with Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Database Access
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

### Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn integration-test

# Run with coverage
mvn test jacoco:report
```

## API Endpoints

### Core Entities
- **Companies**: `/api/companies` - Manage rental companies
- **Locations**: `/api/locations` - Manage rental locations
- **Car Models**: `/api/carmodels` - Manage vehicle models
- **Cars**: `/api/cars` - Manage individual vehicles
- **Bookings**: `/api/bookings` - Create and manage reservations

### Availability Search
- **By Location**: `/api/availability/models/by-location`
- **By City**: `/api/availability/models/by-city-company`
- **Cars by Model**: `/api/availability/cars/by-model-at-location`

## Architecture Highlights

### Domain-Driven Design
- Clear domain boundaries with separate modules
- Rich domain models with business logic
- Repository pattern for data access

### Event-Driven Architecture
- Outbox pattern for reliable event publishing
- Saga pattern for distributed transactions
- Asynchronous payment processing

### Error Handling
- Global exception handler with correlation IDs
- Standardized error responses
- Comprehensive logging with MDC

## Production Readiness Checklist

### Infrastructure & DevOps
- [ ] **Database Migration**: Migrate from H2 to PostgreSQL
    - Configure PostgreSQL connection properties
    - Update Hibernate dialect to PostgreSQL
    - Test all queries for PostgreSQL compatibility
- [ ] **Database Migrations**: Implement Flyway or Liquibase
    - Create migration scripts for schema versioning
    - Set up automated migration on deployment
- [ ] **Environment Configuration**: Create production application.yml
    - Externalize all configuration properties
    - Use environment variables for sensitive data
    - Configure proper logging levels and appenders
- [ ] **Container Deployment**: Docker containerization
    - Create optimized Dockerfile with multi-stage builds
    - Configure Docker Compose for local development
    - Set up Kubernetes manifests for production

### Security
- [ ] **Authentication & Authorization**: Implement Spring Security
    - JWT token-based authentication
    - Role-based access control (RBAC)
    - OAuth2/OpenID Connect integration
    - **Identity Providers**: Replace demo JWT implementation with enterprise solutions:
        - **Keycloak** - Self-hosted, full-featured identity management
        - **Auth0** - Cloud-native identity platform with easy integration
        - **AWS Cognito** - Scalable user directory and authentication service
        - **Azure AD** - Enterprise identity and access management
- [ ] **API Security**: Secure all endpoints
    - Rate limiting and throttling
    - Input validation and sanitization
    - CORS configuration
    - HTTPS enforcement

### Scalability & Performance
- [ ] **Microservices Architecture**: Split into bounded contexts
    - Fleet Management Service
    - Booking Service
    - Payment Service
    - Notification Service
- [ ] **Caching Strategy**: Implement Redis caching
    - Cache frequently accessed data (car models, locations)
    - Distributed session management
- [ ] **Database Optimization**:
    - Connection pooling (HikariCP)
    - Database indexing strategy
    - Read replicas for query scalability
    - Database partitioning for large datasets

### Monitoring & Observability
- [ ] **Application Monitoring**:
    - Micrometer + Prometheus metrics
    - Grafana dashboards
    - Custom business metrics
- [ ] **Distributed Tracing**:
    - Spring Cloud Sleuth + Zipkin
    - Correlation ID propagation across services
- [ ] **Logging**:
    - Structured logging (JSON format)
    - Centralized logging with ELK stack
    - Log aggregation and search

### Quality & Testing
- [ ] **Code Quality**: SonarQube integration
    - Static code analysis
    - Security vulnerability scanning
    - Code coverage reporting (minimum 80%)
- [ ] **Comprehensive Testing**:
    - Unit tests (95%+ coverage)
    - Integration tests with Testcontainers
    - Contract testing (Spring Cloud Contract)
    - Performance testing (JMeter/Gatling)
    - End-to-end testing

### Data & Backup
- [ ] **Data Management**:
    - Automated database backups
    - Point-in-time recovery
    - Data retention policies
    - GDPR compliance for personal data
- [ ] **Event Sourcing**: Consider for audit requirements
    - Event store for complete audit trail
    - Replay capabilities for debugging

### CI/CD Pipeline
- [ ] **Automated Pipeline**:
    - GitHub Actions/Jenkins pipeline
    - Automated testing on PR
    - Dependency vulnerability scanning
    - Automated deployment to staging/production
- [ ] **Blue-Green Deployment**: Zero-downtime deployments
- [ ] **Feature Flags**: Gradual rollout capabilities

### Business Continuity
- [ ] **High Availability**:
    - Multi-region deployment
    - Load balancing
    - Circuit breaker pattern (Hystrix/Resilience4j)
- [ ] **Disaster Recovery**:
    - Automated failover procedures
    - Regular disaster recovery testing
    - RTO/RPO targets defined

