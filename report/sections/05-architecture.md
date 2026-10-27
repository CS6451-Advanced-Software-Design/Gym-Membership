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

![Package diagram](../architecture/package-diagram.png){width=75%}

## Model View Controller

MVC was our choice for its three-layered style, which keeps the code simple and organised. In our case the view is the JSON our controllers return (the records in `dto`), which we look at through Postman and Swagger UI rather than through screens of our own.

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

**Enforced by a test.** A test called `ArchitectureTest`, written with ArchUnit (a Java library for checking architecture rules), enforces this architecture not just on paper but with actual tests (snippet in §8).

## Interceptor

The Interceptor pattern was taught in lectures after we had already started work on the project, so at first we didn't have a clear idea of how to implement it. The lecture suggested logging as an out-of-band service, and after some back-and-forth discussion with GenAI we agreed it was a good place for it.

The pattern wraps every use case in our codebase, so logic can run at fixed points in a consistent and very extensible way. Logging fit this perfectly, and it was what we lacked at that moment. It is "out-of-band": the main business logic doesn't need it, but without the pattern it would have been spread out and unorganised across every service.

It proved very useful after we deployed to Railway and saw that Railway supports JSON logs, which our first deployment didn't produce. By changing only the logging interceptor and one setting, we changed the logging across the whole codebase and improved our log monitoring on Railway.

| POSA2 participant | Class | Package |
| --- | --- | --- |
| Interceptor | `UseCaseInterceptor` (`onBefore`, `onAfter`, `onFailure`) | `interceptor` |
| Context object | `UseCaseContext` | `interceptor` |
| Dispatcher | `UseCaseDispatcher` | `interceptor` |
| Concrete interceptor | `LoggingInterceptor` (logging, the out-of-band service) | `adapter.logging` |

With Interceptor, adding another service such as audit, timing or rate limiting is as simple as the logging service implementation.

## Alternatives Considered

<!--
1–2 sentences, no table: when you chose (7 Oct 2026, with the team), what mattered most, and why not the others: Microservices (Part 1 must be a monolith), Microkernel (overlaps with Strategy at our scale), Pipes and Filters (fits only the billing run), Service Layer + Data Mapper (a common default, so less independent research).
-->

*To be written by Chirag.*

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
| Enterprise Information System | JSON files, one per entity, behind `adapter.persistence` |
| Message bus | Spring `ApplicationEventPublisher`, in-process |
