# FitTrack

Gym trainers create workout and diet plans for clients. Clients tick off the plan every day, log their weight and see their progress.

Stack: Java 17, Spring Boot 3, Maven, MongoDB, plain HTML/CSS/JavaScript, Chart.js (CDN).

## How to run

1. Install Java 17, Maven and MongoDB. Start MongoDB on the default port (27017).
2. Open a terminal in this folder and run:
   ```
   mvn spring-boot:run
   ```
3. Open http://localhost:8080
4. Quick health check: http://localhost:8080/api/test (should show `"mongodb":"connected"`).

The database `fittrack` and its collections (`users`, `plans`, `logs`) are created automatically. To use another MongoDB address, change `spring.data.mongodb.uri` in `src/main/resources/application.properties`.

## Try the full flow

1. Register a trainer (role: Trainer).
2. Logout, register two clients (role: Client) and pick that trainer in the dropdown.
3. Login as the trainer. Open **Create Plan**, choose a client, add exercises and meals, save.
4. Login as a client. Tick items in **Today's Plan**, then go to **Log Progress**, enter weight and submit.
5. Check **History** as the client, and **Client Progress** as the trainer.

## Project structure

```
src/main/java/com/fittrack
  FitTrackApplication.java
  model        User, Trainer, Client, Exercise, Meal, Plan, ProgressLog
  dto          RegisterRequest, LoginRequest, LogRequest
  repository   UserRepository, PlanRepository, LogRepository
  service      AuthService, PlanService, ProgressService,
               PlanCalculator, WeeklyAdherenceCalculator
  controller   AuthController, TrainerController, ClientController, TestController
  exception    InvalidInputException, UserNotFoundException,
               AuthenticationException, GlobalExceptionHandler
  util         PasswordUtil
src/main/resources
  application.properties
  static       index.html, trainer.html, client.html, style.css, js/
```

## Class diagram (text)

```
            +---------------------------+
            |   <<abstract>> User       |
            |---------------------------|
            | - id, name, email,        |
            |   passwordHash            |
            | + getDashboardPath()  {abstract}
            | + getRole()           {abstract}
            +-------------^-------------+
                          |
           +--------------+--------------+
           |                             |
   +-------+--------+          +---------+--------+
   |    Trainer     |          |      Client      |
   |----------------|          |------------------|
   | "trainer.html" |          | - trainerId      |
   | "TRAINER"      |          | "client.html"    |
   +----------------+          | "CLIENT"         |
                               +------------------+

   +------------------+   has many   +-------------+
   |      Plan        |<>----------->|  Exercise   |  name, sets, reps
   |------------------|              +-------------+
   | id, name,        |   has many   +-------------+
   | durationWeeks,   |<>----------->|    Meal     |  name, calories
   | trainerId,       |              +-------------+
   | clientId,        |
   | exercises, meals |
   +------------------+

   +------------------+
   |   ProgressLog    |  clientId, planId, date, completedExercises,
   +------------------+  completedMeals, completionPercent, weight, notes

   <<interface>> PlanCalculator
     + calculateAdherence(logs)
              ^
              |
   WeeklyAdherenceCalculator
```

## Where each OOP concept is used

| Concept | Where |
|---|---|
| Abstraction | `User` is abstract with abstract `getDashboardPath()` and `getRole()` |
| Inheritance | `Trainer` and `Client` extend `User` |
| Polymorphism | `Trainer` and `Client` return different values from `getDashboardPath()` and `getRole()`. `AuthService.login()` returns a `User`, and the frontend redirects to `user.dashboardPath` |
| Encapsulation | All model fields are private with getters and setters. Validation lives in the setters (`User`, `Client`, `Exercise`, `Meal`, `Plan`, `ProgressLog`) |
| Interface | `PlanCalculator` with `calculateAdherence()`, implemented by `WeeklyAdherenceCalculator` |
| Composition | `Plan` holds a `List<Exercise>` and a `List<Meal>` |
| Custom exceptions | `InvalidInputException`, `UserNotFoundException`, `AuthenticationException`, all handled in `GlobalExceptionHandler` (`@RestControllerAdvice`) which returns JSON like `{"status":400,"error":"Bad Request","message":"..."}` |
| Collections | `List` for plans, exercises, meals and logs. `Map` for adherence results and per-client averages. `Set` for matching ticked items |

## API

| Method | Path | Purpose |
|---|---|---|
| POST | /api/auth/register | Create a trainer or client |
| POST | /api/auth/login | Login, returns the user |
| GET | /api/auth/trainers | Trainer list for the register dropdown |
| GET | /api/trainer/{id}/clients | A trainer's clients |
| GET | /api/trainer/{id}/adherence | Average adherence per client, last 7 days |
| POST | /api/plans | Create a plan |
| GET | /api/plans?trainerId= | A trainer's plans |
| GET | /api/plans/{id} | One plan |
| PUT | /api/plans/{id} | Update a plan |
| DELETE | /api/plans/{id} | Delete a plan |
| GET | /api/clients/{id}/plan | A client's current plan (204 if none) |
| POST | /api/clients/{id}/logs | Save today's log (saving again the same day updates it) |
| GET | /api/clients/{id}/logs | A client's logs |
| GET | /api/clients/{id}/adherence | A client's weekly adherence |

## MongoDB

- Database `fittrack`, collections `users`, `plans`, `logs`.
- Plans embed their exercises and meals.
- Unique index on `users.email`. Unique compound index on `logs (clientId, date)`.
- Aggregation (`ProgressService.weeklyAdherenceByClient`): `$match` logs of the last 7 days, `$group` by client and sum the completion percent, then divide by 7.
- Passwords are hashed with SHA-256 (`PasswordUtil`).

## How adherence works

Each log stores the percent of plan items ticked that day. Weekly adherence is the sum of the last 7 days divided by 7, so a missed day counts as 0%.

## Notes

- This is a learning project. SHA-256 without a salt and the missing login tokens are not production-safe.
- A client sees their most recently created plan.
