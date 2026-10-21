# Code

Everything that builds and runs lives here. The report material is in [../report](../report).

| Path | What goes there |
| --- | --- |
| `pom.xml`, `mvnw`, `src/` | The Spring Boot project (see [tech-stack.md](../report/tech-stack.md#setting-up-the-project)) |
| `postman/` | The Postman collection, our simulated front end |
| `data/` | The JSON files behind the repository adapters, created when the app first saves something |

## Running it

You need a JDK 21 or newer. Maven comes with the wrapper. From this folder:

| Command | What it does |
| --- | --- |
| `./mvnw verify` | Compiles, runs every test and builds `target/gym-membership-*.jar`. CI runs the same command on every pull request |
| `./mvnw spring-boot:run` | Starts the app on http://localhost:8080. Check http://localhost:8080/actuator/health returns `UP` |

## Packages

Package by layer with Hexagonal ports and adapters. The reasoning is in [report/architecture](../report/architecture/README.md#package-structure). Each package has a `package-info.java` saying what belongs in it.

| Package | Holds | May use |
| --- | --- | --- |
| `gym.controller` | REST controllers, `ErrorHandler` | `port.in`, `dto`, `domain` |
| `gym.dto` | Request and response records | `domain` |
| `gym.port.in` | Use-case interfaces | `domain` |
| `gym.port.out` | `Repository<T, ID>` and other outbound interfaces | `domain` |
| `gym.service` | Use-case implementations | `port.in`, `port.out`, `domain` |
| `gym.domain` | Domain model, business rules, `NotFoundException`, `RuleViolationException` | The JDK only |
| `gym.adapter.persistence` | `JsonFileRepository` and each feature's JSON repository | `port.out`, `domain` |

`ArchitectureTest` checks these rules, so a wrong import fails the build. If it fails, fix the dependency rather than the test.

## Adding your feature

Taking Booking as the example:

1. `domain`: `GymClass`, `Booking`, `Waitlist` and their rules. Throw `RuleViolationException` when a rule is broken and `NotFoundException` for an unknown id. The `ErrorHandler` turns them into 422 and 404 responses.
2. `port.out`: `interface GymClassRepository extends Repository<GymClass, String>`.
3. `adapter.persistence`: `JsonFileGymClassRepository extends JsonFileRepository<GymClass, String> implements GymClassRepository`. The Javadoc on `JsonFileRepository` shows the constructor.
4. `port.in`: `BookClassUseCase`, taking ids and values rather than DTOs.
5. `service`: `BookingService implements BookClassUseCase`. Inject the `java.time.Clock` bean instead of calling `now()`, so tests can fix the date.
6. `dto` and `controller`: `BookingRequest`, `BookingResponse` and `BookingController`, which calls `BookClassUseCase`.
7. Tests: unit-test the rules in `domain` and `service` with an in-memory fake of the repository port, plus a test for your design pattern.
