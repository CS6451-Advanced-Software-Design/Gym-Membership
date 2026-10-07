# Tech Stack

**Status: agreed 30 Sep 2026, hosting and maintenance notes added 7 Oct 2026.** Feeds report §5 (architecture and hosting stack), §8 (code and tests), §9 (CI/CD and metrics) and §10 (recovered blueprints).

## Choices

| Concern | Choice | Why |
| --- | --- | --- |
| Language | Java 21 (LTS) | Matches the lecture examples, and everyone can read it |
| Framework | Spring Boot 4.1.x (Spring Web starter) | MVC/REST controllers and dependency injection out of the box. Maps directly onto our `controller / port / service / domain / adapter` packages |
| Build | Maven, through the Maven wrapper (`./mvnw`) | Standard, and the CI and metrics plugins below plug straight in. The wrapper means nobody has to install Maven |
| Front end | Postman collection (committed in the repo) | The spec allows a simulated front end, so the effort goes into the business tier |
| Persistence | JSON files behind the `Repository<T, ID>` port (Jackson), with DTOs | The spec allows file I/O plus DTOs. No ORM, since the UI and ORM earn no added-value marks, and the port can be swapped for in-memory fakes in tests |
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

```
                 Postman (simulated front end)
                              |
                              |  HTTP + JSON, localhost:8080
                              v
+--------------------------------------------------------------+
| JVM: Java 21 (Temurin)                                       |
|  +--------------------------------------------------------+  |
|  | gym-membership.jar (Spring Boot 4.1, one deployable)   |  |
|  |                                                        |  |
|  |   Embedded Tomcat           web + application server   |  |
|  |          |                                             |  |
|  |   controller, dto           MVC controller and view    |  |
|  |          |                                             |  |
|  |   port.in                                              |  |
|  |          |                                             |  |
|  |   service, domain           business tier (hexagon)    |  |
|  |          |                                             |  |
|  |   port.out                                             |  |
|  |          |                                             |  |
|  |   adapter                   JSON files, fake payments, |  |
|  |          |                  ApplicationEventPublisher  |  |
|  |          |                  (message bus)              |  |
|  +----------|---------------------------------------------+  |
+-------------|------------------------------------------------+
              |  Jackson, file I/O
              v
     data/*.json                    EIS (one file per entity)


Build and CI (GitHub Actions on every pull request):

  ./mvnw verify --> compile --> JUnit 5 + Mockito --> JaCoCo + PMD --> gym-membership.jar
                                                                         (build artifact)
```

| Tier | What we use |
| --- | --- |
| Web server + application server | Tomcat, embedded in the Spring Boot jar. `java -jar gym-membership.jar` starts everything |
| Business tier | `service` and `domain` packages |
| EIS / database | JSON files in a `data/` folder, accessed only through `adapter.persistence` |
| Message bus | Spring's in-process `ApplicationEventPublisher`. It also carries the Observer pattern for waitlist promotion and payment notifications |
| Client | Postman, calling `http://localhost:8080` |

The §10 deployment diagram follows from this: one node (a laptop or VM) running a JVM, with the jar artifact (Tomcat inside) and the `data/` folder.

Optionally, `./mvnw spring-boot:build-image` packages the app as a Docker image with Cloud Native Buildpacks, with no Dockerfile needed.

## Where to run it

Running locally is enough for the walkthrough. A live deployment is optional, but it strengthens the CD part of §9. Because the data is JSON files on disk, a host must keep files across restarts:

| Option | Cost | Files survive a restart? |
| --- | --- | --- |
| **Local** (`java -jar`) | Free | Yes. **Default for development and the demo** |
| Render free web service | Free | No. Ephemeral filesystem, no disks on the free plan, sleeps after 15 min idle ([Render](https://render.com/docs/free)) |
| Railway | $5 one-off trial credit, then $1/month | Yes, but trial volumes are deleted 30 days after the credit runs out ([Railway](https://docs.railway.com/reference/pricing/free-trial)) |
| Koyeb, Fly.io | Card needed / no free tier for new users | Koyeb's free tier has no volumes |
| **Azure for Students** ($100, no card) or **DigitalOcean** ($200 via the GitHub Student Developer Pack) | Free with student credits | Yes, on a small VM or App Service. **Best choice if we deploy** |

Free-tier terms change often. These were checked on 7 Oct 2026.

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
