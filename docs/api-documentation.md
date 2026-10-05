# API Documentation — Society Maintenance Tracker

Base URL: `http://localhost:8080/api`

All protected routes require: `Authorization: Bearer <token>`

---

## Authentication

### POST /auth/register
Register a new resident account.

**Request Body:**
```json
{
  "name": "Ravi Kumar",
  "email": "ravi@example.com",
  "password": "secret123",
  "phone": "9000000001",
  "apartmentNumber": "B101"
}
```
**Response 201:**
```json
{ "token": "eyJ...", "user": { "id": 1, "name": "Ravi Kumar", "role": "RESIDENT", ... } }
```
**Errors:** `400` validation failed · `409` email already registered

---

### POST /auth/login
```json
{ "email": "ravi@example.com", "password": "secret123" }
```
**Response 200:** `{ "token": "...", "user": { ... } }`
**Errors:** `401` invalid credentials

---

### GET /auth/me
Returns the currently authenticated user.
**Response 200:** `UserDto`

---

## Resident — Complaints

### POST /complaints
Submit a new complaint. `multipart/form-data`

| Part   | Type             | Required |
|--------|------------------|----------|
| `data` | JSON (see below) | Yes      |
| `photo`| Image file       | No       |

**data JSON:**
```json
{ "title": "Water Leakage", "category": "PLUMBING", "description": "Pipe leaking under sink" }
```
**Categories:** `PLUMBING` `ELECTRICAL` `CLEANING` `SECURITY` `LIFT` `WATER` `PARKING` `MAINTENANCE` `OTHER`

**Response 201:** `ComplaintResponse`
**Errors:** `400` validation · `413` file too large

---

### GET /complaints/my
Returns paginated list of the authenticated resident's complaints.

**Query params:** `page` `size` `sort`

**Response 200:** `Page<ComplaintResponse>`

---

### GET /complaints/{id}
Returns a single complaint. Residents can only access their own.

**Response 200:** `ComplaintResponse` · **Errors:** `403` `404`

---

### GET /complaints/{id}/history
Returns status history for a complaint (resident's own only).

**Response 200:** `List<StatusHistoryResponse>`

---

## Admin — Complaints

### GET /admin/complaints
Returns all complaints with filtering and pagination.

**Query params:**

| Param      | Type     | Example          |
|------------|----------|------------------|
| `status`   | String   | `OPEN`           |
| `category` | String   | `PLUMBING`       |
| `priority` | String   | `HIGH`           |
| `overdue`  | Boolean  | `true`           |
| `from`     | Date     | `2026-01-01`     |
| `to`       | Date     | `2026-12-31`     |
| `page`     | Integer  | `0`              |
| `size`     | Integer  | `20`             |

Default sort: `isOverdue DESC, priority DESC, createdAt ASC`

**Response 200:** `Page<ComplaintResponse>`

---

### GET /admin/complaints/{id}
**Response 200:** `ComplaintResponse`

---

### PATCH /admin/complaints/{id}/status
```json
{ "status": "IN_PROGRESS", "note": "Technician assigned" }
```
**Valid transitions:** `OPEN → IN_PROGRESS` · `IN_PROGRESS → RESOLVED`

**Response 200:** Updated `ComplaintResponse`
**Errors:** `400` invalid transition · `404` not found

---

### PATCH /admin/complaints/{id}/priority
```json
{ "priority": "HIGH", "note": "Escalated by resident" }
```
**Response 200:** Updated `ComplaintResponse`

---

### GET /admin/complaints/{id}/history/status
**Response 200:** `List<StatusHistoryResponse>`

---

### GET /admin/complaints/{id}/history/priority
**Response 200:** `List<PriorityHistoryResponse>`

---

## Notices

### GET /notices
Returns all notices. Important notices appear first.
**Auth:** Any authenticated user
**Response 200:** `List<NoticeResponse>`

---

### POST /notices
**Auth:** Admin only
```json
{ "title": "Water Shutdown", "content": "Water will be off Sunday 6-10 AM", "important": true }
```
**Response 201:** `NoticeResponse`
If `important = true`, emails are sent to all residents asynchronously.

---

### PUT /notices/{id}
**Auth:** Admin only
```json
{ "title": "Updated Title", "content": "Updated content", "important": false }
```
**Response 200:** Updated `NoticeResponse`

---

### DELETE /notices/{id}
**Auth:** Admin only
**Response 204:** No content

---

## Dashboard

### GET /admin/dashboard
**Auth:** Admin only
```json
{
  "totalComplaints": 100,
  "open": 30,
  "inProgress": 25,
  "resolved": 45,
  "overdue": 8,
  "highPriority": 12,
  "byCategory": { "PLUMBING": 20, "ELECTRICAL": 15, "LIFT": 10 },
  "byStatus":   { "OPEN": 30, "IN_PROGRESS": 25, "RESOLVED": 45 }
}
```

---

### GET /resident/dashboard
**Auth:** Resident only
```json
{ "total": 5, "open": 2, "inProgress": 1, "resolved": 2 }
```

---

## Response Schemas

### ComplaintResponse
```json
{
  "id": 1,
  "resident": { "id": 2, "name": "Ravi Kumar", "email": "...", "apartmentNumber": "B101" },
  "category": "PLUMBING",
  "title": "Water Leakage",
  "description": "...",
  "photoUrl": "https://res.cloudinary.com/...",
  "status": "OPEN",
  "priority": "HIGH",
  "isOverdue": true,
  "createdAt": "2026-09-01T10:00:00",
  "updatedAt": "2026-09-01T10:00:00",
  "resolvedAt": null
}
```

### StatusHistoryResponse
```json
{
  "id": 1,
  "oldStatus": "OPEN",
  "newStatus": "IN_PROGRESS",
  "changedBy": "Admin User",
  "changedAt": "2026-09-08T10:30:00",
  "note": "Technician assigned"
}
```

### Error Response
```json
{
  "timestamp": "2026-09-08T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot change status of a resolved complaint",
  "path": "/api/admin/complaints/10/status"
}
```

---

## HTTP Status Codes

| Code | Meaning                        |
|------|--------------------------------|
| 200  | OK                             |
| 201  | Created                        |
| 204  | No Content                     |
| 400  | Bad Request / Validation Error |
| 401  | Unauthorized                   |
| 403  | Forbidden                      |
| 404  | Not Found                      |
| 409  | Conflict (duplicate email)     |
| 413  | Payload Too Large (file > 5MB) |
| 500  | Internal Server Error          |
