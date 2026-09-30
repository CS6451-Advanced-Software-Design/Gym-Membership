# Tech Stack

**Status: agreed 30 Sep 2026.** Feeds report §5 (architecture and hosting stack), §8 (code and tests), §9 (CI/CD and metrics) and §10 (recovered blueprints).

## Choices

| Concern | Choice | Why |
| --- | --- | --- |
| Language | Java 21 (LTS) | Matches the lecture examples, and everyone can read it |
| Framework | Spring Boot 4.1.x (Spring Web starter) | MVC/REST controllers and dependency injection out of the box. Maps directly onto our `controller / service / domain / repository` layers |
| Build | Maven | Standard, and the CI and metrics plugins below plug straight in |
| Front end | Postman collection (committed in the repo) | The spec allows a simulated front end, so the effort goes into the business tier |
| Persistence | JSON files behind `Repository` interfaces (Jackson), with DTOs | The spec allows file I/O plus DTOs. No ORM, since the UI and ORM earn no added-value marks, and repositories can be swapped for in-memory fakes in tests |
| Tests | JUnit 5 + Mockito for business rules, MockMvc for a few controller tests | §8 needs 2+ automated tests. Unit-test every business rule without I/O |
| CI/CD | GitHub Actions: build → test → coverage → package the jar as a build artifact | §9 added value. Set up in Iteration 1, not at the end |
| Metrics | JaCoCo (coverage), PMD (code smells), CK or SonarCloud (coupling, cohesion, complexity) | §9 needs metrics that *lead to* refactoring. Record the before and after numbers for each refactoring |
| UML workbench | Visual Paradigm (UL licence), or StarUML as a fallback | §10 must be drawn in a workbench, not Word or PowerPoint. Visual Paradigm can reverse-engineer the code into package and class diagrams |

Generate the project at [start.spring.io](https://start.spring.io) (Maven, Java 21, Spring Boot 4.1.x, dependency: Spring Web). Spring Boot 4.1.1 was the current release on 30 Sep 2026.

## Hosting stack (report §5)

A single Spring Boot jar, deployed as one unit. This is the monolith.

| Tier | What we use |
| --- | --- |
| Web server + application server | Tomcat, embedded in the Spring Boot jar |
| Business tier | `service` and `domain` packages |
| EIS / database | JSON file store, accessed only through `repository` |
| Message bus | Spring's in-process `ApplicationEventPublisher`. It also carries the Observer pattern for waitlist promotion and payment notifications |

## Package structure

Package by layer, see [roles.md](roles.md#package-structure-monolithic-package-by-layer). Package by feature is the microservice layout and belongs to Part 2.
