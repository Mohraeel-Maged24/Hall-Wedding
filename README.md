# Hall Wedding

A desktop wedding hall management system built with JavaFX, Jakarta Persistence (JPA), EclipseLink, and MySQL.

## Features

- Welcome screen and user login
- Customer management
- Wedding hall management
- Service management
- Booking management
- Customer booking history
- Admin dashboard
- Automatic database seeding when the database is empty
- JavaFX views with reusable application themes

## Technology Stack

- Java 21
- JavaFX 25.0.1
- Jakarta Persistence 3.1
- EclipseLink 4.0.2
- MySQL
- Maven
- ControlsFX 11.2.1

## Requirements

- JDK 21 or newer
- Maven 3.8 or newer
- MySQL Server
- A database named `hall_wedding`

## Database Setup

1. Start MySQL Server.
2. Create the database:

```sql
CREATE DATABASE hall_wedding;
```

3. Open `src/META-INF/persistence.xml` and update the database username and password if needed. The current configuration uses:

```text
Host: localhost:3306
Database: hall_wedding
Username: root
Password: 3306
```

Change these values before running the application when they do not match your local MySQL setup.

## Build

From the project directory, run:

```bash
mvn clean package
```

The compiled JAR is generated in `target/`.

## Run

Run the application from your IDE using the main class:

```text
hall_wedding.Hall_Wedding
```

The project can also be opened and built with NetBeans using the included `build.xml` and `nbproject` configuration.

## Project Structure

```text
src/
├── controllers/       Application controllers
├── hall_wedding/      Entities, database utilities, and application startup
├── views/             JavaFX screens
├── viewIcon/          Images and view resources
└── META-INF/          JPA persistence configuration
```

## Notes

- The application creates or extends the required database tables through the JPA configuration.
- Do not commit production database credentials to a public repository.
