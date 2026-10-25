# Code

Everything that builds and runs lives here. The report material is in [../report](../report).

| Path | What goes there |
| --- | --- |
| `pom.xml`, `mvnw`, `src/` | The Spring Boot project (see [tech-stack.md](../report/tech-stack.md#setting-up-the-project)) |
| `postman/` | The Postman collection, our simulated front end |
| `data/` | The JSON files behind the repository adapters, created when the app first saves something. Not committed |

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
| `gym.port.out` | `Repository<T, ID>`, `PaymentGateway`, `NotificationSender` | `domain` |
| `gym.service` | Use-case implementations | `port.in`, `port.out`, `domain` |
| `gym.domain` | Domain model, business rules, `Money`, `Notification`, `NotFoundException`, `RuleViolationException` | The JDK only |
| `gym.adapter.persistence` | `JsonFileRepository` and each feature's JSON repository | `port.out`, `domain` |
| `gym.adapter.payment` | `FakePaymentGateway`, the simulated card provider | `port.out`, `domain` |
| `gym.adapter.notification` | `EventNotificationSender` and `NotificationLog`, delivering notifications over Spring's event bus | `port.out`, `domain` |
| `gym.adapter.logging` | `LoggingInterceptor` and other out-of-band services | `interceptor` |
| `gym.interceptor` | The Interceptor pattern: interceptor, context, dispatcher, proxy | Nothing in `gym`, so it works with any use case |

`ArchitectureTest` checks these rules, so a wrong import fails the build. If it fails, fix the dependency rather than the test.

## Shared building blocks

Use these instead of writing your own:

| Need | Use |
| --- | --- |
| A price, fee, discount or credit | `domain.Money`: always 2 decimal places, rounded half up. `Money.euros("45")`, `plus`, `minus`, `percent(15)`, `proRata(12, 30)`, `min` |
| Take a payment | Inject `port.out.PaymentGateway` and call `charge(memberId, amount)`. It returns `false` when the card is declined. List member ids under `gym.payment.declined-members` in application.properties to demo a decline |
| Tell a member something | Inject `port.out.NotificationSender` and call `send(new Notification(memberId, subject, message))`. For now `NotificationLog` writes it to the log |
| The current time | Inject the `java.time.Clock` bean |
| A repository in a unit test | Extend `gym.support.InMemoryRepository` (test sources): no files, no Spring |
| Members and memberships | `domain.Member`, `domain.Membership` (with `PlanType` and `MembershipStatus`) and their `MemberRepository` and `MembershipRepository` ports, stored in members.json and memberships.json. They only hold data so far: Member and Membership add their behaviour, State and Factory on top |
| Charge a fee or penalty | Inject `port.in.RecordChargeUseCase` and call `record(membershipId, ChargeType.NO_SHOW, Money.euros(5))`. It goes on the next invoice. `creditReferral(membershipId)` gives the referrer €20 and applies the yearly cap |
| Bill a sign-up or a cancellation | Inject `port.in.CalculateChargesUseCase`: `billFirstMonth(membershipId, joined)` in UC1, `billFinal(membershipId)` in UC6 and UC13. Both take the payment and return the `Invoice`, whose `status` says whether it was paid |
| Pay arrears | Inject `port.in.PayArrearsUseCase` and call `payArrears(membershipId)` in UC14. An empty list means the card was declined, so the membership stays Lapsed |

## Adding your feature

Taking Booking as the example:

1. `domain`: `GymClass`, `Booking`, `Waitlist` and their rules. Throw `RuleViolationException` when a rule is broken and `NotFoundException` for an unknown id. The `ErrorHandler` turns them into 422 and 404 responses.
2. `port.out`: `interface GymClassRepository extends Repository<GymClass, String>`.
3. `adapter.persistence`: `JsonFileGymClassRepository extends JsonFileRepository<GymClass, String> implements GymClassRepository`. The Javadoc on `JsonFileRepository` shows the constructor.
4. `port.in`: `BookClassUseCase`, taking ids and values rather than DTOs.
5. `service`: `BookingService implements BookClassUseCase`. Inject the `java.time.Clock` bean instead of calling `now()`, so tests can fix the date.
6. `dto` and `controller`: `BookingRequest`, `BookingResponse` and `BookingController`, which calls `BookClassUseCase`.
7. Tests: unit-test the rules in `domain` and `service` with an in-memory fake of the repository port (extend `gym.support.InMemoryRepository`), plus a test for your design pattern.

## Adding an out-of-band service

Every call to a `port.in` use case already passes through the Interceptor pattern ([report/architecture](../report/architecture/README.md#pattern-3-interceptor-covered-in-lectures-week-7)), so logging, timing or auditing never goes into a controller or service:

1. Write a class in `adapter/` that implements `UseCaseInterceptor` and overrides `onBefore`, `onAfter` or `onFailure`. `UseCaseContext` gives you the use case, arguments, result, failure and elapsed time.
2. Register it in `InterceptorConfiguration` with a priority. Lower runs first on the way in and last on the way out.
