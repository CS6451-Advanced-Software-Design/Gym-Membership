# Architecture (report §5)

> **Bootstrap notes (GenAI, prompts #6–7 in [genai-prompts.md](../genai-prompts.md)).** These are facts and decisions for the Architect, not report text. The report section (max 2 pages) must be written in our own words.

**Status:** pattern chosen 7 Oct 2026, Interceptor added 21 Oct 2026. MVC + **Hexagonal (Ports and Adapters)** + **Interceptor**, monolithic, one Spring Boot jar.

## Diagrams

| Diagram | Purpose |
| --- | --- |
| ![Package diagram](package-diagram.png) | The required high-level package diagram: every package in the skeleton, with `port.in` and `port.out` split, the four driven adapters, and the Interceptor wrapping `port.in`. Every dependency points towards `domain` |

## Pattern 1: MVC (covered in lectures)

| MVC role | Our package | What lives there |
| --- | --- | --- |
| Model | `domain` (+ `service`) | Entities, business rules, the State/Strategy/Observer/Factory classes |
| View | `dto` | Request and response records, rendered as JSON for Postman. No HTML |
| Controller | `controller` | REST endpoints. Translate HTTP to use-case calls. No business rules |

This is MVC in its REST form: the "view" is the JSON representation, not a screen.

## Pattern 2: Hexagonal / Ports and Adapters (self-researched)

Source: Cockburn, A. (2005) *The Hexagonal (Ports & Adapters) Architecture*, HaT Technical Report 2005.02. Available at: https://alistair.cockburn.us/hexagonal-architecture

**Intent (Cockburn):** let the application be driven equally by users, programs, automated tests or batch scripts, and be developed and tested in isolation from its eventual run-time devices and databases.

| Hexagonal element | Our classes | Package |
| --- | --- | --- |
| Application (inside the hexagon) | Domain model + use-case services | `domain`, `service` |
| Driving (inbound) port | `SignUpUseCase`, `BookClassUseCase`, `RunBillingUseCase`, ... | `port.in` |
| Driving (inbound) adapter | REST controllers (Postman), `BillingJob` (clock), JUnit tests | `controller` |
| Driven (outbound) port | `Repository<T, ID>` and its bindings, `PaymentGateway`, `NotificationSender` | `port.out` |
| Driven (outbound) adapter | `JsonFile*Repository`, `FakePaymentGateway`, `EventNotificationSender` | `adapter.*` |

**Dependency rule:** `domain` depends on nothing. `service` depends on `domain` and the ports. Adapters depend inwards on the ports. Nothing in `domain` or `service` imports Spring web, Jackson or `java.io`. An ArchUnit test checks this in CI, so a violation fails the build. Every Hexagonal element in the table above must exist in the code: the report must never claim a pattern the code doesn't have.

**How the two patterns fit together:** MVC organises the *driving* side (how a request reaches the application and how the answer is shown). Hexagonal organises the *whole* application around the domain, so MVC's controller turns into one adapter among several. JUnit tests and the billing clock drive the same ports as Postman does.

**Why Hexagonal for this scenario:**
- QA1 extensibility: a new plan or discount sits in `domain`. A new payment provider or a real database (Part 2) is a new adapter, with no changes to the core.
- QA2 testability (if chosen): every business rule runs with in-memory fakes behind the outbound ports. No files, no HTTP, and a fixed `Clock`.
- The spec allows a simulated front end and file I/O. Hexagonal makes "simulated" an adapter swap rather than a hack.
- It sets up Part 2 (microservices), where each service keeps the same hexagon around its own domain.

**Liabilities to discuss:** more interfaces and indirection than plain layering. A mapping step between DTOs, domain objects and JSON documents. It's easy to let Spring annotations leak into `domain`.

**Alternatives considered (7 Oct 2026):**
- Microkernel / Plug-in: strongest on extensibility, but overlaps with Strategy at our scale.
- Pipes and Filters: fits the billing run (BR-P1) but covers only one feature.
- Service Layer + Data Mapper: a well-known enterprise default, so it shows less independent research.

## Pattern 3: Interceptor (covered in lectures, Week 7)

Source: Schmidt, D.C., Stal, M., Rohnert, H. and Buschmann, F. (2000) *Pattern-Oriented Software Architecture, Volume 2: Patterns for Concurrent and Networked Objects*. Wiley (POSA2). Lectures I and J.

Interceptor was presented in lectures, so it **doesn't replace Hexagonal** as the self-researched pattern (spec §5). It sits on top of it: the lecture asks projects to "prove that your interceptor works by implementing logging as an out-of-band service", which is what `LoggingInterceptor` does.

**Intent (POSA2):** let services be added to a framework transparently, and triggered automatically when certain events occur, without restructuring the framework or touching the code that uses it.

| POSA2 participant | Our class | Package |
| --- | --- | --- |
| Concrete framework | The use-case call path: controller → `port.in` → service | `controller`, `port.in`, `service` |
| Interceptor | `UseCaseInterceptor` (`onBefore`, `onAfter`, `onFailure`) | `interceptor` |
| Context object | `UseCaseContext`: reads the use case, operation, arguments, result, failure, elapsed time. Writes attributes only | `interceptor` |
| Dispatcher | `UseCaseDispatcher`: register and remove with a priority, first in is last out | `interceptor` |
| Callback mechanism | `UseCaseInterception`: wraps every `port.in` bean in a dynamic proxy that calls the dispatcher | `interceptor` |
| Concrete interceptor | `LoggingInterceptor` (out-of-band logging) | `adapter.logging` |
| Application | `InterceptorConfiguration`: creates the concrete interceptors and registers them | `gym` |

**The seven steps from the lecture, applied:**

1. **Model the framework's behaviour.** One use-case call: Idle → Running → Idle, ending in a result or a failure.
2. **Identify interception points.** One interception group (use case) with three points. `onBefore` is a reader and writer: it can record attributes, or veto the call by throwing. `onAfter` and `onFailure` are readers.

```
            call                       returns
   Idle ------------> [onBefore] ---> Running ----------> [onAfter] ----> Idle
                          |              |
                    throws (veto)        |  throws
                          v              v
                        caller      [onFailure] --> exception rethrown to caller
```

3. **Context objects.** One `UseCaseContext` per call (per event, not per registration), because each call has its own arguments and result. It can't change the arguments or the result, so a faulty interceptor can't corrupt business data. That trades some openness for safety, as the lecture warns.
4. **Interceptors.** One interface for the one group, with a hook method per interception point. Default no-op methods, so a concrete interceptor overrides only what it needs.
5. **Dispatchers.** `register(interceptor, priority)` and `remove(interceptor)`. The dispatcher implements `UseCaseInterceptor` itself, so the framework talks to one interceptor.
6. **Callback mechanism.** A Spring `BeanPostProcessor` wraps each `port.in` bean in a JDK dynamic proxy (the "dispatcher as dynamic proxy" note in lecture J). The dispatcher iterates over a snapshot, so registering mid-call is safe.
7. **Concrete interceptors.** `LoggingInterceptor` logs `call`, `done ... in N ms` and `failed ... : reason`, with no log statement in any controller or service.

**Why it fits:** it is a second extensibility tactic (QA1). New cross-cutting services, such as audit, timing or a per-member rate limit, are a new class plus one registration line, with no change to the hexagon.

**Liabilities:** an extra level of indirection on every call. Interceptor order matters, which is why registration takes a priority. A throwing `onBefore` blocks every use case, and an interceptor that calls a use case would be intercepted itself and could recurse. An ArchUnit rule keeps the framework from importing any business package.

## Hosting stack

From [tech-stack.md](../tech-stack.md#hosting-stack-report-5):

| Tier | Technology |
| --- | --- |
| Web server + application server | Tomcat, embedded in the Spring Boot jar |
| Business tier | `domain` + `service` (the hexagon) |
| EIS / database | JSON files, reached only through `adapter.persistence` |
| Message bus | Spring `ApplicationEventPublisher` (in-process), used by `adapter.notification` |

## Package structure

```
gym/
+- GymApplication
+- controller/              inbound adapters: MembershipController, BookingController, BillingController, MemberController
+- dto/                     request and response records (MVC view)
+- port/in/                 use-case interfaces: SignUpUseCase, BookClassUseCase, ...
+- port/out/                Repository<T, ID>, MembershipRepository, GymClassRepository, ..., PaymentGateway, NotificationSender
+- service/                 implements port.in: MembershipService, BookingService, BillingService, MemberService
+- domain/                  Member, Membership, MembershipState (+ states), Plan (+ plans), GymClass, Waitlist, Booking, Charge, Invoice, DiscountPolicy (+ policies)
+- adapter/persistence/     JsonFileMembershipRepository, ...
+- adapter/payment/         FakePaymentGateway
+- adapter/notification/    EventNotificationSender
+- adapter/logging/         LoggingInterceptor (concrete interceptor)
+- interceptor/             UseCaseInterceptor, UseCaseContext, UseCaseDispatcher, UseCaseInterception
+- InterceptorConfiguration registers the concrete interceptors
```

This is still package by layer, so it stays monolithic. Each member owns one feature's classes across every package (see [roles.md](../roles.md)).
