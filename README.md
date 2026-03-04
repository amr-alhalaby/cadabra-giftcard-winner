# Cadabra Gift Card Winner Selector

A Spring Batch application that selects a gift card winner from Cadabra's online shoppers based on purchase data from a CSV file.

---

## How It Works

The batch job runs 3 steps sequentially:

1. **Step 1 – Fetch Users**: Fetches users from the [JSONPlaceholder API](https://jsonplaceholder.typicode.com/users) and upserts them into the database.
2. **Step 2 – Load Purchases**: Reads purchase records from the provided CSV file and writes them to the database, tagged with the current job execution ID.
3. **Step 3 – Select Winner**: Randomly selects one eligible winner from the purchases loaded in the **current run only**.

---

## Assumptions

- **User fetching is idempotent** — users are fetched from the API on every run. If a user already exists in the database, it is overwritten with the latest data. No duplicates are created.
- **Winner is calculated per CSV file** — each run processes one CSV file. The winner is selected only from the purchases loaded in that specific run, not from any previous runs.
- **A user can appear more than once in the same CSV** — if the same user has multiple rows in the CSV, their amounts are summed. A user is eligible to win if their total spend across all rows in that CSV exceeds **20 EUR**.
- **Winner isolation** — purchases from previous batch runs do not affect the winner selection. Each run is fully isolated via a `job_execution_id` column on the purchases table.
- **Winner is logged** — the winner's details (ID, name, username, email, phone) are printed to the application logs for simplicity. No separate output file or notification is produced.
- **Validation errors are persisted** — any rows that fail to parse or process are saved to the `validation_errors` table with the job execution ID, step name, file name, and error message for auditing.

---

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Batch
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Gradle
- Docker / Docker Compose

---

## Prerequisites

- Java 17+
- Docker & Docker Compose

---

## Running

### Place your CSV file in the `data/` folder

```
data/
├── january.csv
├── february.csv
└── purchases-external.csv   ← default
```

CSV format:
```csv
userId,amount
1,25.00
2,35.50
```

---

### Run with Docker (recommended)

```bash
# Run with default CSV (purchases-external.csv)
./run.sh

# Run with a specific CSV file
./run.sh january.csv

# Run and keep the database up for inspection afterwards
./run.sh january.csv --keep-db-up

# Run again while the database is already up (postgres already running)
./run.sh february.csv --rerun

# Tear down all containers when done
docker compose down
```

---

### Run Locally (without Docker)

Make sure PostgreSQL is running on `localhost:5432` with a database named `cadabra`.

```bash
# Run with default CSV
./gradlew bootRun

# Run with a specific CSV file
./gradlew bootRun --args="purchases.csv.path=/absolute/path/to/january.csv"
```

---

## Project Structure

```
├── src/main/java/org/example/
│   ├── CadabraGiftcardWinnerApplication.java   # Entry point
│   ├── config/
│   │   ├── GiftCardWinnerJobConfig.java         # Spring Batch job configuration
│   │   └── RestTemplateConfig.java              # REST client config
│   ├── common/
│   │   ├── Constants.java                       # Shared step/job name constants
│   │   └── ResourceResolver.java                # Classpath vs filesystem resource resolver
│   ├── steps/
│   │   ├── user/                                # Step 1: fetch users from API
│   │   ├── purchase/                            # Step 2: load purchases from CSV
│   │   └── winner/                              # Step 3: select random winner
│   ├── model/
│   │   ├── User.java
│   │   ├── Purchase.java                        # Includes job_execution_id for run isolation
│   │   └── ValidationError.java
│   ├── service/
│   │   ├── UserApiService.java
│   │   ├── WinnerSelectionService.java
│   │   └── ValidationErrorService.java
│   └── repository/
│       ├── UserRepository.java
│       ├── PurchaseRepository.java
│       └── ValidationErrorRepository.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                            # Flyway migrations
├── data/                                        # Drop CSV files here
├── docker-compose.yml
├── docker-entrypoint.sh                         # Injects -run.at timestamp for job rerunnability
├── Dockerfile
├── run.sh                                       # Convenience run script
└── README.md
```

---

## Useful Queries

### Job Monitoring & Alerts

```sql
-- All job executions with status and duration
SELECT job_execution_id,
       start_time,
       end_time,
       status,
       exit_code,
       EXTRACT(EPOCH FROM (end_time - start_time)) AS duration_seconds
FROM batch_job_execution
ORDER BY start_time DESC;

-- Failed job executions (use for alerts)
SELECT job_execution_id,
       start_time,
       exit_message
FROM batch_job_execution
WHERE status = 'FAILED'
ORDER BY start_time DESC;

-- Jobs that did not complete within the last 24 hours (stuck/hung jobs)
SELECT job_execution_id,
       start_time,
       status
FROM batch_job_execution
WHERE status NOT IN ('COMPLETED', 'FAILED')
  AND start_time < NOW() - INTERVAL '24 hours';

-- Winner per job execution (from execution context)
SELECT je.job_execution_id,
       je.start_time,
       je.status,
       jp.string_val                           AS csv_file,
       jec.short_context
FROM batch_job_execution je
         JOIN batch_job_execution_context jec ON je.job_execution_id = jec.job_execution_id
         JOIN batch_job_execution_params jp ON je.job_execution_id = jp.job_execution_id
WHERE jp.parameter_name = 'purchases.csv.path'
ORDER BY je.start_time DESC;
```

---

### Validation Errors

```sql
-- All validation errors
SELECT job_execution_id,
       step_name,
       file_name,
       raw_data,
       error_message,
       created_at
FROM validation_errors
ORDER BY created_at DESC;

-- Validation errors for a specific job execution
SELECT raw_data,
       error_message
FROM validation_errors
WHERE job_execution_id = :jobExecutionId;

-- Validation errors for a specific file
SELECT job_execution_id,
       raw_data,
       error_message,
       created_at
FROM validation_errors
WHERE file_name = :fileName
ORDER BY created_at DESC;

-- Count of errors per job execution (high error count = potential alert)
SELECT job_execution_id,
       file_name,
       COUNT(*) AS error_count
FROM validation_errors
GROUP BY job_execution_id, file_name
ORDER BY error_count DESC;

-- Job executions where errors exceeded a threshold (e.g. more than 5)
SELECT job_execution_id,
       file_name,
       COUNT(*) AS error_count
FROM validation_errors
GROUP BY job_execution_id, file_name
HAVING COUNT(*) > 5
ORDER BY error_count DESC;
```

---

## Scaling Strategy

### Partitioned Step — Parallel CSV Processing
Split the CSV into line ranges and process each range in a **separate thread within the same JVM**:

```
Single JVM
├── Master Step → splits file into N partitions
├── Worker 1   → processes lines 1–1000
├── Worker 2   → processes lines 1001–2000
└── Worker 3   → processes lines 2001–3000
```

Each worker runs the same `loadPurchasesStep` logic on its assigned range, sharing the same DB connection pool and transaction manager.

**When to use:** CSV files too large for a single thread but still running on one machine.

### Remote Partitioning — Distributed Workers
Extends partitioning across **multiple pods/machines** using a message broker (RabbitMQ / Kafka / SQS). The master sends partition metadata as messages; remote workers pick them up and process independently:

```
Master Pod
└── Sends partition messages → Message Broker
                                    ├── Worker Pod 1 → processes partition 1
                                    ├── Worker Pod 2 → processes partition 2
                                    └── Worker Pod 3 → processes partition 3
```

Each worker pod connects to the **same shared DB** and reports back to the master via a reply channel. Spring Batch tracks all partitions in `BATCH_STEP_EXECUTION` — the job only completes when **all partitions** finish.

**When to use:** Files too large for a single machine, or when workers need to scale independently (e.g. Kubernetes HPA).

**Dependencies needed:**
```groovy
implementation 'org.springframework.batch:spring-batch-integration'
implementation 'org.springframework.boot:spring-boot-starter-amqp'  // or kafka
```

