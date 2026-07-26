<div align="center">

# Clinic Portal

### A multi-actor clinic management platform built with **Spring Boot**, **React/TypeScript**, and a dedicated event-driven **Notification Service**.

[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![CI](https://img.shields.io/github/actions/workflow/status/davidgeamanu/Clinic-Portal/ci.yml?style=for-the-badge&label=CI)](https://github.com/davidgeamanu/Clinic-Portal/actions)

*Patients book appointments and track their medical history. Doctors manage their schedule and record consultation notes. Admins oversee users, rooms, and departments. Notifications are handled by a separate microservice that listens to events over RabbitMQ.*

[Features](#features) •
[Tech Stack](#tech-stack) •
[Getting Started](#getting-started) •
[API Documentation](#api-documentation) •
[Future Enhancements](#future-enhancements)

</div>

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [Future Enhancements](#future-enhancements)
- [Author](#author)

---

## Features

<table>
<tr>
<td width="33%">

### Patient
- Self-registration & secure login
- **AI symptom triage** — describe symptoms, get a department recommendation (Claude API)
- Book appointments from **real availability slots** computed from doctor working hours
- Read **doctor reviews** before booking
- View consultation notes & medical documents
- Track personal health profile (vitals, allergies, conditions, family history)
- Rate and review completed appointments
- **Real-time notifications over SSE** (no polling)

</td>
<td width="33%">

### Doctor
- Manage personal schedule & appointments
- Define **weekly working hours** that drive patient booking slots
- Run consultations (start, complete, cancel)
- Record diagnosis, treatment, prescriptions & notes
- Upload, download, and delete medical documents
- View patient history (only for patients with a shared appointment)
- See own **patient reviews** and update biography / consultation fee

</td>
<td width="33%">

### Admin
- Manage users (patients, doctors, admins) with soft-delete
- Create doctor accounts & profiles
- Manage rooms (department, type, status)
- Monitor departments and specializations
- View dashboard KPIs, weekly charts & analytics
- Paginated patient and appointment lists
- Force-cancel any appointment

</td>
</tr>
</table>

---

## Tech Stack

<table>
<tr>
<td width="33%" valign="top">

### Backend (Clinic Portal)
- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Security** with JWT (HttpOnly cookie)
- **Spring Data JPA / Hibernate**
- **PostgreSQL** + **Flyway** migrations
- **Anthropic Java SDK** (AI triage)
- **Lombok**
- **Maven**

</td>
<td width="33%" valign="top">

### Notification Service
- **Java 25**
- **Spring Boot 4.0.6**
- **RabbitMQ** for async event consumption
- **Server-Sent Events** for real-time push
- **JavaMail** via Mailtrap SMTP
- **PostgreSQL** + **Flyway** migrations
- **Maven**

</td>
<td width="33%" valign="top">

### Frontend
- **React 19** + **Vite**
- **TypeScript**
- **shadcn/ui** (Radix + Tailwind)
- **TanStack Query**
- **Axios**
- **React Router v6**
- **React Hook Form + Zod**

</td>
</tr>
</table>

---

## Architecture

```
Clinic Portal (8080) → RabbitMQ → Notification Service (8081) → Frontend (5173, Vite proxy)
```

The system uses two separate PostgreSQL databases: `clinic_portal` for the main backend and `clinic_notifications_db` for the notification service. They share no tables and make no calls to each other.

When something notable happens (an appointment is booked, its status changes, or a consultation note is added), the backend publishes a fat event to one of three durable RabbitMQ queues:

- `clinic.appointment-booked.queue`
- `clinic.appointment-status-changed.queue`
- `clinic.consultation-note-created.queue`

Events are self-contained and carry all the data the notification service needs (names, emails, timestamps), so there are no callbacks into the portal.

Emails are sent through Mailtrap SMTP with color-coded HTML templates per notification type, and can be toggled off via `MAIL_ENABLED`. New notifications are also pushed to the browser in real time over **Server-Sent Events**.

### Messaging Reliability

- **Transactional outbox:** events are written to an `outbox_events` table in the same transaction as the domain change; a scheduled `OutboxRelay` publishes them to RabbitMQ. If the broker is down, events queue up and are delivered when it recovers — nothing is lost.
- **Idempotent consumer:** the notification service records every processed `eventId` in a `processed_events` table, so at-least-once delivery never produces duplicate notifications.
- **Dead letter queue:** all three queues dead-letter into `clinic.notifications.dlq` after the retry policy is exhausted, so poison messages can be inspected instead of being redelivered forever.
- **Correlation IDs:** every HTTP request gets an `X-Correlation-Id` that travels through the outbox into RabbitMQ headers and into the notification service's logs — one booking can be traced across both services.

### Design Patterns

- **Auth:** stateless JWT issued as an HttpOnly, `SameSite=Lax` cookie (8-hour expiry), carrying email, userId, role, profileId, and active status. The `active` flag is re-checked against the database on every request, so deactivating an account takes effect immediately.
- **Authorization:** `@PreAuthorize` with shared SpEL expressions in `AuthorizationExpressions`. A common `AuthenticatedController` interface exposes `currentUser()` to all controllers.
- **Observer pattern:** domain events via `ApplicationEventPublisher`. A synchronous `RoomStatusEventListener` handles atomic side effects, while `OutboxEventRecorder` writes cross-service events to the transactional outbox before commit.
- **State pattern:** one `@Component` per `AppointmentStatus` (including the terminal `NO_SHOW`), split by interface into `AppointmentState`, `EntryValidator`, and `EntryHook`. Looked up through an `EnumMap`-backed `AppointmentStateRegistry`.
- **Strategy pattern:** in the notification service, one `@Component` per `NotificationType` builds the human-readable message. Indexed by `NotificationMessageStrategyRegistry`.
- **Auto-expiry:** `AppointmentExpirationJob` runs every minute and cancels bookings still unconfirmed at their start time, so an abandoned booking stops holding the doctor's slot.
- **No-shows:** a separate, manual action. Only a doctor or admin can mark a *confirmed* appointment as `NO_SHOW`, and only once its start time has passed — patients cannot mark themselves absent.
- **Double-booking protection:** the application checks for overlaps, but the real guarantee is a PostgreSQL **GiST exclusion constraint** on `(doctor_id, time range)` — two concurrent bookings for the same slot cannot both commit.
- **Soft-delete:** the `active` flag on `User` controls account visibility. Errors are returned as a consistent `ExceptionBody { timestamp, code, message, details }`.

---

## Getting Started

### Quick Start with Docker (recommended)

The whole stack — PostgreSQL, RabbitMQ, both services, and the frontend behind nginx — comes up with one command:

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API / Swagger | http://localhost:8080/api/swagger-ui/index.html |
| Notification API / Swagger | http://localhost:8081/api/swagger-ui/index.html |
| RabbitMQ management UI | http://localhost:15672 (guest/guest) |

Optional settings (Claude API key for AI triage, Mailtrap credentials) go in a `.env` file — see [.env.example](.env.example). Everything else has safe dev defaults.

### Manual Setup

### Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| **Java** | 25 | [Download](https://adoptium.net/) |
| **Maven** | 3.9+ | [Download](https://maven.apache.org/download.cgi) |
| **PostgreSQL** | 14+ | [Download](https://www.postgresql.org/download/) |
| **RabbitMQ** | 3.x | [Download](https://www.rabbitmq.com/docs/download) |
| **Node.js** | 18+ | [Download](https://nodejs.org/) |

---

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/davidgeamanu/Clinic-Portal.git
cd Clinic-Portal
```

#### 2. Set Up the Databases

```sql
CREATE DATABASE clinic_portal;
CREATE DATABASE clinic_notifications_db;
```

> The schema is managed by **Flyway** — migrations under `src/main/resources/db/migration` run automatically on startup, and Hibernate validates the result (`ddl-auto: validate`).

---

#### 3. Configure Environment Variables

<details>
<summary><b>Windows (PowerShell)</b></summary>

```powershell
$env:DB_PASS="your_postgres_password"
$env:JWT_SECRET="your_64_character_secret_key"
$env:MAILTRAP_USERNAME="your_mailtrap_username"
$env:MAILTRAP_PASSWORD="your_mailtrap_password"
$env:MAIL_ENABLED="true"
```

</details>

<details>
<summary><b>Linux/Mac</b></summary>

```bash
export DB_PASS=your_postgres_password
export JWT_SECRET=your_64_character_secret_key
export MAILTRAP_USERNAME=your_mailtrap_username
export MAILTRAP_PASSWORD=your_mailtrap_password
export MAIL_ENABLED=true
```

</details>

> `JWT_SECRET` must be the same value in both the backend and the notification service, since both validate the same token.

---

#### 4. Start RabbitMQ

The queues are consumer-declared, so RabbitMQ just needs to be running on `localhost:5672` before you start the backend. No manual queue setup needed.

<details>
<summary><b>Option A - Docker (recommended)</b></summary>

```bash
docker run -d --name clinic-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

The management UI will be available at `http://localhost:15672` (login: `guest` / `guest`).

</details>

<details>
<summary><b>Option B - Local install</b></summary>

Install RabbitMQ from the [official downloads page](https://www.rabbitmq.com/docs/download) and start the service:

```powershell
# Windows - RabbitMQ runs as a service after install
net start RabbitMQ
```

```bash
# Linux/Mac
sudo systemctl start rabbitmq-server
```

</details>

> If RabbitMQ isn't running, the app still works for bookings and status changes. You just won't get any in-app notifications or emails until it's up.

---

#### 5. Set Up Mailtrap (for email notifications)

The notification service sends emails through Mailtrap's SMTP sandbox, so nothing lands in a real inbox during development.

1. Create a free account at [mailtrap.io](https://mailtrap.io/) and open (or create) an **Email Sandbox** inbox.
2. Go to the inbox's **SMTP Settings** and copy the **Username** and **Password**.
3. Set those as `MAILTRAP_USERNAME` and `MAILTRAP_PASSWORD` (see [Environment Variables](#environment-variables)).
4. Leave `MAIL_ENABLED=true` to send to the sandbox, or set it to `false` to skip emails entirely. In-app notifications still work either way.

---

#### 6. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Starts on **`http://localhost:8080`**

---

#### 7. Run the Notification Service

```bash
cd notification-service
./mvnw spring-boot:run
```

Starts on **`http://localhost:8081`**

---

#### 8. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on **`http://localhost:5173`**. The Vite dev server proxies `/api/notifications` to port `8081` and everything else under `/api` to port `8080`.

---

## Environment Variables

| Variable | Used by | Purpose |
|----------|---------|---------|
| `DB_PASS` | both | PostgreSQL password |
| `JWT_SECRET` | both | JWT signing secret (must match across both services) |
| `MAILTRAP_USERNAME` | notification-service | Mailtrap SMTP username |
| `MAILTRAP_PASSWORD` | notification-service | Mailtrap SMTP password |
| `MAIL_ENABLED` | notification-service | Toggles email sending (default: `true`) |
| `ANTHROPIC_API_KEY` | backend | Optional — enables Claude-powered symptom triage. Without it, a keyword-based fallback classifier is used |

---

## API Documentation

Both services expose a Swagger UI once running:

- **Clinic Portal:** `http://localhost:8080/api/swagger-ui/index.html`
- **Notification Service:** `http://localhost:8081/api/swagger-ui/index.html`

All endpoints are documented there with request/response schemas and you can authenticate directly in the UI using the `access_token` cookie. The tables below are just a quick overview.

All endpoints except `/api/auth/login` and `/api/auth/register` require an active session.

### Auth

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/login` | Public | Authenticates and sets the JWT cookie |
| `POST` | `/api/auth/logout` | Public | Clears the JWT cookie |
| `POST` | `/api/auth/register` | Public | Patient self-registration |
| `PATCH` | `/api/auth/password` | Authenticated | Change password |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | KPI counts, weekly chart data, department load |
| `GET` | `/api/admin/analytics` | Monthly revenue and patient trend |
| `GET` | `/api/admin/departments` | All specializations with counts |
| `GET` | `/api/admin/rooms` | All rooms |
| `PATCH` | `/api/admin/rooms/{id}` | Update room department, type, or status |
| `GET` | `/api/admin/patients?page=&size=` | Patients (paginated) |
| `GET` | `/api/admin/patients/{id}/appointments` | Appointments for a patient |
| `GET` | `/api/admin/doctors/{id}/appointments` | Appointments for a doctor |
| `GET` | `/api/admin/users` | All users |
| `GET` | `/api/admin/users/role/{role}` | Users filtered by role |
| `PATCH` | `/api/admin/users/{id}/status?active=` | Activate or deactivate an account |
| `POST` | `/api/admin/doctors` | Create a doctor account and profile |
| `PATCH` | `/api/admin/doctors/{doctorProfileId}/room` | Assign or unassign a consult room for a doctor |
| `GET` | `/api/admin/specializations` | All specializations |
| `GET` | `/api/admin/appointments?page=&size=` | Appointments (paginated) |
| `PATCH` | `/api/admin/appointments/{id}/cancel` | Force-cancel any appointment |

### Patients & Doctors

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/patients/me` | PATIENT | Own patient profile |
| `PUT` | `/api/patients/me` | PATIENT | Update own profile |
| `GET` | `/api/doctors` | Any | All doctor profiles |
| `GET` | `/api/doctors/{id}` | Any | Single doctor profile |
| `GET` | `/api/doctors/specialization/{specializationId}` | Any | Doctors by specialization |
| `GET` | `/api/doctors/{id}/booked-slots?date=` | Any | Booked slots for a doctor on a given date |
| `GET` | `/api/doctors/{id}/availability` | Any | A doctor's weekly working hours |
| `GET` | `/api/doctors/{id}/available-slots?date=` | Any | Free bookable slots computed from working hours minus booked appointments |
| `GET` | `/api/doctors/{id}/reviews` | Any | Patient ratings & reviews for a doctor |
| `GET` | `/api/doctors/me` | DOCTOR | Own doctor profile |
| `PUT` | `/api/doctors/me` | DOCTOR | Update biography and consultation fee |
| `GET` | `/api/doctors/me/availability` | DOCTOR | Own weekly working hours |
| `PUT` | `/api/doctors/me/availability` | DOCTOR | Replace own weekly working hours |
| `GET` | `/api/doctors/me/reviews` | DOCTOR | Own patient reviews |
| `GET` | `/api/doctors/me/recent-patients` | DOCTOR | Last 3 distinct patients |
| `GET` | `/api/doctors/me/patients` | DOCTOR | All distinct patients |
| `GET` | `/api/doctors/me/patients/{id}` | DOCTOR | Patient summary (requires a shared appointment) |

### AI Triage

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/triage` | PATIENT | Recommends a department from a free-text symptom description (Claude API with keyword fallback) |

### Appointments

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/appointments` | PATIENT | Book an appointment |
| `GET` | `/api/appointments/my/patient` | PATIENT | Own appointments |
| `GET` | `/api/appointments/my/doctor` | DOCTOR | Own appointments |
| `GET` | `/api/appointments/{id}` | PATIENT/DOCTOR | Single appointment |
| `PATCH` | `/api/appointments/{id}/status` | Varies | Update status via the state machine |
| `POST` | `/api/appointments/{id}/rate` | PATIENT | Rate a completed appointment (1-5, one time only) |

### Consultation Notes & Documents

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/consultation-notes/appointment/{id}` | DOCTOR | Create a consultation note |
| `PUT` | `/api/consultation-notes/appointment/{id}` | DOCTOR | Update a consultation note |
| `GET` | `/api/consultation-notes/appointment/{id}` | PATIENT/DOCTOR | Get the note for an appointment |
| `GET` | `/api/consultation-notes/mine` | DOCTOR | All notes by this doctor |
| `GET` | `/api/consultation-notes/my-history` | PATIENT | All notes for this patient |
| `GET` | `/api/consultation-notes/patient/{id}` | DOCTOR | All notes for a patient (requires a shared appointment) |
| `POST` | `/api/consultation-notes/{noteId}/documents` | DOCTOR | Upload a document |
| `GET` | `/api/consultation-notes/{noteId}/documents/{docId}` | PATIENT/DOCTOR | Download a document |
| `DELETE` | `/api/consultation-notes/{noteId}/documents/{docId}` | DOCTOR | Delete a document |

### Notifications (port 8081)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/api/notifications/me` | Authenticated | All notifications for the current user |
| `GET` | `/api/notifications/stream` | Authenticated | Server-Sent Events stream — pushes new notifications in real time |
| `PATCH` | `/api/notifications/{id}/read` | Authenticated | Mark one notification as read |
| `PATCH` | `/api/notifications/read-all` | Authenticated | Mark all notifications as read |

---

## Database Schema

### Key Entities

| Entity | Notes |
|--------|-------|
| `User` | Auth only - email, password hash, role, and an `active` flag for soft-delete |
| `PatientProfile` | One-to-one with `User` - DOB, gender, blood type, address, emergency contact, vitals, allergies, conditions, family history, lifestyle |
| `DoctorProfile` | One-to-one with `User` - license number, biography, consultation fee, rating, room (nullable), specializations (many-to-many) |
| `Specialization` | Many-to-many with `DoctorProfile`. 7 fixed records seeded at startup, not user-creatable |
| `Room` | Room number, floor, type (`CONSULT`/`OR`/`IMAGING`), status (`FREE`/`OCCUPIED`), specialization FK |
| `Appointment` | Links a patient and a doctor - scheduled time, duration, mode, status, reason, timestamps, rating, review |
| `ConsultationNote` | One-to-one with `Appointment` - diagnosis, treatment, prescription, notes |
| `MedicalDocument` | Many-to-one with `ConsultationNote` - file metadata (files stored locally on disk) |
| `DoctorAvailability` | Recurring weekly working windows per doctor (day of week, start, end) - drives available booking slots |
| `OutboxEvent` | Transactional outbox row - fat event payload written atomically with the domain change, relayed to RabbitMQ |
| `Notification` | Lives in `clinic_notifications_db` - userId, message, type, read flag, related entity id |
| `ProcessedEvent` | Lives in `clinic_notifications_db` - consumed `eventId` ledger for idempotent event processing |

**Appointment lifecycle:** `SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED`, with `SCHEDULED → CANCELLED`, `CONFIRMED → CANCELLED / NO_SHOW`, and `IN_PROGRESS → CANCELLED`

### Schema Management

The schema is versioned with **Flyway** (`db/migration` in each service) and Hibernate runs in `validate` mode. Highlights: a PostgreSQL `btree_gist` **exclusion constraint** makes overlapping appointments for the same doctor impossible to commit, and a partial index keeps the outbox relay scan cheap.

---

## Project Structure

```
Clinic-Portal/
├── backend/                          # Clinic Portal (Spring Boot, port 8080)
│   └── src/main/java/com/clinic/portal/
│       ├── controller/               # REST endpoints (admin, appointment, auth, consultation, doctor, patient)
│       ├── service/                  # Business logic, including the appointment state machine
│       ├── model/                    # JPA entities & enums
│       ├── repository/               # Spring Data repositories & projections
│       ├── dto/                      # Request/response objects
│       ├── mapper/                   # Entity to DTO mappers
│       ├── security/                 # JWT authentication & filters
│       ├── event/                    # Domain events, listeners & RabbitMQ publisher
│       ├── exception/                # Centralized exception handling
│       └── config/                   # App configuration
├── notification-service/             # Notification microservice (Spring Boot, port 8081)
│   └── src/main/java/com/clinic/notificationservice/
│       ├── consumer/                 # RabbitMQ event consumers
│       ├── strategy/                 # Per-NotificationType message builders
│       ├── channel/                  # Email & in-app delivery channels
│       ├── controller/               # REST endpoints for the frontend
│       └── model/, repository/, dto/, event/, security/, config/
├── frontend/                         # React 19 + Vite + TypeScript
│   └── src/
│       ├── api/                      # Axios calls (the only layer that talks to the backend)
│       ├── hooks/                    # TanStack Query hooks
│       ├── pages/                    # Route-level pages (admin/, doctor/, patient/)
│       ├── components/               # Shared & shadcn/ui components
│       ├── contexts/                 # Auth/QueryClient providers
│       ├── lib/                      # Query keys, utilities, error helpers
│       └── types/                    # Shared TypeScript types
└── README.md
```

---

## Future Enhancements

### Messaging & Reliability
- **Password change notifications** - a new event type, queue, and strategy that sends a security email when a password changes

### Caching
- **Redis** - cache doctor profiles, specialization lists, available slots, and dashboard KPI queries, with cache invalidation on writes

### Infrastructure & Scaling
- **Kubernetes** - orchestrate the backend, notification service, PostgreSQL, and RabbitMQ as pods with horizontal scaling, health checks, and rolling deployments
- **Distributed lock (ShedLock)** - prevent duplicate `AppointmentExpirationJob` and `OutboxRelay` runs when running multiple backend instances
- **Service decomposition** - split the backend into smaller services (appointment-service, user-service, consultation-service) if the domain grows significantly

### Patient-Facing Features
- **Room utilization beyond consultations** - allow `OR` and `IMAGING` rooms to be booked independently for surgeries or scans, not just auto-assigned through appointments
- **Live chat** - upgrade the SSE channel to WebSockets for bidirectional patient-doctor messaging

---

## Author

**David Geamanu**
