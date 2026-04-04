# finApp

A full-stack finance data processing and access control dashboard. This application provides secure, role-based access to financial records, utilizing a Spring Boot backend and a React frontend, with a cloud-hosted PostgreSQL database.

## 🚀 Features

* **Secure Authentication:** JWT-based user authentication.
* **Role-Based Access Control (RBAC):** Distinct access levels for **Admin**, **Analyst**, and **Viewer** roles to ensure data security.
* **Financial Data Processing:** Efficient backend services for managing and processing financial records.
* **Cloud Database:** Integrated with Neon.tech for reliable PostgreSQL hosting.

## 🛠️ Tech Stack

**Frontend:**
* React

**Backend:**
* Java
* Spring Boot
* Spring Security (JWT)
* Maven

**Database:**
* PostgreSQL (hosted on Neon.tech)

## 📋 Prerequisites

Before running the project, ensure you have the following installed on your machine:
* [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) (Version 17 or higher recommended)
* [Node.js and npm](https://nodejs.org/) (for the React frontend)
* [Maven](https://maven.apache.org/) (for backend dependency management)
* A [Neon.tech](https://neon.tech/) account and an active PostgreSQL database instance.

## ⚙️ Installation and Setup

This project contains both the frontend and backend in a single repository. You will need to run both servers concurrently.

### 1. Clone the Repository

```bash
git clone [https://github.com/yourusername/finApp.git](https://github.com/yourusername/finApp.git)
cd finApp

```

###2. Backend Setup (Spring Boot)

1.Navigate to the backend directory:

```bash
cd backend
```
2.Configure your environment variables. Open src/main/resources/application.properties (or application.yml) and update your database credentials and JWT secret:
```bash
# Database Configuration (Neon.tech)
spring.datasource.url=jdbc:postgresql://<your-neon-hostname>/<your-database>?sslmode=require
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=your_super_secret_key_here
jwt.expiration=86400000
```
3.Install dependencies and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```
### 3. Frontend Setup (React)

1. Open a new terminal window and navigate to the frontend directory from the root of the project:
   ```bash
   cd frontend
   ```
2. Install the required npm packages:
   ```bash
   npm install
   ```
3. Ensure your API base URL is correctly pointing to the backend. You may need to set up a `.env` file in the frontend directory:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```
4. Start the React development server:
   ```bash
   npm start
   ```
   *The frontend should now be running on `http://localhost:3000`.*

## 🔒 Default Roles & Access

* **Admin:** Full access to view, edit, delete financial records, and manage user roles.
* **Analyst:** Access to view and process financial data.
* **Viewer:** Read-only access to basic dashboard metrics.
