# Gym & Fitness Club Membership System

CS6451 Advanced Software Design, Team-Based Assignment Part 1 (Brightspace Group 1), University of Limerick, Autumn 2026/27.

A monolithic (package-by-layer MVC + Hexagonal, with Interceptor) business tier for a gym and fitness club: membership plans and billing, class bookings with capacity and waitlists, check-in, freezes, penalties, and referral credits. The front end is simulated with Postman and the data layer with file I/O behind repositories and DTOs.

**Deadline: 23:59 Sun 1 Nov 2026.**

**Try it:** the public [Postman workspace](https://www.postman.com/spaceflight-geoscientist-47956182/workspace/gym-membership) has every endpoint, documented from the code, ready to send. Pick **production** to call the live app on [Railway](https://gym-membership-production-a725.up.railway.app/swagger-ui.html), **mock** for Postman's saved examples, or **local** against the app on your machine. See [code/README.md](code/README.md#postman).

![The monthly billing run in Postman: its docs, generated from the Javadoc, above the live response](report/screenshots/postman.png)

## Team

| Member | GitHub | Roles |
| --- | --- | --- |
| Chirag Aggarwal | @ChiragAgg5k | Architect, Designer. Owns Billing |
| Bharat Doodi | @AKABharat | Project Manager, Tester. Owns Booking |
| DineshReddy Mogili | @sanjuD1603 | Documentation Manager, Technical Lead + DevOps. Owns Member |
| Dhruv Punj | @DHRUVx22 | Business Analyst, Systems Analyst. Owns Membership |

See [report/roles.md](report/roles.md) for the full role split.

## Repository layout

| Folder | What lives there |
| --- | --- |
| [report/](report) | Everything for the report: write-ups, diagrams, diary, GenAI log, plans |
| [code/](code) | Everything that builds and runs: the Spring Boot project, the OpenAPI spec and Postman collections, JSON data |

## Documents

| Doc | Purpose |
| --- | --- |
| [report/requirements](report/requirements/README.md) | Actors, use cases, business rules, quality attributes (report §4) |
| [report/architecture](report/architecture/README.md) | MVC + Hexagonal + Interceptor, package diagram, hosting stack (report §5) |
| [report/analysis](report/analysis/README.md) | Candidate objects, class, sequence, state chart and ER diagrams (report §6) |
| [report/sketches](report/sketches/README.md) | Photos of our rough work and hand-drawn sketches (report §6). **Add photos as you draw** |
| [report/tech-stack.md](report/tech-stack.md) | Java 21 + Spring Boot, Maven, tests, CI/CD, metrics, UML workbench, hosting stack (report §5, §8, §9, §10) |
| [report/sample-project-lessons.md](report/sample-project-lessons.md) | What past submissions did well and where they lost marks |
| [report/report-plan.md](report/report-plan.md) | Owner, evidence and quality bar for every report section (§1–14) |
| [report/roles.md](report/roles.md) | Roles and package ownership (report §3) |
| [report/diary.md](report/diary.md) | Weekly diary, Weeks 4–12 (report §7). **Update every week** |
| [report/genai-prompts.md](report/genai-prompts.md) | Every GenAI prompt used (report §13). **Log prompts as you go** |

## Patterns

Only patterns the code really has are listed as in place, each with where to find it. Details in [report/architecture](report/architecture/README.md).

**In place**

| Pattern | Kind | Where |
| --- | --- | --- |
| Model View Controller | Architectural (lectures) | `controller` (controllers, `ErrorHandler`), `dto` (the JSON view), `domain` + `service` (the model) |
| Hexagonal (Ports and Adapters) | Architectural (self-researched) | `port.in`, `port.out`, `adapter.*`. `ArchitectureTest` fails the build if a dependency points outwards |
| Interceptor | Architectural (lectures, POSA2) | `interceptor` (`UseCaseInterceptor`, `UseCaseContext`, `UseCaseDispatcher`, `UseCaseInterception`), with `adapter.logging.LoggingInterceptor` as the out-of-band service, registered in `InterceptorConfiguration` |
| Repository | Design | `port.out.Repository<T, ID>`, implemented by `adapter.persistence.JsonFileRepository` |
| Proxy (dynamic) | Design (GoF) | `UseCaseInterception` wraps every `port.in` use case so each call reaches the interceptors |
| Dependency Injection | Design | Spring constructor injection throughout. The `Clock` bean in `GymApplication` lets tests fix the date |
| Strategy | Design (GoF) | `domain.DiscountPolicy` with `CorporateDiscount`, `FamilyDiscount` and `LoyaltyDiscount`. `BestDiscount` applies only the largest (BR-P2), chosen in `BillingConfiguration`. Proved by `DiscountPolicyTest.theBestDiscountWinsAndDiscountsDontStack` |

**Planned** (from [report/roles.md](report/roles.md), each with a test that exercises the pattern)

| Pattern | Kind | Where | Owner |
| --- | --- | --- | --- |
| State | Design (GoF) | `Membership` lifecycle: Trial, Active, Frozen, Lapsed, Cancelled | Dhruv |
| Factory | Design (GoF) | Plan creation (`MonthlyPlan`, `AnnualPlan`, `StudentPlan`) | Dhruv |
| Command | Design (GoF) | Book and cancel a class, with undo | Bharat |
| Observer | Design (GoF) | Waitlist promotion in Booking, and member notifications | Bharat, DineshReddy |

## Diagrams

The current diagrams, in report order. Each one is explained in the doc it links to.

### Use cases (report §4)

What each actor can do in the system. Details in [report/requirements](report/requirements/README.md).

<p align="center"><img src="report/requirements/use-case-diagram.png" alt="Use case diagram" width="480"></p>

### Packages (report §5)

The MVC + Hexagonal layers. Every dependency points towards `domain`. Details in [report/architecture](report/architecture/README.md).

![Package diagram](report/architecture/package-diagram.png)

### Classes (report §6)

The domain classes, with pre- and postconditions on the interfaces. Details in [report/analysis](report/analysis/README.md).

![Class diagram](report/analysis/class-diagram.png)

### Booking a class (report §6)

The UC2 sequence: the member gets a place, or joins the waitlist when the class is full.

![UC2 sequence diagram](report/analysis/sequence-book-class.png)

### Membership lifecycle (report §6)

The states a `Membership` moves through, which the State pattern implements.

![Membership state chart](report/analysis/membership-statechart.png)

### Data (report §6)

The entities stored in the JSON files, with cardinality.

<p align="center"><img src="report/analysis/er-diagram.png" alt="ER diagram" width="680"></p>

## Working agreement

- Work on a branch per task and open a pull request. Another member reviews before merge.
- Conventional commit messages, e.g. `feat(booking): add waitlist promotion`.
- Commit under your own GitHub account, **every week**. The commit history is marked evidence of equal contribution (report §7 and §8) and has to agree with the diary.
- Only claim in the report what the code shows. Every pattern, test and pipeline mentioned must be findable in the repo.
- Log any GenAI prompt in `report/genai-prompts.md` in the same PR that uses its output.
- Report prose must be in our own words. GenAI may bootstrap diagrams and code only.
