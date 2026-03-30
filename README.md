# QueueLess Platform

QueueLess is a smart hospital queue and appointment platform. It includes a Spring Boot backend and a React frontend for patients, doctors, and hospital administrators.

The current stable testing mode is:

- MySQL enabled
- Redis disabled
- Kafka disabled unless explicitly being validated

## What is implemented

- JWT authentication with patient, doctor, and admin roles
- Bootstrap admin creation on first startup
- Department management
- Doctor profile and schedule management
- Patient appointment booking and check-in
- Token-based queue flow with live queue position
- Emergency-aware wait time prediction
- Admin analytics overview
- Health endpoint, logs, and OpenAPI support
- Docker and docker-compose setup
- MySQL-first runtime configuration with H2-backed automated tests
- React frontend with role-aware dashboards
- motion-based landing page and lazy-loaded 3D hero
- frontend integration layer for auth, appointments, queue, analytics, and notifications

## Frontend

Frontend stack:

- React
- Vite
- TypeScript
- Tailwind CSS
- Framer Motion
- Three.js

Frontend folder:

- [frontend](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/frontend)

Frontend docs:

- [frontend/README.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/frontend/README.md)
- [frontend-integration.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/frontend-integration.md)

## Unique backend features

- Recommended arrival time is calculated for every appointment
- Queue order respects emergency and high-priority patients
- Doctors can start, complete, or skip consultations
- Admins get a daily operational overview
- Redis, Kafka, and AI integrations are designed as feature-toggle-ready extensions
- In-app notifications are stored for appointment and queue lifecycle events
- Real-time in-app notifications are available through an SSE stream endpoint
- Optional SMTP email delivery can be enabled with environment variables

## Main API groups

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/departments`
- `POST /api/admin/departments`
- `GET /api/doctors`
- `GET /api/doctors/{doctorId}`
- `POST /api/admin/doctors`
- `POST /api/admin/doctors/{doctorId}/availability`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments`
- `POST /api/patient/appointments/{appointmentId}/check-in`
- `PUT /api/patient/appointments/{appointmentId}/cancel`
- `GET /api/queue/doctor/{doctorId}`
- `GET /api/queue/patient/{tokenId}`
- `POST /api/doctor/queue/appointments/{appointmentId}/start`
- `POST /api/doctor/queue/appointments/{appointmentId}/complete`
- `POST /api/doctor/queue/appointments/{appointmentId}/skip`
- `GET /api/notifications`
- `GET /api/notifications/stream`
- `GET /api/admin/notifications`
- `GET /api/predictions/doctors/{doctorId}/wait-time`
- `GET /api/admin/analytics/overview`

## Default run

1. Make sure MySQL is running and the database exists.
2. Run `mvn spring-boot:run`.
3. Open Swagger UI at `http://localhost:8082/swagger-ui/index.html`.

## Real-time notifications

QueueLess now supports live in-app notifications over Server-Sent Events.

- Endpoint: `GET /api/notifications/stream`
- Auth: patient, doctor, or admin JWT required
- Purpose: push new notification events without polling

SMTP email delivery is also supported when configured. Keep `NOTIFICATIONS_EMAIL_ENABLED=false` if you only want in-app real-time alerts.

Recommended Gmail SMTP setup:

- `MAIL_HOST=smtp.gmail.com`
- `MAIL_PORT=587`
- `MAIL_PROTOCOL=smtp`
- `MAIL_SMTP_AUTH=true`
- `MAIL_SMTP_STARTTLS_ENABLE=true`
- `MAIL_SMTP_STARTTLS_REQUIRED=true`
- `MAIL_SMTP_SSL_ENABLE=false`
- `MAIL_SMTP_SSL_TRUST=*`
- `MAIL_SMTP_SSL_PROTOCOLS=TLSv1.2`

## MySQL setup

1. Create the database:
```sql
CREATE DATABASE queueless_db;
```
2. Set MySQL connection variables if your setup is different from the defaults:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/queueless_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="your_mysql_username"
$env:DB_PASSWORD="your_mysql_password"
```
3. Start the app:
```powershell
mvn spring-boot:run
```

## Optional H2 fallback

If you ever want the old local H2 mode again:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
mvn spring-boot:run
```

## Docker run

1. Copy `.env.example` to `.env`.
2. Run `docker compose up --build`.
3. The app will be available at `http://localhost:8082`.

## Documentation map

- [backend-architecture.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/backend-architecture.md)
- [project-feature-guide.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/project-feature-guide.md)
- [postman-guide.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/postman-guide.md)
- [postman-chronological-test-plan.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/postman-chronological-test-plan.md)
- [frontend-integration.md](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/docs/frontend-integration.md)

## Default bootstrap admin

- Email: value of `BOOTSTRAP_ADMIN_EMAIL`
- Password: value of `BOOTSTRAP_ADMIN_PASSWORD`

## Testing

- Run `mvn clean test`
- Follow the chronological Postman flow in `docs/postman-chronological-test-plan.md`

## Documentation

- [Backend architecture](docs/backend-architecture.md)
- [Postman testing guide](docs/postman-guide.md)
- [Feature guide](docs/project-feature-guide.md)
- [Chronological Postman test plan](docs/postman-chronological-test-plan.md)
