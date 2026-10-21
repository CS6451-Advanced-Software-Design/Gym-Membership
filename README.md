# Gym & Fitness Club Membership System

CS6451 Advanced Software Design, Team-Based Assignment Part 1 (Brightspace Group 1), University of Limerick, Autumn 2026/27.

A monolithic (package-by-layer MVC + Hexagonal, with Interceptor) business tier for a gym and fitness club: membership plans and billing, class bookings with capacity and waitlists, check-in, freezes, penalties, and referral credits. The front end is simulated with Postman and the data layer with file I/O behind repositories and DTOs.

**Deadline: 23:59 Sun 1 Nov 2026.**

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
| [code/](code) | Everything that builds and runs: the Spring Boot project, Postman collection, JSON data |

## Documents

| Doc | Purpose |
| --- | --- |
| [report/requirements](report/requirements/README.md) | Actors, use cases, business rules, quality attributes (report §4) |
| [report/architecture](report/architecture/README.md) | MVC + Hexagonal + Interceptor, package diagram, hosting stack (report §5) |
| [report/analysis](report/analysis/README.md) | Candidate objects, class, sequence, state chart and ER diagrams (report §6) |
| [report/tech-stack.md](report/tech-stack.md) | Java 21 + Spring Boot, Maven, tests, CI/CD, metrics, UML workbench, hosting stack (report §5, §8, §9, §10) |
| [report/sample-project-lessons.md](report/sample-project-lessons.md) | What past submissions did well and where they lost marks |
| [report/report-plan.md](report/report-plan.md) | Owner, evidence and quality bar for every report section (§1–14) |
| [report/roles.md](report/roles.md) | Roles and package ownership (report §3) |
| [report/diary.md](report/diary.md) | Weekly diary, Weeks 4–12 (report §7). **Update every week** |
| [report/genai-prompts.md](report/genai-prompts.md) | Every GenAI prompt used (report §13). **Log prompts as you go** |

## Diagrams

The current diagrams, in report order. Each one is explained in the doc it links to.

### Use cases (report §4)

What each actor can do in the system. Details in [report/requirements](report/requirements/README.md).

![Use case diagram](report/requirements/use-case-diagram.png)

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

![ER diagram](report/analysis/er-diagram.png)

## Working agreement

- Work on a branch per task and open a pull request. Another member reviews before merge.
- Conventional commit messages, e.g. `feat(booking): add waitlist promotion`.
- Commit under your own GitHub account, **every week**. The commit history is marked evidence of equal contribution (report §7 and §8) and has to agree with the diary.
- Only claim in the report what the code shows. Every pattern, test and pipeline mentioned must be findable in the repo.
- Log any GenAI prompt in `report/genai-prompts.md` in the same PR that uses its output.
- Report prose must be in our own words. GenAI may bootstrap diagrams and code only.
