# Recovered Blueprints {#recovered-blueprints}

<!--
Owner: Chirag.
Recovered from the code in Visual Paradigm (not hand-written PlantUML), with the choice of workbench justified. The
design-time package diagram compared with the one in the Architecture section, with the differences explained.
Component and deployment diagrams. A note under each saying what it shows.
Evidence: report/blueprints/ (the Visual Paradigm project, the exported diagrams, and how they were built).
-->

## Workbench

<!--
Why Visual Paradigm (your words). Points you could cover:
- The spec asks for a UML workbench from the community editions and rules out Word and PowerPoint.
- Visual Paradigm Community Edition is free for non-commercial use and draws all three diagram types.
- It reverse engineers Java into a model (Instant Reverse), so the diagrams come from the code, not from memory.
- The catch: Instant Reverse is not in Community Edition. You reversed the code under the 30-day Enterprise
  evaluation (18.1, build 20261012) and exported from Community Edition, because evaluation exports are covered
  in a watermark.
- Anything you'd compare it with (StarUML, Modelio, Papyrus) only if you looked at them.
-->

| Step | How | What came out |
| --- | --- | --- |
| Reverse the source | Instant Reverse on `code/src/main/java` | 66 of the 85 types. All 19 Java `record` types (`Membership`, `Member`, `Money`, `Invoice`, `Charge` and every DTO) were silently dropped: the Java parser predates records |
| Reverse the bytecode | Instant Reverse on `code/target/classes` | All 85 types, 14 packages, 24 realizations, 17 generalizations, 84 associations |
| Package dependencies | The `import` lines in the code, one edge per package pair | 19 edges. Instant Reverse records realization, generalization and association, but not imports |
| Draw | A Visual Paradigm Open API plugin, run headless, placed the reversed packages and port interfaces (GenAI #57) | The three diagrams below, in `report/blueprints/gym-membership.vpp` |
| Export | Visual Paradigm Community Edition 18.1 | PNGs with only the small Community Edition mark |

Table: How the blueprints were recovered

<!-- One or two sentences on what the records finding says about tools lagging the language, if you want it here
rather than in the Critique. -->

## Design-Time Package Diagram

![Recovered package diagram. A triangle marks a package whose classes implement interfaces in the other](../blueprints/package-diagram.png){width=100%}

<!--
Note under the diagram (your words): what it shows. Then compare it with the design-time diagram in §5 (Architecture).
What matches: domain depends on nothing; interceptor imports no business package; service implements port.in and
uses port.out; nothing imports service or the adapters.
-->

| Difference | Design (§5) | Recovered | Why |
| --- | --- | --- | --- |
| `dto` → `domain` | No edge | 16 imports | The response DTOs are built straight from domain objects |
| `controller` → `domain` | No edge | 2 imports | ErrorHandler maps NotFoundException and RuleViolationException to HTTP errors |
| `adapter.persistence` → `domain` | No edge | 4 imports | The JSON adapter stores domain objects directly: the liability admitted in §5 |
| Root package `gym` | Not drawn | BillingConfiguration → `domain`; InterceptorConfiguration → `interceptor`, `adapter.logging` | The composition root: Spring wiring of the discount Strategy and the Interceptor |
| Stereotypes | «driving adapter», «MVC model» … | None | Intent isn't in the code, so the recovered diagram can't show it |
| Booking, Member | Planned | Not in the code yet | Future release (Bharat's Command and Observer, DineshReddy's Member) |

Table: Design-time against recovered package diagram

<!-- Your take: which differences are drift we should fix, and which are the design being too tidy. -->

## Component Diagram

![Component diagram, built on the reversed port interfaces](../blueprints/component-diagram.png){width=100%}

<!--
Note under the diagram (your words): what it shows. Facts you could use:
- Each component is the class or classes named in it. Its stereotype is its Hexagonal role.
- A triangle from an interface means the component provides it; a dashed arrow to it means the component requires it.
- MembershipService requires four of Billing's use cases (CalculateCharges, RecordCharge, PayArrears, ViewInvoices),
  so Membership depends on Billing through port.in, not on BillingService directly. The design-time diagram doesn't
  show this coupling.
- BillingService needs all six driven ports. MembershipService needs three.
- UseCaseDispatcher wraps every port.in call in a dynamic proxy and calls LoggingInterceptor through UseCaseInterceptor.
- Every component also uses gym.domain; those edges are left out.
-->

## Deployment Diagram

![Deployment diagram](../blueprints/deployment-diagram.png){width=100%}

<!--
Note under the diagram (your words): what it shows. Facts you could use (code/Dockerfile, .railway/railway.ts):
- Railway (region iad) builds code/Dockerfile and redeploys main once CI passes (GitHub check suites).
- The Docker image is two stages: build on eclipse-temurin:21-jdk-noble, run on eclipse-temurin:21-jre-noble.
- Railway's proxy ends HTTPS and forwards to the jar's embedded Tomcat on $PORT.
- The JSON files live on the 500 MB gym-data volume at /data (GYM_PERSISTENCE_DIRECTORY=/data), so they survive
  redeploys. Health check: /actuator/health.
- Clients: Postman with live, mock and local environments, and the Swagger UI. The mock environment talks to
  Postman's mock server, not to Railway.
- How this maps to the hosting stack in §5: web server, application server, EIS (the volume), no message bus.
-->
