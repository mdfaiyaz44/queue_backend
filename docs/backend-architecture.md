# QueueLess Backend Architecture

## 1. High-level design

QueueLess uses a modular monolith backend for the first production-ready version. This keeps the code simple to operate while still preparing the system for future scaling.

Flow:

1. React frontend calls REST APIs.
2. Spring Boot handles authentication, business rules, validation, queue logic, and analytics.
3. MySQL stores transactional data.
4. Redis is planned for hot queue snapshots and short-lived cache.
5. Kafka is planned for queue events and notification pipelines.
6. A future FastAPI service can consume queue features and return stronger AI predictions.

## 2. Current modules

- `auth`: registration, login, current-user lookup, JWT generation
- `department`: admin-managed hospital departments
- `doctor`: doctor profile and availability schedule
- `appointment`: patient booking, cancellation, and check-in
- `queue`: token lifecycle and live queue tracking
- `prediction`: rule-based wait prediction with AI-ready contract
- `analytics`: daily operational summaries for admins

## 3. Security flow

1. User logs in with email and password.
2. Spring Security authenticates using `DaoAuthenticationProvider`.
3. QueueLess generates a JWT with `userId` and `role` claims.
4. The client sends `Authorization: Bearer <token>` on protected APIs.
5. `JwtAuthenticationFilter` reads the token and loads the user context.
6. Role checks are enforced with `@PreAuthorize`.

## 4. Core data model

- `users`: shared identity table for patients, doctors, and admins
- `departments`: hospital departments
- `doctors`: medical profile linked to a user and department
- `doctor_availability`: weekly schedule by doctor
- `appointments`: patient bookings
- `queue_tokens`: live queue token for each appointment

## 5. Redis design

Redis is included as a planned runtime dependency but not forced for local startup.

Recommended Redis usage:

- Cache queue snapshots by `doctorId + date`
- Cache doctor list and department list
- Store short-lived notification reminder jobs
- Store real-time queue position counters for fast reads

Suggested keys:

- `queue:doctor:{doctorId}:{date}`
- `doctor:{doctorId}`
- `departments:all`

## 6. Kafka design

Kafka is represented today by an event publisher abstraction and logs. This keeps the application ready for event-driven expansion without blocking local development.

Recommended event topics:

- `queue.token.created`
- `queue.token.started`
- `queue.token.completed`
- `queue.token.skipped`
- `appointment.created`
- `notification.requested`

Consumers you can add later:

- notification service
- analytics stream processor
- audit/event history service
- AI feature builder

## 7. AI prediction pipeline

Current implementation:

- Rule-based heuristic using queue length, emergency load, average consultation time, and time-of-day penalty

Future AI pipeline:

1. Emit queue and appointment events to Kafka
2. Build feature tables in a warehouse or feature store
3. Train a regression model in Python FastAPI service
4. Expose `/predict` from the AI service
5. Spring Boot calls the AI service and stores prediction results
6. Fall back to the heuristic model if AI is unavailable

Recommended features:

- queue length
- emergency cases ahead
- doctor average consultation time
- historical delay by doctor
- day of week
- time slot
- patient check-in lag

## 8. Deployment architecture

Recommended first deployment:

- Spring Boot app in Docker
- MySQL in managed cloud database
- Redis in managed cache
- Kafka in managed event streaming service
- Nginx or API gateway in front

Cloud-ready layout:

1. Frontend on Vercel or Netlify
2. Backend container on AWS ECS, Azure Container Apps, or Render
3. MySQL on RDS, Azure Database for MySQL, or PlanetScale
4. Redis on ElastiCache or Redis Cloud
5. Kafka on Confluent Cloud or MSK
6. AI service as separate FastAPI container

## 9. Non-functional requirements

- Availability: target 99.9% for core APIs
- Security: JWT auth, password hashing, role-based access
- Performance: queue lookup should stay low-latency under normal load
- Reliability: queue state changes must be transactional
- Maintainability: clear service boundaries and DTO-based API layer
- Observability: logs, health checks, and metrics endpoints

## 10. Scalability design

Short-term scale:

- horizontal scaling of stateless backend instances
- move repeated queue reads to Redis
- add database indexes for appointment and queue queries

Medium-term scale:

- stream queue events to Kafka
- async notifications
- read replicas for analytics-heavy workloads

Long-term scale:

- split notification, analytics, and AI into separate services
- use WebSocket gateway for live queue updates
- add multi-hospital tenant partitioning
