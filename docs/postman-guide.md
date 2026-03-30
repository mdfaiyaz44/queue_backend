# Postman Testing Guide

Use [postman-chronological-test-plan.md](postman-chronological-test-plan.md) as the main testing document.
This file is the short version.

## 1. Health check

- `GET /health`

Expected:

- `success = true`
- `data = "UP"`

## 2. Register patient

- `POST /api/auth/register`

Body:

```json
{
  "name": "Patient One",
  "email": "patient1@example.com",
  "phone": "9876543210",
  "password": "Patient@123"
}
```

## 3. Login

- `POST /api/auth/login`

Body:

```json
{
  "email": "patient1@example.com",
  "password": "Patient@123"
}
```

Save `data.accessToken` as a Postman variable.

## 4. Login as admin

Use the bootstrap admin credentials from your `.env`.

## 5. Create department

- `POST /api/admin/departments`
- Header: `Authorization: Bearer {{adminToken}}`

```json
{
  "name": "Cardiology",
  "description": "Heart and vascular care"
}
```

## 6. Create doctor

- `POST /api/admin/doctors`
- Header: `Authorization: Bearer {{adminToken}}`

```json
{
  "name": "Dr. Meera Shah",
  "email": "meera.shah@hospital.com",
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

## 7. Add doctor availability

- `POST /api/admin/doctors/{{doctorId}}/availability`

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "13:00:00",
  "slotDurationMinutes": 15,
  "maxPatients": 16
}
```

## 8. Book appointment

- `POST /api/patient/appointments`
- Header: `Authorization: Bearer {{patientToken}}`

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

## 9. Track queue

- `GET /api/queue/patient/{{tokenId}}`
- `GET /api/queue/doctor/{{doctorId}}?date=2026-03-16`

## 10. Doctor queue actions

Login as doctor, then call:

- `POST /api/doctor/queue/appointments/{{appointmentId}}/start`
- `POST /api/doctor/queue/appointments/{{appointmentId}}/complete`
- `POST /api/doctor/queue/appointments/{{appointmentId}}/skip`

## 11. Prediction and analytics

- `GET /api/predictions/doctors/{{doctorId}}/wait-time?appointmentDate=2026-03-16&appointmentTime=10:00:00&priority=NORMAL`
- `GET /api/admin/analytics/overview?date=2026-03-16`

## 12. Real-time notifications

- `GET /api/notifications/stream`
- Header: `Authorization: Bearer {{patientToken}}`
- Keep the request open and trigger a new appointment or queue action in another tab

## 13. Optional email notifications

Enable SMTP config and set `NOTIFICATIONS_EMAIL_ENABLED=true` before starting the app.

## 14. Recommended test mode

- MySQL enabled
- Redis disabled
- Kafka disabled unless you are specifically testing event publishing
