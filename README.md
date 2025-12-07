A Spring Boot web app that parses the XML file exported from Tabbycat and then stores the information in a database. The intention is to make it easier to keep track of debaters throughout time and analyze their performance. Also could be used to track adjudicators as well.


## Setup

Follow these steps to run the project on Windows.

### Prerequisites
- Java 21 (set `JAVA_HOME`).
- Maven 3.6+

### Clone the repository
```bash
git clone https://github.com/DDH13/debateTracker.git
cd debateTracker
```

### Configure the database
The project defaults to an in-memory H2 database for development. To use PostgreSQL, set database properties in src/main/resources/application.properties or via environment variables:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=myuser
spring.datasource.password=mypassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Build and run the application
```bash
mvn clean install
mvn spring-boot:run
```

### Access the application
Open your web browser and navigate to `http://localhost:8080`.
For documentation and API endpoints, visit `http://localhost:8080/swagger-ui.html`.

