# QueueLess Frontend Integration Guide

This document explains how the React frontend connects to the QueueLess Spring Boot backend.

## 1. Final architecture

- Frontend: React + Vite + TypeScript
- Backend: Spring Boot + JWT + MySQL
- Database: MySQL
- Notifications: backend notification APIs plus SSE realtime stream

Frontend default URL:

- `http://localhost:5173`

Backend default URL:

- `http://localhost:8082`

## 2. Backend requirements before frontend start

Make sure these are true first:

- MySQL is running
- QueueLess backend starts successfully
- backend health endpoint works
- JWT login works
- database schema is already created by Hibernate

Backend quick check:

```bash
mvn spring-boot:run
```

Health check:

- `GET http://localhost:8082/health`

## 3. Backend changes already made for frontend support

These frontend-related backend adjustments are already in place:

- CORS support for the frontend origin
- allowed frontend origin defaults to `http://localhost:5173`
- `Authorization` header is exposed
- credentials support is enabled
- deprecated Kafka serializer class usage was removed from config

Relevant backend files:

- [SecurityConfig.java](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/src/main/java/com/queueless/queueless/config/SecurityConfig.java)
- [KafkaConfig.java](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/src/main/java/com/queueless/queueless/config/KafkaConfig.java)
- [application.properties](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/src/main/resources/application.properties)

## 4. Frontend environment setup

Inside the `frontend` folder, create `.env` only if you need a custom backend URL.

Example:

```env
VITE_API_BASE_URL=http://localhost:8082
```

Example file already provided:

- [frontend/.env.example](/c:/Users/arish/OneDrive/Desktop/Final_year_project/queueless/frontend/.env.example)

## 5. Frontend run steps

From the `frontend` folder:

```bash
npm install
npm run dev
```

Open:

- `http://localhost:5173`

Production build:

```bash
npm run build
```

## 6. JWT integration flow

The frontend follows this flow:

1. user logs in from `/login`
2. backend returns `accessToken`
3. token is stored in local storage
4. Axios automatically sends `Authorization: Bearer <token>`
5. frontend fetches `/api/auth/me`
6. dashboard changes based on role:
   - `PATIENT`
   - `DOCTOR`
   - `ADMIN`

## 7. Frontend modules and mapped backend APIs

### Landing page

- marketing only
- no auth required

### Authentication

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### Patient dashboard

- `GET /api/doctors`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments`
- `POST /api/patient/appointments/{appointmentId}/check-in`
- `PUT /api/patient/appointments/{appointmentId}/cancel`
- `GET /api/queue/patient/{tokenId}`
- `GET /api/predictions/doctors/{doctorId}/wait-time`
- `GET /api/notifications`
- `GET /api/notifications/stream`

### Doctor dashboard

- `GET /api/doctors`
- `GET /api/queue/doctor/{doctorId}`
- `POST /api/doctor/queue/appointments/{appointmentId}/start`
- `POST /api/doctor/queue/appointments/{appointmentId}/complete`
- `POST /api/doctor/queue/appointments/{appointmentId}/skip`
- `GET /api/notifications`
- `GET /api/notifications/stream`

### Admin dashboard

- `GET /api/departments`
- `POST /api/admin/departments`
- `GET /api/doctors`
- `POST /api/admin/doctors`
- `POST /api/admin/doctors/{doctorId}/availability`
- `GET /api/admin/analytics/overview`
- `GET /api/admin/notifications`

## 8. Realtime notification integration

Frontend realtime notifications use:

- `GET /api/notifications/stream`

Important detail:

- browser native `EventSource` does not support custom auth headers properly for this use case
- frontend uses `@microsoft/fetch-event-source`
- JWT token is sent in the `Authorization` header

## 9. Security notes

- do not hardcode MySQL passwords inside tracked frontend or backend files
- use local env files for real secrets
- `.gitignore` already excludes local secret patterns
- frontend stores JWT locally for now
- if you later want harder browser security, move to secure cookie auth design

## 10. Full-stack local startup order

Use this order every time:

1. start MySQL
2. start backend from project root
3. verify `http://localhost:8082/health`
4. start frontend from `frontend`
5. open `http://localhost:5173`
6. login with a valid patient, doctor, or admin account

## 11. Manual frontend verification checklist

- landing page loads without console errors
- login works
- register works for patient
- dashboard route is protected
- token persists across refresh
- patient can book appointment
- prediction panel updates
- patient notifications panel loads
- doctor queue actions work
- admin can create department
- admin can create doctor
- admin can add availability
- analytics panel loads

## 12. Room for future improvement

- split the 3D scene further if you want smaller optional visual bundles
- add charts for analytics
- add WebSocket queue screens for richer live updates
- add SMS delivery and external AI microservice status panels
- add role-specific routing depth instead of one shared dashboard page
