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
| Chirag | Chose Hexagonal with the team. Drafted §5 (package diagram, hosting stack) and §6 (candidate objects, class, sequence, state chart, ER). GenAI #5–15 | Iteration 0 late (due 4 Oct). Open: when a Trial pays, whether Lapsed ends in Cancelled, whether family plans are in scope | Skeleton, JSON repository, CI |
| Bharat | | | |
| DineshReddy | Took part in team discussions on the scenario and tech stack; reviewed Chirag's early §5/§6 drafts | No commits of my own yet; roles not settled until 7 Oct | Read up on the Member feature and the DevOps/CI role; review the report plan |
| Dhruv | Reviewed Chirag's §5 and §6 drafts (package diagram, candidate objects, class, sequence, state chart and ER diagrams) | No commits of my own yet; roles not settled until 7 Oct | Read the scenario rules for the BA role; review the report plan |

**Notes:**

## Week 6 (12 – 18 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Reviewed the new sample projects. Report plan and a tighter quality bar (CI blocks merges, ArchUnit, tool-made numbers, a test per pattern). GenAI #16–17 | No code yet, 2.5 weeks left; nobody else committing | Skeleton, JSON repository, CI; an issue per feature |
| Bharat | | | |
| DineshReddy | Reviewed the report plan and its quality bar; looked into CI/CD and metrics tooling (PMD, CK, SonarQube) ahead of building | Still no commits of my own, with the deadline approaching | Start committing: build the Member feature, review the CI/CD pipeline once it exists |
| Dhruv | Reviewed the report plan and its quality bar | Still no commits of my own, with 2.5 weeks left | Start committing: reorganise the repo, bring the diagrams up to date, build Membership |

**Notes:**

## Week 7 (19 – 25 Oct)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Spring Boot skeleton: `Repository` port and JSON adapter, ArchUnit, CI that fails on any test failure. Interceptor with logging. Billing feature: invoices, best-discount Strategy, referral cap, pro-rating, freeze, early-exit fee, arrears; `BillingJob`. Postman workspace, test split, CI metrics, Railway deploy, `v0.1.0`, report PDF build. GenAI #19–48, #51 | One week left; Booking and Member not built | §5; then §10 in Visual Paradigm and §11 |
| Bharat | | | |
| DineshReddy | | | |
| Dhruv | Split the repo into `report/` and `code/`. Redrew the use case, class, sequence, state chart, ER and package diagrams. Shared `Money` type, payment and notification ports (PR #13). Membership feature (PR #38): State pattern for the five states, `PlanFactory` for the four plans, UC1 sign-up, UC5 freeze, UC6 cancel, UC13 daily checks (`MembershipChecksJob`), UC14 pay arrears, with unit, integration and E2E tests. GenAI #18, #25–26, #49–50 | Membership had to bill through Billing's ports without changing `BillingService`; memberships already saved on Railway had to keep loading; `main` became protected, so everything goes through a PR | Draw the Membership sketches by hand; start §4 |

**Notes:**

## Week 8 (26 Oct – 1 Nov)

| Member | Contributions | Issues arising | Plan for next week |
| --- | --- | --- | --- |
| Chirag | Memberships in Postman, OpenAPI nullable fields, Railway auto-deploy on merge. §5 Architecture. Dhruv's sketches in §6, two to a row. Industry experience, lessons for the reflections. `v0.2.0`. GenAI #52–56 | Booking and Member still not built, so no Command or Observer; most report sections still placeholders; §5 had to be cut to fit 2 pages | Before 1 Nov: §10 in Visual Paradigm, §11 after SonarQube, §8 snippets, my reflection. Then the interview |
| Bharat | | | |
| DineshReddy | Member feature finished across every layer: `MemberController`, `MemberService`, `CheckIn` domain/ports/JSON persistence, tests, OpenAPI docs (PR #58). Reviewed it end to end and fixed real gaps: `Member.hasFamily()`, `MemberRepository.findByEmail()` with a direct test, two CI bugs (`curl -f`, `timeout-minutes`). Ran PMD and CK, picked `BillingService` and `MembershipService` as the worst classes, and refactored both (`BillingAccountFactory`, `MembershipDailyChecksService`), with before/after metrics kept in `report/metrics/`. §9 Added value and §14 References written from that evidence | SonarQube Cloud account/project not confirmed yet, so §9's SonarQube numbers are still pending | Confirm SonarQube Cloud, run a true before/after against the pre-refactor commit, finish §9. My reflection and GenAI log |
| Dhruv | Six hand-drawn Membership sketches: membership life, State, Plan Factory, sign-up, daily checks, booking and check-in (PR #44). Updated my row in `roles.md` (PR #45). Set up signed commits | §1, §4 and my §6 text not started yet, with the deadline on Sunday | Before 1 Nov: §4 (UC1, UC2, two quality attributes, two GUI prototypes), §6 candidate objects and captions, State and Factory snippets in §8, §1, my reflection |

**Notes:** Submission due 23:59 Wed 4 Nov (moved from Sun 1 Nov).
