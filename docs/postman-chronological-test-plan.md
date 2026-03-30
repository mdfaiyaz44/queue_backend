# QueueLess Chronological Postman Test Plan

This document is the strict order to test the backend in Postman.

This plan assumes:

- MySQL is the active database
- Redis is intentionally disabled for now
- Kafka is optional and can remain disabled during API testing
- the current prediction flow is the backend heuristic predictor

## 1. Before you begin

Make sure these are ready:

- Spring Boot app is running
- MySQL is running
- database is connected
- app is using port `8082`
- Swagger is reachable
- Redis is not required
- Kafka is not required

Base URL:

`http://localhost:8082`

## 2. Postman environment variables

Create these variables first:

- `baseUrl`
- `adminToken`
- `patientToken`
- `doctorToken`
- `departmentId`
- `doctorId`
- `doctorUserId`
- `appointmentId`
- `tokenId`

Set:

- `baseUrl = http://localhost:8082`

## 3. Security rule you must remember

QueueLess uses JWT authentication.

That means:

- public endpoints do not need token
- protected endpoints need `Authorization: Bearer <token>`
- patient token can only access patient endpoints
- doctor token can only access doctor endpoints
- admin token can only access admin endpoints
- some read endpoints such as doctor listing and doctor queue view are public

Recommended Postman headers for JSON requests:

- `Content-Type: application/json`
- `Authorization: Bearer <token>` only for protected endpoints

## 4. Step 1: health check

Request:

- `GET {{baseUrl}}/health`

Expected:

- `200 OK`
- response says backend is healthy

## 5. Step 2: login as admin

Use the bootstrap admin created by the app.

Default values unless you changed them:

- email: `admin@queueless.local`
- password: `Admin@12345`

Request:

- `POST {{baseUrl}}/api/auth/login`

Body:

```json
{
  "email": "admin@queueless.local",
  "password": "Admin@12345"
}
```

Expected:

- `200 OK`
- copy `data.accessToken` into `adminToken`

## 6. Step 3: create department

Request:

- `POST {{baseUrl}}/api/admin/departments`

Headers:

- `Authorization: Bearer {{adminToken}}`

Body:

```json
{
  "name": "Cardiology",
  "description": "Heart and vascular care"
}
```

Expected:

- `200 OK`
- save `data.id` as `departmentId`

## 7. Step 4: create doctor

Request:

- `POST {{baseUrl}}/api/admin/doctors`

Headers:

- `Authorization: Bearer {{adminToken}}`

Body:

```json
{
  "name": "Dr. Meera Shah",
  "email": "doctor1@queueless.local",
  "phone": "9123456789",
  "password": "Doctor@123",
  "departmentId": "{{departmentId}}",
  "qualification": "MD Cardiology",
  "licenseNumber": "DOC-CARD-001",
  "experienceYears": 12,
  "averageConsultationMinutes": 15,
  "consultationBufferMinutes": 5
}
```

Expected:

- `200 OK`
- save `data.id` as `doctorId`
- save `data.userId` as `doctorUserId`

## 8. Step 5: add doctor availability

Request:

- `POST {{baseUrl}}/api/admin/doctors/{{doctorId}}/availability`

Headers:

- `Authorization: Bearer {{adminToken}}`

Body:

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "13:00:00",
  "slotDurationMinutes": 15,
  "maxPatients": 16
}
```

Expected:

- `200 OK`

## 9. Step 6: register patient

Request:

- `POST {{baseUrl}}/api/auth/register`

Body:

```json
{
  "name": "Patient One",
  "email": "patient1@queueless.local",
  "phone": "9876543210",
  "password": "Patient@123"
}
```

Expected:

- `200 OK`

## 10. Step 7: login as patient

Request:

- `POST {{baseUrl}}/api/auth/login`

Body:

```json
{
  "email": "patient1@queueless.local",
  "password": "Patient@123"
}
```

Expected:

- `200 OK`
- save `data.accessToken` as `patientToken`

## 11. Step 8: login as doctor

Request:

- `POST {{baseUrl}}/api/auth/login`

Body:

```json
{
  "email": "doctor1@queueless.local",
  "password": "Doctor@123"
}
```

Expected:

- `200 OK`
- save `data.accessToken` as `doctorToken`

## 12. Step 9: list departments

Request:

- `GET {{baseUrl}}/api/departments`

Expected:

- `200 OK`
- department list includes Cardiology

## 13. Step 10: list doctors

Request:

- `GET {{baseUrl}}/api/doctors`

Expected:

- `200 OK`
- doctor list includes the doctor you created

## 14. Step 11: book appointment

Choose a date that matches the doctor availability day.
For the sample below, `2026-03-16` is a Monday.

Request:

- `POST {{baseUrl}}/api/patient/appointments`

Headers:

- `Authorization: Bearer {{patientToken}}`

Body:

```json
{
  "doctorId": "{{doctorId}}",
  "appointmentDate": "2026-03-16",
  "appointmentTime": "10:00:00",
  "priority": "NORMAL",
  "symptoms": "Chest discomfort",
  "notes": "First visit"
}
```

Expected:

- `200 OK`
- save `data.appointmentId` as `appointmentId`
- save `data.tokenId` as `tokenId`

## 15. Step 12: patient appointment list

Request:

- `GET {{baseUrl}}/api/patient/appointments`

Headers:

- `Authorization: Bearer {{patientToken}}`

Expected:

- `200 OK`
- new appointment appears in list

## 16. Step 13: patient check-in

Request:

- `POST {{baseUrl}}/api/patient/appointments/{{appointmentId}}/check-in`

Headers:

- `Authorization: Bearer {{patientToken}}`

Expected:

- `200 OK`
- status becomes `CHECKED_IN`

## 17. Step 14: patient queue tracking

Request:

- `GET {{baseUrl}}/api/queue/patient/{{tokenId}}`

Headers:

- `Authorization: Bearer {{patientToken}}`

Expected:

- `200 OK`
- queue token data is returned

## 18. Step 15: doctor queue view

Request:

- `GET {{baseUrl}}/api/queue/doctor/{{doctorId}}?date=2026-03-16`

Expected:

- `200 OK`
- queue shows the patient token

Note:

- this endpoint is currently public, so no JWT is required

## 19. Step 16: doctor starts consultation

Request:

- `POST {{baseUrl}}/api/doctor/queue/appointments/{{appointmentId}}/start`

Headers:

- `Authorization: Bearer {{doctorToken}}`

Expected:

- `200 OK`
- token status becomes `IN_PROGRESS`

## 20. Step 17: doctor completes consultation

Request:

- `POST {{baseUrl}}/api/doctor/queue/appointments/{{appointmentId}}/complete`

Headers:

- `Authorization: Bearer {{doctorToken}}`

Expected:

- `200 OK`
- token status becomes `DONE`

## 21. Step 18: patient notifications

Request:

- `GET {{baseUrl}}/api/notifications`

Headers:

- `Authorization: Bearer {{patientToken}}`

Expected:

- `200 OK`
- notification list includes booking, check-in, and queue lifecycle messages

## 22. Step 19: real-time notification stream

Request:

- `GET {{baseUrl}}/api/notifications/stream`

Headers:

- `Authorization: Bearer {{patientToken}}`

Expected:

- connection stays open
- first event is `connected`
- when you trigger a new appointment or queue event for that same user, a `notification` event should arrive

Tip:

- if Postman does not display the live stream clearly, test this endpoint with a browser-based SSE client or `curl`

## 23. Step 20: admin notifications

Request:

- `GET {{baseUrl}}/api/admin/notifications`

Headers:

- `Authorization: Bearer {{adminToken}}`

Expected:

- `200 OK`

## 24. Step 21: prediction endpoint

Request:

- `GET {{baseUrl}}/api/predictions/doctors/{{doctorId}}/wait-time?appointmentDate=2026-03-16&appointmentTime=10:00:00&priority=NORMAL`

Expected:

- `200 OK`
- predicted wait information is returned

## 25. Step 22: analytics overview

Request:

- `GET {{baseUrl}}/api/admin/analytics/overview?date=2026-03-16`

Headers:

- `Authorization: Bearer {{adminToken}}`

Expected:

- `200 OK`
- counts and department metrics are returned

## 26. Security negative tests

Run these to validate access control:

- call an admin endpoint with patient token and expect `403`
- call a patient endpoint with doctor token and expect `403`
- call a protected endpoint without token and expect `401` or `403`
- call `/api/auth/me` with a valid token and expect current user

## 27. Optional cancel flow test

Create a second appointment and test:

- `PUT {{baseUrl}}/api/patient/appointments/{{appointmentId}}/cancel`

Expected:

- appointment becomes `CANCELLED`
- token becomes `CANCELLED`
- notification is created

## 28. Optional email notification test

Enable these environment variables before starting the app:

- `NOTIFICATIONS_EMAIL_ENABLED=true`
- `MAIL_HOST=<your smtp host>`
- `MAIL_PORT=<your smtp port>`
- `MAIL_USERNAME=<your smtp username>`
- `MAIL_PASSWORD=<your smtp password>`
- `NOTIFICATIONS_EMAIL_FROM=<verified sender email>`

Expected:

- notification-triggering actions create both `IN_APP` and `EMAIL` notification records
- email records should show `SENT` if SMTP works
- email records should show `FAILED` if SMTP credentials are wrong or the mail server rejects the message

## 29. If Redis is enabled

Skip this section for now.

Enable:

- `REDIS_ENABLED=true`

Expected extra behavior:

- doctor queue reads should use cached snapshots
- queue-changing actions should refresh cache

## 30. If Kafka is enabled

Skip this section unless you are explicitly validating Kafka.

Enable:

- `KAFKA_ENABLED=true`

Expected extra behavior:

- queue lifecycle events should be published to `queue.events`
- app should still work even if event publishing fails

## 31. Improvement room still left

These parts are still open for future upgrades:

- real email and SMS delivery
- WebSocket live push updates
- external AI microservice
- stronger analytics dashboards
- appointment reschedule flow

## 32. Recommended testing mode for now

Use this stable mode while you test:

- MySQL enabled
- Redis disabled
- Kafka disabled
- AI feature flag optional

In practice, your default config already behaves this way unless you manually turn the optional features on.
