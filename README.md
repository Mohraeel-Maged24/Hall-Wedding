# Hall Wedding Management System

> A team-developed desktop application for managing wedding halls, customers, services, and bookings in one place.

## About The Project

Hall Wedding is a desktop management system designed for wedding hall businesses. It provides separate workflows for administrators and customers, connects to a MySQL database, and uses JPA to persist the application's data.

The project was developed as a team project for an advanced database application, combining a JavaFX user interface with a relational database and an object-relational mapping layer.

## Main Workflows

### Admin

- Log in to the administration area.
- View the admin dashboard.
- Add, edit, and delete halls.
- Manage available wedding services.
- Manage customer records.
- Review and manage bookings.

### Customer

- Create or use a customer account.
- Browse available halls and services.
- Create a booking with the selected hall, date, duration, and services.
- Review previous bookings.

## Features

- Welcome screen and login flow
- Admin and customer workflows
- Hall CRUD operations
- Service CRUD operations
- Customer management
- Booking creation and management
- Customer booking history
- Database seeding for an empty database
- JavaFX screens with shared application styling
- JPA entities and persistence through EclipseLink

## Team

This project was developed collaboratively by a team. Add the final team member names below before submitting the project:

| Member | 
| --- | 
| Basmala Ayman Omer | 
| Makary Nour Zaki | 
| Mohraeel Maged Shawky | 
| Youssef Medhat | 

## Technology Stack

| Technology | Version / Purpose |
| --- | --- |
| Java | 21 |
| JavaFX | 25.0.1, desktop user interface |
| Jakarta Persistence | 3.1, persistence API |
| EclipseLink | 4.0.2, JPA provider |
| MySQL | Relational database |
| ControlsFX | 11.2.1, additional JavaFX controls |

## Requirements

- JDK 21 or newer
- MySQL Server
- A database named `hall_wedding`

## Database Setup

1. Start MySQL Server.
2. Create the database:

```sql
CREATE DATABASE hall_wedding;
```

3. Open `src/META-INF/persistence.xml`.
4. Update the database username and password to match your local MySQL configuration.

The default connection points to:

```text
Host: localhost:3306
Database: hall_wedding
```

The application is configured to create or extend the required tables through JPA when it starts. Do not commit production database credentials to a public repository.

## Build And Run

From the project directory, install dependencies and create the JAR with:

```bash
mvn clean package
```

Run the application from your IDE using:

```text
hall_wedding.Hall_Wedding
```

The packaged application is generated in `target/`. The project can also be opened and built with NetBeans using the included `build.xml` and `nbproject` configuration.

## Project Architecture

```text
src/
├── controllers/       Controllers for admin, customer, hall, service, booking, and login actions
├── hall_wedding/      Entities, database utilities, seeding, navigation, and application startup
├── views/             JavaFX screens
├── viewIcon/          Images and view resources
└── META-INF/          JPA persistence configuration
```

The application follows a simple layered structure:

1. **Views** display the JavaFX screens and collect user input.
2. **Controllers** handle actions and coordinate application behavior.
3. **Entities** represent customers, admins, halls, services, and bookings.
4. **JPA/EclipseLink** maps entities to the MySQL database.

## Repository

GitHub: [Hall-Wedding](https://github.com/Mohraeel-Maged24/Hall-Wedding)
