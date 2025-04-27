### StatisticsService

## About

Микросервис статистики для управления и получения данных о здоровье и физической активности, включая GPS, сердечный ритм и калории.

Default configuration for this microservice:

## Environment Setup

Create an `.env` file in the project root with the following keys:

```
MODE=LOCAL
PORT=8080

CLICKHOUSE_SERVICE_URL=jdbc:clickhouse://localhost:8080/test_db
DATABASE_USER=default
DATABASE_PASSWORD=

```

```plaintext
ktor {
  deployment {
    port = ${?PORT}
  }
  application {
    modules = [ com.mad.statistics.ApplicationKt.module ]
  }
}
```

Default port for this service is 8082. (Application.kt)

## Start:

Download ClickHouse


<https://clickhouse.com/docs/getting-started/quick-start>


Connect to ClickHouse

```plaintext
./clickhouse client --user default
```

```plaintext
CREATE DATABASE test_db;
```

```plaintext
USE test_db
```

Create tables

table gps_data:

```plaintext
CREATE TABLE IF NOT EXISTS gps_data (
    id String,
    exercise_id String,
    timestamp DateTime64(3),
    position_timestamp DateTime64(3),
    latitude Float64,
    longitude Float64,
    altitude Float64,
    speed Float64,
    accuracy Float64
) ENGINE = MergeTree()
ORDER BY (exercise_id, timestamp)
SETTINGS index_granularity = 8192;
```

table heart_rate_data

```plaintext
CREATE TABLE IF NOT EXISTS heart_rate_data (
    id String,
    exercise_id String,
    timestamp DateTime64(3),
    bpm Int32
) ENGINE = MergeTree()
ORDER BY (exercise_id, timestamp)
SETTINGS index_granularity = 8192;
```

table calories_data

```plaintext

CREATE TABLE IF NOT EXISTS calories_data (
    id String,
    user_id String,
    timestamp DateTime64(3),
    calories Float64
) ENGINE = MergeTree()
ORDER BY (user_id, timestamp)
SETTINGS index_granularity = 8192;
```

## Routes:

### GPS Routes:

- GET `/api/statistics/gps?exercise_id={exerciseId}` - Get GPS data for a specific exercise
- POST `/api/statistics/gps` - Upload GPS data for an exercise

### Heart Rate Routes:

- GET `/api/statistics/heartrate?exercise_id={exerciseId}` - Get heart rate data for a specific exercise
- POST `/api/statistics/heartrate` - Upload heart rate data for an exercise

### Calories Routes:

- GET `/api/statistics/calories?user_id={userId}` - Get calories data for a specific user
- POST `/api/statistics/calories` - Upload calories data for a user

## Query examples

### GPS Data

#### Get GPS data by exercise ID

URL (body is empty):

```plaintext
GET http://localhost:8082/api/statistics/gps?exercise_id=exercise-001
```

Response:

```json
{
  "gps_data": [
    {
      "meta": {
        "id": "gps-123",
        "exerciseId": "exercise-001",
        "timestamp": "2023-08-15T10:00:00Z"
      },
      "positions": [
        {
          "timestamp": "2023-08-15T10:00:05Z",
          "latitude": 55.7558,
          "longitude": 37.6173,
          "altitude": 120.5,
          "speed": 5.2,
          "accuracy": 3.0
        },
        {
          "timestamp": "2023-08-15T10:00:10Z",
          "latitude": 55.7559,
          "longitude": 37.6175,
          "altitude": 121.0,
          "speed": 5.5,
          "accuracy": 2.8
        }
      ]
    }
  ]
}
```

#### Upload GPS data

URL:

```plaintext
POST http://localhost:8082/api/statistics/gps
```

Body:

```json
{
  "meta": {
    "id": "gps-123",
    "exerciseId": "exercise-001",
    "timestamp": "2023-08-15T10:00:00Z"
  },
  "positions": [
    {
      "timestamp": "2023-08-15T10:00:05Z",
      "latitude": 55.7558,
      "longitude": 37.6173,
      "altitude": 120.5,
      "speed": 5.2,
      "accuracy": 3.0
    },
    {
      "timestamp": "2023-08-15T10:00:10Z",
      "latitude": 55.7559,
      "longitude": 37.6175,
      "altitude": 121.0,
      "speed": 5.5,
      "accuracy": 2.8
    }
  ]
}
```

Response:

```plaintext
200 OK
```

### Heart Rate Data

#### Get heart rate data by exercise ID

URL (body is empty):

```plaintext
GET http://localhost:8082/api/statistics/heartrate?exercise_id=exercise-001
```

Response:

```json
{
  "heart_rate_data": [
    {
      "meta": {
        "id": "hr-123",
        "exerciseId": "exercise-001",
        "timestamp": "2023-08-15T10:00:15Z"
      },
      "bpm": 75
    },
    {
      "meta": {
        "id": "hr-124",
        "exerciseId": "exercise-001",
        "timestamp": "2023-08-15T10:01:15Z"
      },
      "bpm": 78
    }
  ]
}
```

#### Upload heart rate data

URL:

```plaintext
POST http://localhost:8082/api/statistics/heartrate
```

Body:

```json
{
  "meta": {
    "id": "hr-123",
    "exerciseId": "exercise-001",
    "timestamp": "2023-08-15T10:00:15Z"
  },
  "bpm": 75
}
```

Response:

```plaintext
200 OK
```

### Calories Data

#### Get calories data by user ID

URL (body is empty):

```plaintext
GET http://localhost:8082/api/statistics/calories?user_id=user-001
```

Response:

```json
{
  "calories_data": [
    {
      "meta": {
        "id": "cal-123",
        "userId": "user-001",
        "timestamp": "2023-08-15T10:30:00Z"
      },
      "calories": 250.5
    },
    {
      "meta": {
        "id": "cal-124",
        "userId": "user-001",
        "timestamp": "2023-08-15T11:30:00Z"
      },
      "calories": 150.3
    }
  ]
}
```

#### Upload calories data

URL:

```plaintext
POST http://localhost:8082/api/statistics/calories
```

Body:

```json
{
  "meta": {
    "id": "cal-123",
    "userId": "user-001",
    "timestamp": "2023-08-15T10:30:00Z"
  },
  "calories": 250.5
}
```

Response:

```plaintext
200 OK
```

## Data Models

### GPS Data

```kotlin
data class GPSData(
    val meta: ExerciseMetadata,
    val positions: List<GPSPosition>
)

data class ExerciseMetadata(
    val id: String,
    val exerciseId: String,
    val timestamp: Instant
)

data class GPSPosition(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val accuracy: Double
)
```

### Heart Rate Data

```kotlin
data class HeartRateData(
    val meta: ExerciseMetadata,
    val bpm: Int
)
```

### Calories Data

```kotlin
data class CaloriesData(
    val meta: UserMetadata,
    val calories: Double
)

data class UserMetadata(
    val id: String,
    val userId: String,
    val timestamp: Instant
)
```

## SQL

### GPS Data Table

```sql
CREATE TABLE IF NOT EXISTS gps_data (
    id VARCHAR(255) PRIMARY KEY,
    exercise_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    position_timestamp TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_gps_exercise_id ON gps_data(exercise_id);
```

### Heart Rate Data Table

```sql
CREATE TABLE IF NOT EXISTS heart_rate_data (
    id VARCHAR(255) PRIMARY KEY,
    exercise_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    bpm INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_heart_rate_exercise_id ON heart_rate_data(exercise_id);
```

### Calories Data Table

```sql
CREATE TABLE IF NOT EXISTS calories_data (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    calories DOUBLE PRECISION NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_calories_user_id ON calories_data(user_id);
```
