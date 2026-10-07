# Tech Stack

**Status: agreed 30 Sep 2026. Hosting, maintenance and Appwrite notes added 7 Oct 2026.** Feeds report §5 (architecture and hosting stack), §8 (code and tests), §9 (CI/CD and metrics) and §10 (recovered blueprints).

## Choices

| Concern | Choice | Why |
| --- | --- | --- |
| Language | Java 21 (LTS) | Matches the lecture examples, and everyone can read it |
| Framework | Spring Boot 4.1.x (Spring Web starter) | MVC/REST controllers and dependency injection out of the box. Maps directly onto our `controller / port / service / domain / adapter` packages |
| Build | Maven, through the Maven wrapper (`./mvnw`) | Standard, and the CI and metrics plugins below plug straight in. The wrapper means nobody has to install Maven |
| Front end | Postman collection (committed in the repo) | The spec allows a simulated front end, so the effort goes into the business tier |
| Persistence | Appwrite Databases (TablesDB) behind the `Repository<T, ID>` port, with JSON files (Jackson) as the offline adapter | One port, two adapters, picked by Spring profile: this is the Hexagonal pattern in action. No ORM. Tests use in-memory fakes, so they never touch the network |
| Notifications | Appwrite Messaging (email) behind the `NotificationSender` port | Real welcome and waitlist-promotion emails without writing an email client |
| Tests | JUnit 5 + Mockito for business rules, MockMvc for a few controller tests | §8 needs 2+ automated tests. Unit-test every business rule without I/O |
| CI/CD | GitHub Actions: build → test → coverage → package the jar as a build artifact | §9 added value. Set up in Iteration 1, not at the end |
| Metrics | JaCoCo (coverage), PMD (code smells), CK or SonarCloud (coupling, cohesion, complexity) | §9 needs metrics that *lead to* refactoring. Record the before and after numbers for each refactoring |
| UML workbench | Visual Paradigm (UL licence), or StarUML as a fallback | §10 must be drawn in a workbench, not Word or PowerPoint. Visual Paradigm can reverse-engineer the code into package and class diagrams |

## Setting up the project

Generate it once at [start.spring.io](https://start.spring.io) (or the same wizard in IntelliJ / VS Code):

| Field | Value |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.1 (current stable release, checked 7 Oct 2026) |
| Group / Artifact | `gym` / `gym-membership` |
| Java | **21** (Initializr defaults to 17, so change it) |
| Dependencies | Spring Web, Validation, Spring Boot Actuator. Optional: Spring Boot DevTools for auto-restart while coding |

Don't add JPA or a database driver: the data lives in JSON files.

Each member needs a **JDK 21** locally, e.g. `brew install --cask temurin@21` on macOS, or [SDKMAN](https://sdkman.io) (pick a `21.x-tem` build from `sdk list java`). Then `./mvnw spring-boot:run` starts the app on http://localhost:8080 and `./mvnw verify` runs the build and tests.

## Hosting stack (report §5)

A single Spring Boot jar, deployed as one unit. This is the monolith.

| Tier | What we use |
| --- | --- |
| Web server + application server | Tomcat, embedded in the Spring Boot jar. `java -jar gym-membership.jar` starts everything |
| Business tier | `service` and `domain` packages |
| EIS / database | Appwrite Databases on Appwrite Cloud (`appwrite` profile), or JSON files in a `data/` folder (`local` profile). Accessed only through `adapter.persistence` |
| Message bus | Spring's in-process `ApplicationEventPublisher`. It also carries the Observer pattern for waitlist promotion and payment notifications |
| Client | Postman, calling `http://localhost:8080` |
| External services | Appwrite Cloud: TablesDB (EIS) and Messaging (email), over HTTPS |

The §10 deployment diagram follows from this: one node (a laptop or VM) running a JVM, with the jar artifact (Tomcat inside) and the `data/` folder, connected over HTTPS to an Appwrite Cloud node.

Optionally, `./mvnw spring-boot:build-image` packages the app as a Docker image with Cloud Native Buildpacks, with no Dockerfile needed.

## Where to run it

Running locally is enough for the walkthrough. A live deployment is optional, but it strengthens the CD part of §9. With the `appwrite` profile the data lives in Appwrite Cloud, so any host works, even one with an ephemeral disk. With the `local` profile the data is JSON files on disk, so the host must keep files across restarts:

| Option | Cost | Files survive a restart? |
| --- | --- | --- |
| **Local** (`java -jar`) | Free | Yes. **Default for development and the demo** |
| Render free web service | Free | No. Ephemeral filesystem, no disks on the free plan, sleeps after 15 min idle ([Render](https://render.com/docs/free)). Fine with the `appwrite` profile |
| Railway | $5 one-off trial credit, then $1/month | Yes, but trial volumes are deleted 30 days after the credit runs out ([Railway](https://docs.railway.com/reference/pricing/free-trial)) |
| Koyeb, Fly.io | Card needed / no free tier for new users | Koyeb's free tier has no volumes |
| **Azure for Students** ($100, no card) or **DigitalOcean** ($200 via the GitHub Student Developer Pack) | Free with student credits | Yes, on a small VM or App Service. **Best choice if we deploy** |

Free-tier terms change often. These were checked on 7 Oct 2026.

## Appwrite

We use [Appwrite Cloud](https://cloud.appwrite.io) wherever it fits the monolith. The Spring Boot app itself still runs as one jar. Appwrite is an external service behind our outbound ports, like any database server.

| Appwrite product | Use | Adapter (implements port) |
| --- | --- | --- |
| **TablesDB** (Databases) | EIS. One table per entity in the [ER diagram](analysis/README.md): `members`, `plans`, `memberships`, `classes`, `bookings`, `waitlist_entries`, `charges`, `invoices` | `AppwriteMembershipRepository` etc. (`MembershipRepository` etc.) |
| **Messaging** (email) | Welcome email (UC1), waitlist promotion (BR-B2), lapsed-payment warning (BR-M4) | `AppwriteNotificationSender` (`NotificationSender`) |
| **Users** | One Appwrite user per member, used only as the email target for Messaging. Not used for login, since the front end is Postman | Part of `AppwriteNotificationSender` |
| **Storage** (optional) | Upload of a student ID for the Student plan check (BR-M1) | Later, if time allows |
| Functions, Sites | **Not used.** Functions are per-request handlers and would split the monolith (Part 2 territory). Sites hosts JavaScript front ends, not a Java server | |

Setup:

- SDK: `io.appwrite:sdk-for-kotlin` **22.1.0** (latest on 7 Oct 2026). It is written in Kotlin but has a Java API. The Maven Central search index still shows 9.0.0, so take the version from the [GitHub releases](https://github.com/appwrite/sdk-for-kotlin/releases).
- SDK calls are asynchronous (`CoroutineCallback`). Each adapter wraps them in a `CompletableFuture` and waits, so the ports stay simple, synchronous Java.
- Create one Appwrite Cloud project for the team and add all four members to its organisation. Create a server **API key** with only the TablesDB and Messaging scopes we need.
- Configure an email provider in Messaging (e.g. SMTP or Mailgun) before sending.
- Configuration comes from environment variables, never from committed files: `APPWRITE_ENDPOINT`, `APPWRITE_PROJECT_ID`, `APPWRITE_API_KEY`. Add the API key to GitHub Actions secrets only if a CI job needs it. Unit tests must not.
- Spring profiles: `appwrite` uses the Appwrite adapters, `local` (the default) uses JSON files and logs notifications, so anyone can run the app offline.

Chirag works at Appwrite. The report should say so when it justifies this choice.

## Keeping it maintained (report §9)

| Tool | Job |
| --- | --- |
| GitHub Actions + `actions/setup-java` (Temurin 21) | CI on every pull request: `./mvnw verify` (build, tests, JaCoCo coverage) |
| Dependabot (`.github/dependabot.yml`) | Opens pull requests when Maven dependencies or GitHub Actions versions go out of date |
| PMD, SonarCloud (free for public repos) | Code smells, complexity and coupling numbers that drive refactoring |
| [OpenRewrite](https://docs.openrewrite.org/recipes/java/spring/boot4/upgradespringboot_4_0-community-edition) (`rewrite-spring`) | Automated Spring Boot upgrades, run from the command line without changing `pom.xml` |
| Spring Boot Actuator | `/actuator/health` and `/actuator/info` to check the app is up after a deploy |
| CD job (optional) | Build the jar as an Actions artifact, push a Buildpacks image to GitHub Container Registry (GHCR), deploy to Azure or DigitalOcean |

## Package structure

Package by layer with Hexagonal ports and adapters, see [architecture/README.md](architecture/README.md#package-structure). Package by feature is the microservice layout and belongs to Part 2.
