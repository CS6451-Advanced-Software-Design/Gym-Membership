# Lessons Learned

Notes for the reflections (§12) and the critique (§11). The lecturer encouraged including what we learned along the way. These are facts with their evidence, not report text: write the report in your own words.

Add a lesson in the PR where it happened, while it's fresh. Say what we tried, what went wrong or changed, and what we'd do next time.

## Chirag

| Lesson | What happened | Evidence | Fits |
| --- | --- | --- | --- |
| The architecture decides the hosting | We first planned Appwrite as the data store (TablesDB), then dropped it to keep JSON files (7 Oct). When it came to deploying, platforms built on serverless functions or static and server-rendered sites didn't fit, because our monolith is a Spring Boot server that has to keep running and keep its files. Railway runs our Docker container all the time, with a persistent volume | GenAI #11–13, #27; `Dockerfile`, `.railway/railway.ts` | §12, §5 (alternatives considered) |
| Simple diagrams over complete ones | The first generated diagrams were too complicated to explain, so we cut them down to the classes that matter. The lecturer's note that the class diagram and the ER diagram should have a similar number of classes and entities found contradictions between our six diagrams | GenAI #6–7, #21; `report/analysis/README.md` | §12, §6 |
| Documentation drifts from the code | The architecture notes named a `BillingJob` (the clock driving UC7) that didn't exist until 25 Oct. We now check every claim against the code, and generate what we can from it (the OpenAPI spec and Postman collection come from the Javadoc) | GenAI #25, #32; `BillingJob`, `OpenApiSpecIT` | §11, §12 |
| Enforce the architecture with tests | ArchUnit fails the build if a dependency points the wrong way, so Hexagonal is checked on every PR, not just drawn | `ArchitectureTest` | §5, §8, §11 |
| The Interceptor paid off | Plain-text logs on Railway were hard to read. Because all logging lives in one interceptor, switching to JSON logs with use-case fields was a change to one class, with no service touched | GenAI #28; `LoggingInterceptor` | §5, §12 |
| Infrastructure as code catches mistakes | `railway config plan` showed that applying our config would recreate the data volume in another region. Pinning the region fixed it before anything was lost | GenAI #28; `.railway/railway.ts` | §12 |
| Keep code explainable | The first Billing code was more complicated than it needed to be, and we have to explain every line by hand, so it was simplified. Seeded demo data was dropped too | GenAI #23 | §12 |
| Don't stack pull requests | Stacking PRs on each other (#10–#12) blocked merges, and deleting a parent branch closed PR #9. Every PR now branches from `main` | PRs #9–#12 | §2, §12 |
| Remove what doesn't earn its place | We dropped a smoke test because another test already started the app, and a demo Postman collection because the generated one covered it | GenAI #25, #26 | §9, §12 |
| Keep logs compact as you go | The GenAI log grew to 6 pages of the PDF before we shortened the entries to 2 | GenAI #33–34 | §13 |
| Check the samples against the spec | One AY25/26 sample report ran its §2 to 4 pages against a 2-page limit, and one had no references. Our build fails if a section goes over its limit | GenAI #31; `report/pdf/build.sh` | §12 |

## DineshReddy

| Lesson | What happened | Evidence | Fits |
| --- | --- | --- | --- |
| The Observer plumbing already paid for itself | Wiring check-in notifications for the Member feature, I expected to build a new notification channel. `NotificationSender`, `EventNotificationSender` (Spring's event bus) and `NotificationLog` already existed from the Membership and Billing work. Calling `notifications.send(...)` from `MemberService` was the whole cost; no new adapter, no new listener. A fourth feature using Observer confirms it: adding a notification site is a one-line call, not a new publisher/subscriber pair | `MemberService`; `EventNotificationSender`, `NotificationLog` | §5, §8, §12 |
| PMD's GodClass flag goes away exactly where the ports already drew the line | Refactoring `BillingService`, I pulled `account()` and its two private helpers into a new `BillingAccountFactory`, following the seam `port.in` already draws around what Billing needs from a membership. That one cut was enough: PMD's `GodClass` violation disappeared entirely (WMC 54→44), and `MemberRepository` turned out to be used by nothing else in `BillingService`, so it dropped out completely instead of just moving. The metric agreed with the architecture once the extraction followed an existing interface boundary instead of an arbitrary one | `BillingAccountFactory`, `MembershipDailyChecksService`; `report/metrics/before-refactor/`, `after-refactor-2/` | §9, §11, §12 |
| Reading the pipeline line by line surfaces silent failures | `curl -sLo` for the PMD/CK jars had no `-f`, so a broken download would silently save an HTML error page as the `.jar`; none of the jobs had `timeout-minutes`, so a hung build could run for GitHub's 6-hour default | `.github/workflows/ci.yml`, `release.yml`, `report.yml` | §9, §12 |
| A metric needs a human to tell real from false | SonarQube flagged 7 High severity Reliability issues; 5 were genuine (`MembershipState` holding static subclass references, rule S2390, fixed via `MembershipStateFactory`), 2 were `DayOfWeek != ` comparisons in `OffPeakPlan` (rule S8696), safe since it's an enum — resolved as false positive rather than changing working code | `MembershipStateFactory`; SonarQube Cloud Reliability rating A | §9, §12 |
| Set up the quality gate before, not after | SonarQube Cloud was only wired up after the PMD/CK refactors were already merged, so the before/after numbers aren't a true per-commit comparison. Next time I'd set it up as soon as the first feature existed, not as a last-week task | §9's SonarQube Cloud section | §12 |
