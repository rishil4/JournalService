# Microservices System: User Management and Journaling

This project consists of two Spring Boot microservices: User Service and Journal Service, which communicate via Kafka.

## Prerequisites

- Java 8 or later
- Gradle
- Docker and Docker Compose

## Running the System

1. Open a terminal and navigate to the user-service directory:
   ```
   cd path/to/user-service
   ```

2. Build the user-service:
   ```
   ./gradlew build
   ```

3. Navigate to the journal-service directory:
   ```
   cd ../journal-service
   ```

4. Build the journal-service:
   ```
   ./gradlew build
   ```

5. Navigate back to the parent directory:
   ```
   cd ..
   ```

6. Start the entire system using Docker Compose:
   ```
   docker-compose up --build
   ```

To stop the services, press Ctrl+C in the terminal where Docker Compose is running, or run:
```
docker-compose down
```

## Notes

- This setup uses in-memory H2 databases for simplicity. For a production environment, you should use a persistent database.
- The services are configured to use `localhost` for Kafka. In a real distributed environment, you'd need to adjust the Kafka configuration.
