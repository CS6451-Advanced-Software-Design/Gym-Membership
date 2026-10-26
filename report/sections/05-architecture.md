# Architecture {#architecture}

<!--
Owner: Chirag. Maximum 2 pages.
MVC, Hexagonal (self-researched, Cockburn 2005) and Interceptor (lectures, logging as the out-of-band service), why a monolith, the alternatives we rejected, the package diagram, and the hosting stack: web server, application server, Enterprise Information System, message bus.
Evidence: report/architecture/README.md, report/tech-stack.md, ArchitectureTest.
-->

## Overview

We built the system as a monolith mainly because the professor asked for one in Part 1, but it had real advantages for us too. Keeping everything in one application made development much simpler, which is what we wanted for this first iteration of the project, and it made testing and debugging easier. Deployment was simple as well: one Dockerfile that we can deploy to any service, in our case Railway. Our goal is to convert the monolith into microservices later, using what we learn in future lectures.

The system uses three architectural patterns: MVC, Hexagonal and Interceptor. MVC handles requests in and out, Hexagonal shapes the whole app, and Interceptor wraps every use case.

The code is organised by layer rather than by feature, which keeps it a monolith rather than microservices:

```text
gym/                 classes
├── controller/         5   MVC controller, driving adapters (Postman and the clock)
├── dto/                9   MVC view: the JSON records
├── port/in/           11   inbound ports: one interface per use case
├── port/out/           7   outbound ports: repositories, payments, notifications
├── service/            2   use cases, implementing port.in
├── domain/            31   MVC model: the core, plain Java only
├── adapter/           12   driven adapters: JSON files, payments, events, logging
├── interceptor/        4   the Interceptor framework
└── (root)              4   GymApplication and the Spring configuration
```

![Package diagram](../architecture/package-diagram.png)

## Model View Controller

MVC was our choice for its three-layered style, which keeps the code simple and organised. In our case the view is the JSON our controllers return (the records in `dto`), which we look at through Postman and Swagger UI rather than through screens of our own.

| Role | Package | Example |
| --- | --- | --- |
| Model | `domain`, `service` | `Membership`, `BillingService` |
| View | `dto`, rendered as JSON | `InvoiceResponse` |
| Controller | `controller` | `BillingController`, `ErrorHandler` |

## Hexagonal (Ports and Adapters)

**What it is.** Hexagonal architecture was proposed by Alistair Cockburn (2005). The idea is that the core logic lives in a single structure in the middle, which doesn't know who calls it or where its data is stored. Ports (interfaces) and adapters (the classes that implement them) plug into that middle structure as plug-and-play components. The hexagon shape itself has no real meaning; it just shows the core logic in the middle with many ports around it.

There are two kinds of ports. Inbound ports (`port.in`) are the use cases the outside world can call, such as running the monthly billing. Outbound ports (`port.out`) are what the core needs from the outside world, such as storing data or taking a payment, without knowing how it is done.

**Why we chose it.**

- **Less coupling:** MVC's three layers alone were too abstract for the actual implementation, and could have led to a tightly coupled solution.
- **Room to evolve:** it gave us a clear direction for how the system can evolve. The data is currently stored in JSON files, but it could move to a proper PostgreSQL database in future without changing any business logic.
- **Testability:** since the core logic in the middle, such as `BillingService`, is so decoupled, we could test it easily without involving outside services like the JSON files or payments.

**How it fits with MVC.** The two architectures turned out not to compete, and fit very well together:

- MVC describes how requests come in and go out: the controller receives them, and the view shows the model.
- Hexagonal describes how the whole application can be built for extensibility and very little coupling.

| Role | Package | Example |
| --- | --- | --- |
| Application (the hexagon) | `domain`, `service` | `BillingService`, `Membership` |
| Inbound port | `port.in` | `RunMonthlyBillingUseCase`, `SignUpUseCase` |
| Driving adapter | `controller` | `BillingController` (Postman), `BillingJob` and `MembershipChecksJob` (the clock) |
| Outbound port | `port.out` | `Repository<T, ID>`, `PaymentGateway`, `NotificationSender` |
| Driven adapter | `adapter.*` | `JsonFileInvoiceRepository`, `FakePaymentGateway`, `EventNotificationSender` |

**Enforced by a test.** *To be written by Chirag.* <!-- The dependency rule isn't just a convention: ArchitectureTest (ArchUnit) checks it on every build, and CI blocks the merge if it fails (snippet in §8). `domain` may only use the JDK, so no Spring, Jackson or file I/O can leak into the business rules. -->

## Interceptor

<!--
About a quarter page:
- Cite POSA2 (Schmidt et al. 2000) and lectures I and J. It came from the lectures, so it sits on top of Hexagonal and doesn't replace it as the self-researched pattern.
- Intent in your words: add services to a framework without changing the framework or the code that uses it.
- The lecture asked to prove it works by implementing logging as an out-of-band service: LoggingInterceptor logs every use-case call, its outcome and duration, and no controller or service has a log statement. On Railway the logs are JSON, so you can filter them by use case or outcome.
- One design choice worth naming: the context object is read-only, so a faulty interceptor can't change arguments or results (safety over openness, as the lecture warns).
-->

*To be written by Chirag.*

| POSA2 participant | Class | Package |
| --- | --- | --- |
| Interceptor | `UseCaseInterceptor` (`onBefore`, `onAfter`, `onFailure`) | `interceptor` |
| Context object | `UseCaseContext` | `interceptor` |
| Dispatcher | `UseCaseDispatcher` | `interceptor` |
| Callback mechanism | `UseCaseInterception` (a dynamic proxy around every `port.in` bean) | `interceptor` |
| Concrete interceptor | `LoggingInterceptor` (logging, the out-of-band service) | `adapter.logging` |
| Application | `InterceptorConfiguration` | `gym` |

<!--
After the table, 1 sentence: adding a new cross-cutting service (audit, timing, rate limiting) is one class plus one register line in InterceptorConfiguration (snippet in §8), with no change to the hexagon. That's a second extensibility tactic for QA1.
-->

*To be written by Chirag.*

## Alternatives Considered

<!--
1–2 sentences before the table: when you chose (7 Oct 2026, with the team) and what mattered most (extensibility, and showing independent research).
-->

*To be written by Chirag.*

| Alternative | Why not |
| --- | --- |
| Microservices | Part 1 must be a monolith |
| Microkernel (Plug-in) | Overlaps with Strategy at our scale |
| Pipes and Filters | Fits only the billing run |
| Service Layer + Data Mapper | A common default, so less independent research |

## Liabilities

<!--
2–4 honest sentences; markers reward admitting the costs:
- More interfaces and indirection than plain layers (a port per use case).
- A mapping step between DTOs, domain objects and the JSON documents.
- Every use-case call goes through an extra proxy, and interceptor order matters (hence the priority on register).
- It's easy to let Spring annotations leak into the core; ArchitectureTest is what stops that.
-->

*To be written by Chirag.*

## Hosting Stack

<!--
1 sentence before the table: everything runs in one process, and the Enterprise Information System sits behind an outbound port, so a real database in Part 2 would only need a new adapter.
-->

*To be written by Chirag.*

| Tier | Technology |
| --- | --- |
| Web server | Tomcat, embedded in the Spring Boot jar |
| Application server | The same embedded Tomcat, running `domain` and `service` |
| Enterprise Information System | JSON files, one per entity, reached only through `adapter.persistence` |
| Message bus | Spring `ApplicationEventPublisher` (in-process), used by `adapter.notification` |
| Hosting | Railway, deployed from `main`, data on a volume at `/data` |
