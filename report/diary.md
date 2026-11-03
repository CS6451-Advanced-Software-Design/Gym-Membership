# Diary

Report §7. Reviewed in the lab. Fill in at the end of each week (spec Table 1 format).

## Week 4 (28 Sep – 4 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Package by layer, so Part 1 stays a monolith. Proposed the tech stack, agreed 30 Sep. GenAI #3–4 | Architecture and analysis (due 30 Sep) not started; no pattern chosen | Choose the pattern; draft §5 and §6 |
| Bharat | Took part in team meetings on the scenario and tech stack | | Choose roles. Review the architecture and analysis drafts |
| DineshReddy | Took part in team meetings on the scenario and tech stack | | Choose roles. Review the architecture and analysis drafts |
| Dhruv | Suggested project ideas for the scenario | | Choose roles. Review the architecture and analysis drafts |

**Notes:** The role split in `roles.md` is still a proposal. All repository commits so far are Chirag's, so the others need to start committing under their own accounts.

## Week 5 (5 – 11 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Chose Hexagonal with the team. Drafted §5 (package diagram, hosting stack) and §6 (candidate objects, class, sequence, state chart, ER). GenAI #5–14 | Iteration 0 late (due 4 Oct). Open: when a Trial pays, whether Lapsed ends in Cancelled, whether family plans are in scope | Skeleton, JSON repository, CI |
| Bharat | Added license to project, took Insights and reviewed UML, ER, State Chart & Sequence Diagram, Received the Hexagonal Architecture for the project from Chirag §5/§6 drafts | Roles not settled until 7 Oct | Understand the Project Manager role and features to be created, reviewed the report plans |
| DineshReddy | Went through the class and sequence diagrams, including the Hexagonal architecture, in Chirag's early §5/§6 drafts | No commits of my own yet; roles not settled until 7 Oct | Read up on the Member feature and the DevOps/CI role; review the report plan |
| Dhruv | Reviewed Chirag's §5 and §6 drafts (package diagram, candidate objects, class, sequence, state chart and ER diagrams) | No commits of my own yet; roles not settled until 7 Oct | Read the scenario rules for the BA role; review the report plan |

**Notes:**

## Week 6 (12 – 18 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Reviewed the new sample projects. Report plan and a tighter quality bar (CI blocks merges, ArchUnit, tool-made numbers, a test per pattern). GenAI #15 | No code yet, 2.5 weeks left; nobody else committing | Skeleton, JSON repository, CI; an issue per feature |
| Bharat | Reviewed the quality rules set for the project and checked Report plan | No commits yet, deadline getting closer. | Understand the Booking Part and Start committing: building the Booking features |
| DineshReddy | Reviewed the report plan and its quality bar; looked into CI/CD and metrics tooling (PMD, CK, SonarQube) ahead of building | Still no commits of my own, with the deadline approaching | Start committing: build the Member feature, review the CI/CD pipeline once it exists |
| Dhruv | Reviewed the report plan and its quality bar | Still no commits of my own, with 2.5 weeks left | Start committing: reorganise the repo, bring the diagrams up to date, build Membership |

**Notes:**

## Week 7 (19 – 25 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Spring Boot skeleton: `Repository` port and JSON adapter, ArchUnit, CI that fails on any test failure. Interceptor with logging. Billing feature: invoices, best-discount Strategy, referral cap, pro-rating, freeze, early-exit fee, arrears; `BillingJob`. Postman workspace, test split, CI metrics, Railway deploy, `v0.1.0`, report PDF build. GenAI #17–37 | One week left; Booking and Member not built | §5; then §10 in Visual Paradigm and §11 |
| Bharat | Reviewed the Interceptor Pattern and UC2 Book Class Sequence Diagram, go through how booking works for membership. | One week left before the final push, currently no commits. | Build and commit the Booking Part features |
| DineshReddy | Reviewed the Spring Boot skeleton and the CI pipeline as they were built; planned the Member feature's design | Still no commits of my own, one week left before the final push | Build and commit the Member feature end to end; audit the DevOps role |
| Dhruv | Split the repo into `report/` and `code/`. Redrew the use case, class, sequence, state chart, ER and package diagrams. Shared `Money` type, payment and notification ports (PR #13). Membership feature (PR #38): State pattern for the five states, `PlanFactory` for the four plans, UC1 sign-up, UC5 freeze, UC6 cancel, UC13 daily checks (`MembershipChecksJob`), UC14 pay arrears, with unit, integration and E2E tests. GenAI #16, #19–20, #35–36 | Membership had to bill through Billing's ports without changing `BillingService`; memberships already saved on Railway had to keep loading; `main` became protected, so everything goes through a PR | Draw the Membership sketches by hand; start §4 |

**Notes:**

## Week 8 (26 Oct – 1 Nov)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Memberships in Postman, Railway auto-deploy on merge. §5 Architecture. `v0.2.0` | Booking and Member still not built; §5 had to be cut to fit 2 pages | §10 in Visual Paradigm, §11, §8 snippets, my reflection |
| Bharat | Build the core objects for Booking: Booking, GymClass, Waitlist. Build two Interfaces - GymClassRepository and BookingRepository, and their Adapters. Build Booking rules in BookingService - UC2 Book place, UC3 cancel and waiting list, UC8 attendance or no-shows, UC10 add class and see schedule | Issue with Race condition coming; Command, Observer Pattern and the API not done yet | Command and Observer, Booking API Endpoints, End-to-End testing |
| DineshReddy | Member feature finished across every layer, with tests. Fixed two CI bugs (`curl -f`, `timeout-minutes`). Ran PMD and CK and refactored the two worst classes, `BillingService` and `MembershipService`. §9 Added value and §14 References | SonarQube Cloud not set up yet, so §9's numbers are pending | Confirm SonarQube Cloud, finish §9. My reflection |
| Dhruv | Six hand-drawn Membership sketches. §6 candidate objects and diagram notes. §4 use cases, quality attributes and GUI prototypes. §1 Narrative. State and Factory in §8. My reflection | Much of my report text was drafted with GenAI; §4 grew to 7 pages | Shorten and rewrite my sections in my own words |

**Notes:** Submission due 23:59 Wed 4 Nov (moved from Sun 1 Nov).

## Week 9 (2 – 8 Nov)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Brought the live app, Postman and the report up to date with Booking. Recovered the §10 blueprints again. Rewrote §4 and shortened §7 and §9 | Railway held the Booking merges for approval, so the live app was behind | Cut `v1.0.0`, submit; prepare for the walkthrough |
| Bharat | | | |
| DineshReddy | Wrote §13's intro. Re-checked `BookingService` with PMD/CK, no third refactor needed. Re-ran SonarQube after Booking merged and resolved the 2 `OffPeakPlan` false positives, Reliability D→A. Drew my two Member check-in sketches | Postman auto-publish still waiting on the workspace's GitHub link | My reflection; submit; prepare for the walkthrough |
| Dhruv | refactored the report a bit and made some changes in different sections of report | The report was 51 pages, most of it in §4 | Rewrite my sections in my own words |
