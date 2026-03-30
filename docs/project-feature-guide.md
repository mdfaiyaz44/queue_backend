# QueueLess Feature Guide

## 1. What QueueLess does

QueueLess is a smart hospital queue and appointment backend.

Its main goal is to reduce waiting time and confusion inside hospitals by doing these things:

- let patients register and log in
- let hospitals create departments and doctors
- let admins define doctor working hours
- let patients book appointments
- generate a queue token for each appointment
- show live queue position
- let doctors move the queue forward
- estimate waiting time
- give admins a daily operational overview

In simple words, this project turns a manual hospital queue into a digital, trackable system.

## 2. What is already implemented

### Authentication and security

- patient registration
- login with JWT token
- role-based access for `PATIENT`, `DOCTOR`, and `ADMIN`
- password hashing with BCrypt
- protected endpoints using Spring Security

### Hospital structure

- department creation
- doctor creation
- doctor profile with qualification, license number, and average consultation time
- doctor weekly availability schedule

### Appointment and queue flow

- patient can book appointment
- appointment creates a queue token
- patient can check in
- patient can cancel appointment
- doctor can start consultation
- doctor can complete consultation
- doctor can skip no-show patients
- patient can see token status and queue position

### Smart queue behavior

- emergency and high-priority patients are handled earlier
- predicted waiting time is generated
- recommended arrival time is generated
- doctor queue view shows waiting, active, and completed state

### Admin visibility

- analytics overview for doctors, appointments, tokens, and department load

### System support

- Swagger/OpenAPI docs
- health endpoint
- Docker support
- MySQL runtime setup
- in-app notification history
- Kafka-ready event publishing layer
- Redis-ready queue cache layer, currently disabled for main testing
- automated test coverage for main startup and auth flow

## 3. What is not fully implemented yet

These are the main features still left if you want a more complete real-world system:

### Notifications

Implemented now:

- in-app notification records for appointment confirmation
- check-in confirmation notification
- queue start, completion, and skip notifications
- real-time in-app notification stream using SSE
- optional SMTP email delivery with feature flag

Still pending:

- SMS reminder
- turn approaching notification

### Real-time live updates

Partially implemented now:

- live notification push is available through SSE

Still pending:

- live queue push updates for the queue board itself
- full frontend subscription flow

### Redis caching

Redis support is prepared, but it is intentionally paused for now.

The code includes:

- a feature toggle
- a queue cache service
- cache invalidation hooks on queue changes

For your current testing path, Redis is disabled so MySQL remains the single source of truth.

### Kafka event streaming

Kafka support is partially implemented.

Current status:

- queue event payload model exists
- Kafka producer wiring exists
- publishing is feature-flag controlled

Still pending:

- consumer workflows
- retry/dead-letter strategy
- analytics and notification consumers

### AI microservice integration

Right now the prediction logic is rule-based inside Spring Boot.
The separate FastAPI AI service is not yet connected.

### Advanced hospital features

Still left if you want to grow this system:

- multi-hospital support
- doctor leave and holiday calendar
- reschedule appointment flow
- patient medical history integration
- receptionist or staff role
- no-show analytics
- payment integration
- audit trail screen

## 4. What Redis does here

Redis is an in-memory data store.

In QueueLess, Redis is useful for fast-changing queue data that should be read quickly many times.

Good use cases here:

- store live queue snapshot for each doctor
- store current queue position counters
- cache doctor list and department list
- store temporary reminder jobs
- reduce repeated database reads

Why Redis matters:

- faster than reading MySQL every time
- useful for real-time queue screen
- useful when many patients refresh the queue page at once

Current status:

- dependency is included
- feature toggle exists
- cache service exists
- disabled for the current stable MySQL-first testing setup

## 5. What Kafka does here

Kafka is an event streaming system.

In QueueLess, Kafka is useful when one action should trigger many other background actions.

Example:

1. patient books appointment
2. backend emits event
3. notification service sends message
4. analytics service records it
5. AI feature pipeline stores it

Why Kafka matters:

- separates core booking flow from background processing
- improves scalability
- helps build notifications, analytics, and AI pipelines cleanly

Current status:

- dependency is included
- event publisher abstraction exists
- producer wiring exists
- full event-driven workflow is still pending

## 6. What Docker does here

Docker packages the backend and its dependencies into containers.

In QueueLess, Docker helps in these ways:

- same setup on every machine
- easier local development
- easier deployment to cloud
- easier team collaboration

`Dockerfile`:

- builds the Spring Boot app
- creates a runnable container image

`docker-compose.yml`:

- starts app container
- starts MySQL
- starts Redis
- starts Kafka

Why Docker matters:

- no manual installation mismatch
- faster project setup
- better for deployment later

## 7. What MySQL does here

MySQL is the main persistent database.

It stores:

- users
- departments
- doctors
- doctor availability
- appointments
- queue tokens

Why MySQL matters:

- permanent storage
- relational consistency
- transaction safety
- suitable for appointment and queue operations

## 8. What the AI prediction part does here

The current prediction feature estimates waiting time using backend rules like:

- queue size
- emergency patients ahead
- doctor average consultation time
- time-of-day penalty

It also gives:

- predicted wait minutes
- estimated consultation start
- recommended arrival time

Why this matters:

- patient knows when to arrive
- hospital reduces crowded waiting areas

Current status:

- working rule-based predictor is implemented
- real machine learning service is still pending

## 9. What each main backend module does

### Auth module

Manages registration, login, JWT token creation, and current user lookup.

### Department module

Lets admins create hospital departments.

### Doctor module

Lets admins create doctor accounts and define doctor schedules.

### Appointment module

Lets patients book, cancel, and check in for appointments.

### Queue module

Creates and manages tokens and lets doctors move patients through the queue.

### Prediction module

Estimates waiting time and arrival suggestions using the current backend heuristic engine.

### Analytics module

Shows admin-level operational summary for the day.

## 10. Suggested next features

If I continue building this project, these are the features I would add next:

1. notification service with email/SMS delivery
2. WebSocket live queue updates
3. Redis runtime activation after API testing is complete
4. Kafka consumer integration
5. appointment reschedule flow
6. doctor leave management
7. dashboard-ready analytics endpoints
8. FastAPI AI service integration
9. audit trail and activity history
10. multi-hospital support

## 11. Final summary

This backend is already a strong foundation for a real hospital queue system.

It already supports the main queue lifecycle end to end on a MySQL-backed backend.
What is left now is mostly the advanced production layer:

- SMS delivery
- real-time push updates
- Redis activation
- Kafka consumers and background processing
- external AI service
- more advanced hospital operations
