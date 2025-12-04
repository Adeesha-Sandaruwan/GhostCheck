# GhostCheck Deployment Guide

This guide explains how to run GhostCheck:
- Locally on your machine
- On Render.com (recommended free tier)
- Using Supabase as the external PostgreSQL database
- With environment variables for configuration

## Prerequisites
- Java 17+
- Maven 3.8+
- Git
- A Supabase account (for managed PostgreSQL)
- A Render.com account

## Environment Variables
Configure these variables for all deployments:

- SPRING_PROFILES_ACTIVE: profile name, e.g., "prod" (optional)
- SPRING_DATASOURCE_URL: JDBC URL to PostgreSQL
- SPRING_DATASOURCE_USERNAME: DB username
- SPRING_DATASOURCE_PASSWORD: DB password
- SPRING_JPA_HIBERNATE_DDL_AUTO: update | validate | none (recommend "update" for demos)
- SERVER_PORT: port to run the app (defaults to 8080)
- APP_BRANDING_NAME: optional, defaults to "GhostCheck"

Example values (Supabase PostgreSQL):
- SPRING_DATASOURCE_URL=jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require
- SPRING_DATASOURCE_USERNAME=postgres
- SPRING_DATASOURCE_PASSWORD=<your-db-password>
- SPRING_JPA_HIBERNATE_DDL_AUTO=update

Note: Supabase exposes SSL. If needed, add "?sslmode=require" to the JDBC URL:
jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require

### Using your Supabase connection
Do not hardcode credentials. Use environment variables.

- JDBC URL template:
  jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require

- Example (macOS/Linux):
  export SPRING_DATASOURCE_URL="jdbc:postgresql://db.qdulppoyouncbrivakic.supabase.co:5432/postgres?sslmode=require"
  export SPRING_DATASOURCE_USERNAME="postgres"
  export SPRING_DATASOURCE_PASSWORD="<REDACTED_PASSWORD>"
  export SPRING_JPA_HIBERNATE_DDL_AUTO="update"

- Example (Windows PowerShell):
  $env:SPRING_DATASOURCE_URL="jdbc:postgresql://db.qdulppoyouncbrivakic.supabase.co:5432/postgres?sslmode=require"
  $env:SPRING_DATASOURCE_USERNAME="postgres"
  $env:SPRING_DATASOURCE_PASSWORD="<REDACTED_PASSWORD>"
  $env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"

## 1) Run Locally

### A) Use local PostgreSQL (recommended for dev)
1. Start PostgreSQL locally and create a database:
   - Database: ghostcheck
   - User: ghostcheck
   - Password: ghostcheck

2. Set environment variables:
   - On macOS/Linux:
     export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ghostcheck
     export SPRING_DATASOURCE_USERNAME=ghostcheck
     export SPRING_DATASOURCE_PASSWORD=ghostcheck
     export SPRING_JPA_HIBERNATE_DDL_AUTO=update
     export SERVER_PORT=8080

   - On Windows (PowerShell):
     $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/ghostcheck"
     $env:SPRING_DATASOURCE_USERNAME="ghostcheck"
     $env:SPRING_DATASOURCE_PASSWORD="ghostcheck"
     $env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"
     $env:SERVER_PORT="8080"

3. Build and run:
   mvn clean package
   mvn spring-boot:run

4. Open in browser:
   http://localhost:8080

### B) Use Supabase locally
1. Get Supabase DB connection info:
   - Host: db.<YOUR_PROJECT>.supabase.co
   - Port: 5432
   - DB: postgres
   - User: postgres
   - Password: your Supabase password

2. Set env vars:
   - macOS/Linux:
     export SPRING_DATASOURCE_URL="jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require"
     export SPRING_DATASOURCE_USERNAME="postgres"
     export SPRING_DATASOURCE_PASSWORD="<your-password>"
     export SPRING_JPA_HIBERNATE_DDL_AUTO=update

   - Windows:
     $env:SPRING_DATASOURCE_URL="jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require"
     $env:SPRING_DATASOURCE_USERNAME="postgres"
     $env:SPRING_DATASOURCE_PASSWORD="<your-password>"
     $env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"

3. Run:
   mvn spring-boot:run

## 2) Deploy on Render.com (Free Tier)

### A) Prepare the repository
- Ensure your project builds with: mvn clean package
- The built artifact will be target/ghostcheck-*.jar

### B) Create a Web Service on Render
1. Login to Render and click "New" → "Web Service".
2. Connect your GitHub repo containing GhostCheck.
3. Configure:
   - Name: ghostcheck
   - Region: any
   - Runtime: Docker (optional) or directly from build command
   - Build Command: mvn clean package
   - Start Command: java -jar target/ghostcheck-*.jar
   - Instance Type: Free

4. Add Environment Variables:
   - SPRING_DATASOURCE_URL=jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require
   - SPRING_DATASOURCE_USERNAME=postgres
   - SPRING_DATASOURCE_PASSWORD=<your-password>
   - SPRING_JPA_HIBERNATE_DDL_AUTO=update
   - SERVER_PORT=10000 (Render dynamically sets PORT env var; you can map SERVER_PORT to $PORT using Render’s "Environment" feature if preferred)
   - Optionally: APP_BRANDING_NAME=GhostCheck

5. Click "Create Web Service" to deploy.

Notes:
- Render sets PORT automatically (e.g., $PORT). You can set Start Command to:
  java -Dserver.port=$PORT -jar target/ghostcheck-*.jar
- Ensure outbound access is allowed (default is allowed) to connect to Supabase.

### C) Health Check and Logs
- After deploy, check "Events" and "Logs" tabs for build and runtime details.
- Visit the public Render URL to access GhostCheck.

## 3) Provision Supabase PostgreSQL

### A) Create a Supabase project
1. Login to Supabase and create a new project.
2. Choose a region and a strong database password.

### B) Get connection details
- Host: db.<YOUR_PROJECT>.supabase.co
- Port: 5432
- Database: postgres
- User: postgres
- Password: the one you created

### C) Configure SSL
- Use sslmode=require in the JDBC URL to ensure encrypted connection:
  jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require

### D) Schema creation
- Spring JPA with "SPRING_JPA_HIBERNATE_DDL_AUTO=update" will create tables on first run.
- For production-grade control, set to "validate" and manage schema via Flyway/Liquibase (optional).

## 4) Profiles and Config

### A) application.properties (example)
You can rely solely on env vars, but if you prefer properties:
- ...existing code...
- spring.datasource.url=${SPRING_DATASOURCE_URL}
- spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
- spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
- spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
- server.port=${SERVER_PORT:8080}
- ...existing code...

### B) Using profiles
- Set SPRING_PROFILES_ACTIVE=prod (optional).
- Provide profile-specific overrides via application-prod.properties if needed.
- ...existing code...

## 5) Security & Production Tips
- Never commit secrets. Use Render’s and Supabase’s environment variable managers.
- Restrict database users to least privilege in production.
- Prefer Flyway for schema migrations.
- Add proper logging and monitoring (Render "Logs" or external tools).
- Configure CORS if exposing API endpoints publicly.
- Use HTTPS-only links to Render URL.

## Security Notes
- Never paste raw passwords or full connection strings into source control.
- Prefer Render’s and local shell environment variable management for secrets.

## 6) Troubleshooting
- Database connection failures:
  - Verify host, port, user, password.
  - Ensure sslmode=require in Supabase JDBC URL.
- App fails to bind port on Render:
  - Use java -Dserver.port=$PORT -jar target/ghostcheck-*.jar
- Tables not created:
  - Check SPRING_JPA_HIBERNATE_DDL_AUTO=update
  - Confirm entities are scanned (packages under com.ghostcheck).
### Lombok not found / cannot find symbol: Getter, Builder, etc.
If you see compilation errors like “package lombok does not exist” or “cannot find symbol: Getter/Builder/…”, ensure Lombok is added and annotation processing is enabled.

1) Add Lombok and annotation processing to Maven pom.xml:

```xml
<!-- In pom.xml -->
<dependencies>
  <!-- ...existing dependencies... -->

  <!-- Lombok for annotations like @Getter, @Builder -->
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <!-- ...existing build plugins... -->
  <plugins>
    <!-- Ensures annotation processing is enabled for Lombok -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
          <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

2) IDE configuration:
- IntelliJ IDEA:
  - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing.
  - Ensure Lombok plugin is installed (Settings → Plugins → search “Lombok”).
- Eclipse:
  - Project → Properties → Java Compiler → Annotation Processing → Enable project specific settings → Enable annotation processing.
  - Install Lombok by running the downloaded lombok.jar and integrating with Eclipse.

3) Clean and rebuild:
- mvn clean compile
- If using IDE, invalidate caches/restart (IntelliJ) after adding Lombok.

4) Verify entity accessors:
- With Lombok in place, annotations like @Getter/@Setter/@Builder will generate methods used across controllers/services (e.g., getEmail(), builder()).

## 7) Quick Commands Summary

Local (PostgreSQL):
- export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ghostcheck
- export SPRING_DATASOURCE_USERNAME=ghostcheck
- export SPRING_DATASOURCE_PASSWORD=ghostcheck
- export SPRING_JPA_HIBERNATE_DDL_AUTO=update
- mvn spring-boot:run

Render Start Command:
- java -Dserver.port=$PORT -jar target/ghostcheck-*.jar

Supabase JDBC URL:
- jdbc:postgresql://db.<YOUR_PROJECT>.supabase.co:5432/postgres?sslmode=require

Happy scanning with GhostCheck!
