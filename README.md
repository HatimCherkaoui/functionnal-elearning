# E-Learning Platform

A full-featured online learning management system built with Spring Boot. Students can enroll in courses, track their progress, take quizzes, and earn certificates. Instructors can create and manage courses, while administrators oversee the entire platform.

## Features

- **User Management** - Role-based authentication (Admin, Instructor, Student)
- **Course Management** - Create, publish, and organize courses with lessons
- **Enrollment System** - Students can enroll in courses and track progress
- **Quizzes & Assessments** - Interactive learning with instant feedback
- **Certificates** - Automatically generated upon course completion
- **Analytics Dashboard** - Track engagement and performance metrics
- **Real-time Notifications** - Kafka-based messaging for course updates
- **Instructor Approval** - Admin workflow for vetting new instructors

## Tech Stack

- **Backend**: Spring Boot 4.0.3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3
- **Database**: MySQL 8.0 with Liquibase migrations
- **Messaging**: Apache Kafka
- **Build Tool**: Gradle
- **Containerization**: Docker & Docker Compose

## Quick Start

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose

### Running the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd functionnal
```

2. Build and start the services:
```bash
./gradlew clean bootJar
docker compose up -d
```

3. Access the application at `http://localhost:8080`

### Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@elearning.com | admin123 |
| Instructor | john.instructor@elearning.com | john123 |
| Student | alice@elearning.com | alice123 |

## Project Structure

```
src/
├── main/
│   ├── java/org/eckmo/functionnal/
│   │   ├── config/          # Security, Kafka configuration
│   │   ├── controller/      # REST and web controllers
│   │   ├── service/         # Business logic layer
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # JPA entities
│   │   └── dto/             # Data transfer objects
│   └── resources/
│       ├── db/changelog/    # Liquibase migrations
│       ├── templates/       # Thymeleaf views
│       └── static/          # CSS, JS, images
└── test/
    └── java/                # Integration tests
```

## Database Schema

The application uses MySQL with the following main entities:

- **Users** - Student, instructor, and admin accounts
- **Courses** - Course information and metadata
- **Lessons** - Individual course content
- **Enrollments** - Student-course relationships
- **Certificates** - Completion certificates
- **Notifications** - User notifications
- **Subscriptions** - Course subscription tracking
- **Instructor Documents** - Document verification for instructors

Migrations are managed with Liquibase and run automatically on startup.

## API Endpoints

### Authentication
- `POST /login` - User login
- `POST /register` - New user registration
- `GET /logout` - User logout

### Users
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Courses
- `GET /api/courses` - List all courses
- `POST /api/courses` - Create new course
- `PUT /api/courses/{id}` - Update course
- `POST /api/courses/{id}/publish` - Publish a course

### Enrollments
- `GET /api/enrollments` - List enrollments
- `POST /api/enrollments` - Enroll in a course
- `PUT /api/enrollments/{id}/complete` - Mark course as completed

See the controllers for the complete API documentation.

## Development

### Running Tests

```bash
./gradlew test
```

The integration tests use Testcontainers to spin up MySQL and Kafka instances automatically.

### Building for Production

```bash
./gradlew clean build
```

### Environment Variables

The application can be configured using environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/elearning_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Kafka Topics

The application uses Kafka for asynchronous messaging:

- `course.published` - New course published
- `course.updated` - Course information updated
- `user.registered` - New user registration
- `enrollment.created` - Student enrolled in course

Messages are consumed by `KafkaConsumerService` for logging and potential future integrations.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License.

## Acknowledgments

Built as a demonstration of modern Spring Boot application architecture with microservices patterns.

