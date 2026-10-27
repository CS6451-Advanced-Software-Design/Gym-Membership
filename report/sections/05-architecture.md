# Architecture {#architecture}

<!--
Owner: Chirag. Maximum 2 pages.
MVC, Hexagonal (self-researched, Cockburn 2005) and Interceptor (lectures, logging as the out-of-band service), why a monolith, the alternatives we rejected, the package diagram, and the hosting stack: web server, application server, Enterprise Information System, message bus.
Evidence: report/architecture/README.md, report/tech-stack.md, ArchitectureTest.
-->

## Overview

We built the system as a monolith mainly because the professor asked for one in Part 1, but it had real advantages for us too. Keeping everything in one application made development much simpler, which is what we wanted for this first iteration of the project, and it made testing and debugging easier. Deployment was simple as well: one Dockerfile that we can deploy to any service, in our case Railway. Our goal is to convert the monolith into microservices later, using what we learn in future lectures.

The system uses three architectural patterns: MVC, Hexagonal and Interceptor. MVC handles requests in and out, Hexagonal shapes the whole app, and Interceptor wraps every use case.

The code is organised by layer rather than by feature, which keeps it a monolith rather than microservices.

![Package diagram](../architecture/package-diagram.png)

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

When deciding on the architecture, we wanted to cover as many of the patterns discussed in our lectures as possible. We were already constrained to a monolith by the specification, so MVC made the most sense. For our self-researched pattern, we discussed options with GenAI, and after some comparisons Hexagonal seemed to fit best.

We could have used Service Layer + Data Mapper, which is the default for most Spring Boot apps, but that wouldn't have involved much research of our own.

## Liabilities

- **Complexity:** Hexagonal uses technical terms such as `port.in` and `port.out`, making the stack look more complex than it might be.
- **Shortcut:** we store the domain objects directly as JSON files, without a separate persistence document in between, for example a `MembershipDocument` with `from(Membership)` and `toDomain()`, where old-data defaults would be handled.

## Hosting Stack

Tomcat embedded in the Spring Boot jar is both the web server and the application server; JSON files behind `adapter.persistence` are the Enterprise Information System; and Spring's `ApplicationEventPublisher` is the in-process message bus.
