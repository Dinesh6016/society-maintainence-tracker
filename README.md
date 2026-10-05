# Society Maintenance Tracker

A full-stack apartment society management platform where residents raise maintenance complaints, track progress, and receive email updates. Admins manage complaints, set priorities, detect overdue issues, and post notices.

---

## Features

- JWT role-based auth (RESIDENT / ADMIN)
- Complaint lifecycle: OPEN → IN_PROGRESS → RESOLVED
- Immutable status & priority history with actor + timestamp
- Photo uploads via Cloudinary (JPG/PNG/WEBP, max 5MB)
- Configurable overdue detection with scheduled job
- Notice board with important-notice pinning
- Email notifications on status change and important notices
- Admin dashboard with Recharts bar/pie charts
- Resident dashboard with personal stats
- Full filtering, pagination, and sorting on all list views

---

## Tech Stack

| Layer     | Technology                                      |
|-----------|-------------------------------------------------|
| Frontend  | React 18, Vite, React Router v6, Recharts       |
| Backend   | Java 17, Spring Boot 3, Spring Security, JWT    |
| Database  | MySQL 8                                         |
| Storage   | Cloudinary                                      |
| Email     | Spring Mail (Gmail SMTP / any SMTP)             |
| Testing   | JUnit 5, Mockito, Spring Boot Test, H2          |

---

## Project Structure

```
society-maintenance-tracker/
├── backend/          Spring Boot API
├── frontend/         React + Vite SPA
├── database/
│   ├── schema.sql    DDL for all tables
│   └── sample-data.sql
├── docs/
│   ├── system-design.md
│   └── api-documentation.md
├── README.md
└── .gitignore
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+
- Cloudinary account (free tier)

---

## Setup

### 1. Database

```sql
CREATE DATABASE society_tracker;
```
Or run: `mysql -u root -p < database/schema.sql`

To load sample data: `mysql -u root -p society_tracker < database/sample-data.sql`

---

### 2. Backend

```bash
cd backend
cp .env.example .env    # fill in your values
```

Set environment variables in `.env` or export them, then:

```bash
mvn spring-boot:run
# Server starts on http://localhost:8080
```

Tables are auto-created by Hibernate on first run (`ddl-auto=update`).

---

### 3. Frontend

```bash
cd frontend
cp .env.example .env    # VITE_API_BASE_URL=http://localhost:8080/api
npm install
npm run dev
# App starts on http://localhost:5173
```

---

### 4. Create Admin User

Register via `/register`, then promote in MySQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
```

Or use the sample data which includes `admin@society.com` / `Admin@123`.

---

## Environment Variables

### Backend (`backend/.env.example`)

```
DB_URL=jdbc:mysql://localhost:3306/society_tracker?...
DB_USERNAME=root
DB_PASSWORD=

JWT_SECRET=your_64_char_hex_secret
JWT_EXPIRATION=86400000

CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=noreply@societytracker.com
MAIL_ENABLED=false

COMPLAINT_OVERDUE_DAYS=3
FRONTEND_URL=http://localhost:5173
```

### Frontend (`frontend/.env.example`)

```
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## Cloudinary Setup

1. Sign up at [cloudinary.com](https://cloudinary.com) (free tier)
2. Copy Cloud Name, API Key, API Secret from dashboard
3. Set in backend `.env`

---

## Email Setup (Gmail)

1. Enable 2-Step Verification on your Google account
2. Generate an App Password: Google Account → Security → App Passwords
3. Set `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_ENABLED=true` in `.env`

---

## Default Dev Accounts (sample-data.sql)

| Role     | Email                  | Password    |
|----------|------------------------|-------------|
| Admin    | admin@society.com      | Admin@123   |
| Resident | ravi@resident.com      | Admin@123   |
| Resident | priya@resident.com     | Admin@123   |
| Resident | arjun@resident.com     | Admin@123   |

---

## Running Tests

```bash
cd backend
mvn test
```

Tests use H2 in-memory database (no MySQL required for tests).

---

## Build for Production

```bash
# Backend JAR
cd backend && mvn clean package -DskipTests
java -jar target/tracker-1.0.0.jar

# Frontend
cd frontend && npm run build
# Output in frontend/dist/
```

---

## Deployment

### Backend → Render

1. Create Web Service → connect GitHub repo → root dir: `backend`
2. Build: `mvn clean package -DskipTests`
3. Start: `java -jar target/tracker-1.0.0.jar`
4. Add all env vars from `.env.example`

### Frontend → Vercel

1. Import repo → set root dir: `frontend`
2. Set `VITE_API_BASE_URL` to your Render backend URL + `/api`
3. Deploy — Vercel auto-detects Vite

### Database → Railway / PlanetScale

1. Create MySQL instance
2. Copy connection string to `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

---

## API Documentation

See [docs/api-documentation.md](docs/api-documentation.md)

## System Design

See [docs/system-design.md](docs/system-design.md)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Access denied for user 'root'` | Check `DB_USERNAME` / `DB_PASSWORD` in `.env` |
| `CORS error` in browser | Ensure `FRONTEND_URL` matches your frontend origin exactly |
| Photos not uploading | Check Cloudinary credentials; ensure `CLOUDINARY_CLOUD_NAME` is set |
| Emails not sending | Set `MAIL_ENABLED=true` and verify Gmail App Password |
| `Invalid JWT` after restart | JWT secret changed — users need to log in again |
