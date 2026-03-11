# NovaCore - Enterprise Java Backend Base

Professional Java backend base project using Spring Boot 3.x with **domain-based architecture**.

## 🏗️ Architecture

This project follows a **domain-based architecture** with clear separation of concerns:

```
com.novacore/
├── config/                 # System configuration
│   ├── WebConfig.java
│   ├── AsyncConfig.java
│   └── properties/
│
├── shared/                 # Shared components across system
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BaseException.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── SystemException.java
│   ├── response/
│   │   ├── ApiResponse.java
│   │   └── ApiResponseBuilder.java
│   ├── constants/
│   │   ├── ApiConstants.java
│   │   ├── ErrorCode.java
│   │   ├── Channel.java
│   │   ├── MdcConstants.java
│   │   └── RequestHeaderConstants.java
│   ├── util/
│   │   └── LocaleUtils.java
│   └── context/
│       ├── RequestContext.java
│       ├── RequestContextHolder.java
│       ├── RequestContextFilter.java
│       └── RequestContextTaskDecorator.java
│
├── infrastructure/         # Technical infrastructure
│   └── persistence/
│       └── BaseEntity.java
│
├── user/                   # ===== DOMAIN: USER =====
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── UserServiceImpl.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── domain/
│   │   └── User.java
│   ├── dto/
│   │   ├── UserDto.java
│   │   ├── CreateUserRequest.java
│   │   └── UpdateUserRequest.java
│   └── mapper/
│       └── UserMapper.java
│
└── health/
    └── controller/
        └── HealthController.java
```

## 📋 Features

- ✅ **Spring Boot 3.x** with Java 17
- ✅ **Domain-Based Architecture** - Clear domain boundaries
- ✅ **DTO Pattern** - Entities never exposed to controllers
- ✅ **Service Interface + Implementation** pattern
- ✅ **Global Exception Handling** with standardized error responses
- ✅ **API Response Wrapper** for consistent responses
- ✅ **Validation** using Bean Validation
- ✅ **Request Context** - Thread-local context with trace/request IDs
- ✅ **Profile-based Configuration** (dev, prod)
- ✅ **PostgreSQL** with Flyway migrations
- ✅ **Redis** for caching
- ✅ **Kafka** for messaging
- ✅ **Docker** & Docker Compose

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for infrastructure services)

### Running Infrastructure Services

```bash
# Start PostgreSQL, Redis, Kafka
docker-compose -f novacore/docker-compose.yml --profile infra up -d
```

### Running the Application

```bash
# Development profile (default)
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Build

```bash
# Compile
mvn clean compile

# Package
mvn clean package

# Run JAR
java -jar target/novacore-1.0.0.jar
```

## 📡 API Endpoints

### Health Check
```
GET /api/v1/health
```

### User Management
```
POST   /api/v1/users          # Create user
GET    /api/v1/users/{id}      # Get user by ID
GET    /api/v1/users           # Get all users
PUT    /api/v1/users/{id}      # Update user
DELETE /api/v1/users/{id}      # Delete user
```

### Example Request

**Create User:**
```bash
curl -X POST http://localhost:5001/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "phone": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "code": "SUCCESS_200_OK",
  "message": "User created successfully",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "phone": "+1234567890",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00",
  "requestId": "uuid-here",
  "path": "/api/v1/users"
}
```

## 🗄️ Database

### Development
- PostgreSQL (via Docker Compose)
- Flyway migrations in `src/main/resources/db/migration/`
- Auto-migration on startup

### Production
- Configure in `application-prod.yml`
- Update datasource URL, username, password
- Use environment variables for sensitive data

## 📁 Project Structure

```
novacore/
├── pom.xml
├── README.md
├── .gitignore
├── docker/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   └── env.example
├── infra/
│   └── docker/
│       └── databases/
│           ├── postgres-init.sh
│           └── redis.conf
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── novacore/
    │   │           ├── NovaCoreApplication.java
    │   │           ├── config/          # System configuration
    │   │           ├── shared/           # Shared components
    │   │           ├── infrastructure/   # Technical infrastructure
    │   │           ├── user/             # User domain
    │   │           └── health/            # Health check
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/
    │           └── migration/            # Flyway migrations
    └── test/
        └── java/
```

## 🔧 Configuration

### Profiles

- **default/dev**: Development settings with detailed logging
- **prod**: Production settings with minimal logging

### Environment Variables (Production)

```bash
DB_URL=jdbc:postgresql://localhost:5432/novacore
DB_USERNAME=novacore
DB_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
PORT=5001
```

## 🎯 Best Practices Implemented

1. **Domain-Based Architecture**: Clear domain boundaries, easy to scale
2. **Separation of Concerns**: Clear layer boundaries
3. **No Business Logic in Controllers**: All logic in service layer
4. **DTO Pattern**: Entities never exposed to API
5. **Service Interface Pattern**: Easy to mock and test
6. **Global Exception Handling**: Consistent error responses
7. **Validation**: Input validation at controller level
8. **Transaction Management**: `@Transactional` in service layer
9. **Request Context**: Thread-local context with trace IDs
10. **Logging**: Structured logging with SLF4J and MDC
11. **Configuration**: Profile-based configuration
12. **Clean Code**: Meaningful names, comments, and structure

## 🏛️ Architecture Principles

### Domain-Based Structure
- Each domain (user, auth, etc.) is self-contained
- Domain contains: controller, service, repository, domain, dto, mapper
- Easy to add new domains without affecting existing ones

### Shared Components
- `shared/` contains components used across all domains
- Exception handling, response wrappers, constants, utilities
- Request context for distributed tracing

### Infrastructure
- Technical concerns separated from business logic
- Base entities, persistence, messaging, caching
- Easy to swap implementations

## 🔮 Future Extensions

This base is ready for:
- ✅ Redis (caching) - Configured
- ✅ Kafka (messaging) - Configured
- ✅ Spring Security + JWT
- ✅ WebSocket
- ✅ Database migrations (Flyway) - Configured
- ✅ Docker & Docker Compose - Configured
- ✅ Unit & Integration Tests
- ✅ API Documentation (Swagger/OpenAPI)

## 📝 License

MIT License
