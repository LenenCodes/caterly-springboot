# Caterly

A full-stack catering management web application built with **Java, Spring Boot, MySQL, Spring Security, and Thymeleaf**.

Caterly is designed to connect customers with catering businesses for events and functions. Customers can browse verified caterers, view their menus and plate packages, create bookings, and submit reviews. Caterers can manage their profiles, dishes, packages, and incoming bookings, while administrators manage caterer verification, customers, caterers, and bookings.

## Features

### 👤 Customer

* Customer registration and login
* Browse verified caterers
* View caterer profiles
* Browse available dishes
* View dishes grouped by course type
* View plate-based catering packages
* Create catering bookings
* View booking information
* Update customer profile
* Review and rate caterers after completed bookings

### 🍽️ Caterer

* Caterer registration
* Profile management
* Caterer verification workflow
* Manage business information
* Add and manage dishes
* Toggle dish availability
* Create plate-based catering packages
* Toggle package visibility
* View incoming customer bookings
* View booking details
* Accept, reject, and complete bookings
* Upload profile media

### 🛡️ Administrator

* Role-based admin dashboard
* View pending caterers
* Verify caterers
* Revoke caterer access
* Manage customer accounts
* Manage caterer accounts
* View customer and caterer details
* View all bookings

## Tech Stack

| Technology              | Purpose                          |
| ----------------------- | -------------------------------- |
| Java 17                 | Application development          |
| Spring Boot 3.4.0       | Backend framework                |
| Spring MVC              | Web and controller layer         |
| Spring Data JPA         | Database persistence             |
| Hibernate               | ORM                              |
| Spring Security         | Authentication and authorization |
| Thymeleaf               | Server-side HTML rendering       |
| MySQL                   | Relational database              |
| Maven                   | Dependency management and build  |
| HTML / CSS / JavaScript | Frontend                         |
| BCrypt                  | Password hashing                 |

## Architecture

The application follows a layered Spring Boot architecture:

```text
┌─────────────────────────────┐
│       Thymeleaf UI          │
│        HTML / CSS / JS      │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Controller Layer      │
│  Customer / Caterer / Admin │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│        Service Layer        │
│   Business Logic & Rules    │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Repository Layer      │
│       Spring Data JPA       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│          MySQL              │
│        Database             │
└─────────────────────────────┘
```

The application separates controllers, services, repositories, DTOs, entities, security configuration, and enums into dedicated packages.

## Security

Caterly uses **Spring Security** for authentication and role-based authorization, with **form-based login and session authentication** (no JWT or token-based auth is used).

The application defines protected areas for:

* `CUSTOMER`
* `CATERER`
* `ADMIN`

Public routes such as login and registration remain accessible without authentication.

Passwords are encoded using **BCryptPasswordEncoder**.

Database credentials and the administrator password are configured through environment variables rather than being stored as plain-text credentials in the source code.

Example:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/caters_springboot}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

## Booking Flow

A typical booking flow is:

```text
Customer
   │
   ▼
Browse Verified Caterers
   │
   ▼
Select Caterer
   │
   ▼
View Menu & Plate Packages
   │
   ▼
Create Booking
   │
   ▼
Caterer Receives Request
   │
   ├── Accept
   ├── Reject
   └── Complete
   │
   ▼
Completed Booking
   │
   ▼
Customer Can Submit Review
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/caters/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   │
│   └── resources/
│       ├── templates/
│       └── application.properties
│
└── test/
    └── java/com/caters/
```

## Database

The application uses **MySQL** with Spring Data JPA and Hibernate.

Database schema management is configured through Hibernate:

```properties
spring.jpa.hibernate.ddl-auto=update
```

The application contains entities for users, customers, caterers, bookings, dishes, plate packages, selected booking dishes, and reviews.

## Configuration

Before running the application locally, configure the required environment variables.

### Database

```text
DB_URL=jdbc:mysql://localhost:3306/caters_springboot
DB_USERNAME=root
DB_PASSWORD=your_database_password
```

### Administrator

```text
ADMIN_PASSWORD=your_admin_password
```

Do not commit your real credentials to Git.

## Running the Project

### Prerequisites

* Java 17+
* MySQL
* Maven, or the included Maven Wrapper

### 1. Create the database

Create a MySQL database named:

```sql
CREATE DATABASE caters_springboot;
```

### 2. Configure environment variables

Set:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
ADMIN_PASSWORD
```

### 3. Run with Maven Wrapper

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Or with Maven:

```bash
mvn spring-boot:run
```

The application can then be accessed through the configured local server.

## Security & File Handling

User-uploaded media is stored outside the Git repository and excluded using `.gitignore`.

Sensitive environment configuration is also excluded from version control.

This repository intentionally does not contain production credentials or uploaded user files.

## Project Status

This is a portfolio project demonstrating a multi-role catering management application using the Spring Boot ecosystem.

The project is being actively improved with a focus on backend development, Spring Security, database relationships, business logic, and clean project structure.

## Author

**Lenen Glen**

Java / Spring Boot Developer

[GitHub](https://github.com/LenenCodes)
