# FinApp

A secure, full-stack finance dashboard with role-based access control. Built with **Spring Boot 4** and **React 19**, it lets organisations record, filter, and analyse financial data — with every action gated by the authenticated user's role.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 23 · Spring Boot 4.0.5 · Spring Security · Maven |
| Auth | JWT (JJWT 0.11.5) · BCrypt password hashing |
| Database | PostgreSQL on [Neon.tech](https://neon.tech) · Spring Data JPA |
| Frontend | React 19 · Vite 8 · Axios · React Router 7 |
| Charts | Recharts 3 |
| API Docs | Springdoc OpenAPI 2.3 (Swagger UI) |

---

## Features

- **JWT authentication** — stateless, 24-hour tokens with role and user ID embedded as claims
- **Three-tier RBAC** — Admin, Analyst, and Viewer with backend-enforced access at the method level
- **Full financial CRUD** — create, read, update, and delete records (amount, type, category, date, notes)
- **Filtering** — filter records by transaction type, category (partial match), and date range
- **Dashboard aggregations** — income/expense totals, net balance, category-wise breakdowns, and monthly trends — all computed at the database level
- **IDOR protection** — users can only access their own data; elevated roles may access any
- **Global error handling** — typed exception hierarchy maps directly to HTTP status codes (400 / 401 / 403 / 404 / 409 / 500)
- **Protected frontend routes** — token presence and expiry checked before rendering any page
- **Swagger UI** — interactive API docs at `/swagger-ui.html`

---

## Role Permissions

| Action | Viewer | Analyst | Admin |
|---|:---:|:---:|:---:|
| View company summary (income / expense / balance) | ✓ | ✓ | ✓ |
| View category totals & monthly trends | ✓ | ✓ | ✓ |
| View all company records (paginated) | ✗ | ✓ | ✓ |
| View own records & personal summary | own only | ✓ | ✓ |
| Create financial records | ✗ | ✗ | ✓ |
| Update / delete financial records | ✗ | ✗ | ✓ |
| Manage users (create, role change, activate/deactivate) | ✗ | ✗ | ✓ |

---

## Project Structure

```
Finance-App/
├── FinApp/                          ← Spring Boot backend
│   └── src/main/java/com/Fin/FinApp/
│       ├── config/                  ← Security, JWT filter, CORS
│       ├── controller/              ← AuthController, FinanceRecordController, UserController
│       ├── dto/                     ← AuthRequestDTO, AuthResponseDTO, DashboardSummaryDTO
│       ├── entity/                  ← User, FinanceRecord, Role (enum), TransactionType (enum)
│       ├── exception/               ← GlobalExceptionHandler, InvalidCredentialsException, ResourceNotFoundException
│       ├── repository/              ← JPA repositories with custom JPQL queries
│       └── service/                 ← FinanceRecordService, UserService, JwtService
└── finapp-frontend/                 ← React + Vite frontend
    └── src/
        ├── components/ProtectedRoute.jsx
        ├── pages/                   ← Login, Dashboard, UserManagement
        └── services/api.js          ← Axios instance with JWT interceptor
```

---

## Prerequisites

- **JDK 23** or higher
- **Node.js 18+** and npm
- **Maven 3.9+** (or use the included `./mvnw` wrapper — no installation needed)
- A **PostgreSQL** database (cloud: [Neon.tech](https://neon.tech) recommended, or local)

---

## Environment Variables

All secrets are read from environment variables — nothing is hardcoded.

| Variable | Description |
|---|---|
| `DB_URL` | Full JDBC connection string, e.g. `jdbc:postgresql://host/mydb?sslmode=require` |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | 64-character hex string used to sign JWT tokens |
| `CORS_ALLOWED_ORIGIN` | Frontend origin allowed by CORS (defaults to `http://localhost:5173`) |

Set them in your shell before starting the backend:

```bash
export DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require
export DB_USERNAME=<your-db-username>
export DB_PASSWORD=<your-db-password>
export JWT_SECRET=<64-char-hex-string>
export CORS_ALLOWED_ORIGIN=http://localhost:5173
```

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/midhun-e-m/Finance-App.git
cd Finance-App
```

### 2. Start the backend

```bash
cd FinApp
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Start the frontend

Open a second terminal from the project root:

```bash
cd finapp-frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## API Overview

### Authentication — `/api/auth` (public)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user (assigned Viewer role) |
| `POST` | `/api/auth/login` | Log in and receive a JWT token |

### Financial Records — `/api/records`

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/records` | Admin | Create a record |
| `GET` | `/api/records/user/{userId}` | Any (own ID) | Get personal records (paginated) |
| `GET` | `/api/records/user/{userId}/summary` | Any (own ID) | Personal income/expense/balance |
| `GET` | `/api/records/user/{userId}/summary/by-category` | Any (own ID) | Personal category totals |
| `GET` | `/api/records/user/{userId}/summary/by-month` | Any (own ID) | Personal monthly trends |
| `PUT` | `/api/records/{id}` | Admin | Update a record |
| `DELETE` | `/api/records/{id}` | Admin | Delete a record |
| `GET` | `/api/records/all` | Admin, Analyst | All company records (paginated) |
| `GET` | `/api/records/all/summary` | All roles | Company-wide summary |
| `GET` | `/api/records/all/summary/by-category` | All roles | Company category totals |
| `GET` | `/api/records/all/summary/by-month` | All roles | Company monthly trends |

**Query parameters for listing endpoints:** `type` (INCOME / EXPENSE), `category` (partial match), `startDate`, `endDate` (YYYY-MM-DD), `page` (default 0), `size` (default 5).

### User Management — `/api/users` (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users` | List all users |
| `POST` | `/api/users` | Create a user |
| `PUT` | `/api/users/{id}` | Update role or active status |

---

## Error Responses

Every error returns a JSON object. HTTP status codes are mapped by exception type — no string matching.

| Status | Cause |
|---|---|
| `400` | Validation failure (`@Valid`) — response includes per-field messages |
| `401` | Wrong password (`InvalidCredentialsException`) |
| `403` | Accessing another user's data (IDOR check) |
| `404` | Email not found at login (`ResourceNotFoundException`) |
| `409` | Duplicate email on register |
| `500` | Unhandled runtime error — message is not leaked to the client |

---

## Running Tests

```bash
cd FinApp
./mvnw test
```

---

## Building for Production

```bash
# Backend — produces an executable JAR
cd FinApp
./mvnw clean package -DskipTests
java -jar target/FinApp-0.0.1-SNAPSHOT.jar

# Frontend — produces static assets in dist/
cd finapp-frontend
npm run build
```

Before deploying, set all five environment variables and change `spring.jpa.hibernate.ddl-auto` from `update` to `validate` in `application.properties` to prevent accidental schema changes in production.
