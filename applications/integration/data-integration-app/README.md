# Data Integration App

A Spring Boot application that moves sepsis patient vital-sign data from a public dataset, through RabbitMQ, into PostgreSQL. It is the data-integration step of the low-latency AI data at the edge solution.

## How It Works

```text
PhysioNet dataset ──HTTP──▶ SepsisDatasetDownloader ──▶ RabbitMQ stream ──▶ Spring Integration flow ──▶ PostgreSQL
 (one .psv per patient)      (scheduled, with cursor)    sepsis.vitals.queue   (ConsumerConfig)          (JPA entity)
```

1. **Download (scheduled).** `SepsisDatasetDownloader` runs on a cron schedule. Each run downloads the next batch of patient files (`p000001.psv`, `p000002.psv`, ...) from the [PhysioNet Challenge 2019](https://physionet.org/content/challenge-2019/1.0.0/) training set (open access).
2. **Parse and publish.** `SepsisPsvParser` converts each file into `SepsisVitalHistory` records. Each record is published as JSON to the `sepsis.vitals.queue` RabbitMQ stream.
3. **Consume and store.** A Spring Integration flow (`ConsumerConfig`) reads the queue, deserializes each message, and saves it through `SepsisVitalHistoryRepository` as a `SepsisVitalHistoryEntity`.

### Resuming

A cursor file (`sepsis-dataset-cursor.txt`) records the last patient that was fully processed. If a download fails, the run stops at that patient and the next run resumes there. When the last patient (`max-patient-number`) is reached, runs do nothing.

## Prerequisites

- Java 21
- PostgreSQL, reachable at `localhost:5433` (database `postgresml`)
- RabbitMQ with stream support (for example, Tanzu RabbitMQ)
- The Gradle multi-project build, since this app depends on `:components:domains`

## Configuration

Settings are in [application.properties](src/main/resources/application.properties).

### Environment variables

| Variable                  | Purpose           |
|---------------------------|-------------------|
| `TANZU_RABBITMQ_USER`     | RabbitMQ username |
| `TANZU_RABBITMQ_PASSWORD` | RabbitMQ password |

The datasource username is `postgresml`. Set `spring.datasource.password` (for example, with `SPRING_DATASOURCE_PASSWORD`), because no password is configured in the file.

### Dataset properties

| Property                           | Default                  | Description                                       |
|------------------------------------|--------------------------|---------------------------------------------------|
| `sepsis.dataset.cron`              | `0 0 2 * * *`            | Schedule for each download run (daily at 02:00)   |
| `sepsis.dataset.base-url`          | PhysioNet `training_setA` | Location of the `.psv` patient files             |
| `sepsis.dataset.patients-per-run`  | `100`                    | Patients processed in each run                    |
| `sepsis.dataset.max-patient-number`| `20336`                  | Last patient number in the dataset                |
| `sepsis.dataset.cursor-file`       | `sepsis-dataset-cursor.txt` | File that tracks progress                      |

### Other notes

- `server.port=0` picks a random free port.
- `spring.jpa.hibernate.ddl-auto=update` creates and updates the schema on startup.
- `spring.jpa.show-sql=true` logs the generated SQL.

## Build and Run

Run these from the repository root. Adjust the project path if your Gradle settings differ.

```bash
export TANZU_RABBITMQ_USER=<user>
export TANZU_RABBITMQ_PASSWORD=<password>

./gradlew :applications:integration:data-integration-app:bootRun
```

To try it without waiting for 02:00, set a more frequent schedule, for example `--sepsis.dataset.cron="0 * * * * *"` to run every minute.

### Tests

```bash
./gradlew :applications:integration:data-integration-app:test
```

### Native image

The build includes the GraalVM native build tools plugin:

```bash
./gradlew :applications:integration:data-integration-app:nativeCompile
```

## Message Format

Each message on `sepsis.vitals.queue` is a JSON document like this:

```json
{
  "id": "VT-2026-0902-8841",
  "heartRate": 115.5,
  "systolicBp": 88.0,
  "diastolicBp": 54.0,
  "meanArterialPressure": 65.3,
  "respiratoryRate": 24.0,
  "spo2": 93.5,
  "temperature": 38.6,
  "wbcCount": 14.8,
  "lactate": 3.2,
  "hasSepsis": true
}
```

| Field                  | Description                       |
|------------------------|-----------------------------------|
| `id`                   | Unique record identifier          |
| `heartRate`            | Heart rate (beats/min)            |
| `systolicBp`           | Systolic blood pressure (mmHg)    |
| `diastolicBp`          | Diastolic blood pressure (mmHg)   |
| `meanArterialPressure` | Mean arterial pressure (mmHg)     |
| `respiratoryRate`      | Respiratory rate (breaths/min)    |
| `spo2`                 | Oxygen saturation (%)             |
| `temperature`          | Body temperature (°C)             |
| `wbcCount`             | White blood cell count (10³/µL)   |
| `lactate`              | Lactate (mmol/L)                  |
| `hasSepsis`            | Whether sepsis is present         |

## Tech Stack

Spring Boot 4.1, Spring Integration (AMQP), Spring AMQP and RabbitMQ streams, Spring Cloud Stream, Spring Data JPA, PostgreSQL, and Lombok.

## License

See [LICENSE](LICENSE).
