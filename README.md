<<<<<<< HEAD
# GhostCheck: Digital Footprint & Risk Radar 👻

GhostCheck is a Spring Boot application designed to help users assess their digital risk. It provides tools to scan for public breaches, analyze password reuse patterns (securely), and maintain a history of digital footprint checks.

## Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.x
* **Frontend:** Thymeleaf + Tailwind CSS (via CDN/local)
* **Database:** PostgreSQL (Supabase)
* **Migration:** Flyway
* **Build:** Maven

## Getting Started

### Prerequisites

1.  Java 17 SDK installed.
2.  Maven installed.
3.  A Supabase project (or local PostgreSQL instance).

### Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-repo/ghostcheck.git](https://github.com/your-repo/ghostcheck.git)
    cd ghostcheck
    ```

2.  **Configure Environment:**
    Copy `application.properties` or set the following environment variables for Supabase connection:
    * `SPRING_DATASOURCE_URL`
    * `SPRING_DATASOURCE_USERNAME`
    * `SPRING_DATASOURCE_PASSWORD`

3.  **Lombok:**
    Ensure your IDE (IntelliJ/Eclipse) has the Lombok plugin installed and annotation processing enabled.

### Running the App

```bash
mvn spring-boot:run
=======
# GhostCheck
The app is Digital Footprint &amp; Risk Radar, built with Java 17, Spring Boot, Thymeleaf frontend, and Supabase (Postgres) database.
>>>>>>> 2b6b68012f71166653eb1b3f32b6ba86ae26c040
