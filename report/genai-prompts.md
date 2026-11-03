# GenAI Prompt Log

Report §13. The spec requires the **list of prompts** in the main body of the report. GenAI may bootstrap artefacts such as diagrams and code. It must **not** be used to improve the writing: the student voice must be audible.

Every prompt went to Claude through Claude Code. Each row keeps the prompts that shaped the output, in the member's own words; "…" marks follow-ups left out.

| # | Date | Member | Prompt | Used for |
| --- | --- | --- | --- | --- |
| 1 | 23 Sep | Chirag | "list 4-5 ideas" (for the scenario) | Scenario shortlist; the team chose gym membership |
| 2 | 23 Sep | Chirag | "help me with Assign roles, set up GitHub, look at existing projects, start requirements" | Repo scaffold, role split, first draft of actors, use cases and business rules |
| 3 | 30 Sep | Chirag | "make sure we use monolithic, not microservice" (with the package-by-layer slides) | Package by layer in roles.md |
| 4 | 30 Sep | Chirag | "whats the tech stack?" then "yep sounds good, document this" | tech-stack.md |
| 5 | 7 Oct | Chirag | "lets start on this - Iteration 0 … the architecture section (§5) and all analysis sketches (§6 …) haven't been started." Then chose Hexagonal | First PlantUML diagrams, candidate objects, architecture notes |
| 6 | 7 Oct | Chirag | "diagrams are way too complicated, should be simple, human made" | Simpler diagrams with fewer classes |
| 7 | 7 Oct | Chirag | "no dont use unprofessional lines... it should be normal diagrams" | Plain straight-line UML |
| 8 | 7 Oct | Chirag | "lets remove the code added for the diagrams, we only need the diagram images" | Use case diagram image; PlantUML sources removed |
| 9 | 7 Oct | Chirag | "assign random roles" | Random role split in roles.md |
| 10 | 7 Oct | Chirag | "research how hosting of this tech stack will take place, are there tools we can use to init the stack and maintain it" | Setup, hosting and maintenance notes in tech-stack.md |
| 11 | 7 Oct | Chirag | "can we use appwrite?" … "okay lets use it, but only for TablesDB" | Appwrite as a TablesDB adapter (dropped in #14) |
| 12 | 7 Oct | Chirag | "lets not add \"appwrite\" which is vendor name in the architecture code", "do we still keep it monolith if we use appwrite?" | Vendor-neutral names; note on staying a monolith |
| 13 | 7 Oct | Chirag | "can we still deploy with just this?" then "lets remove appwrite, keep it simple as it was" | Back to JSON files only |
| 14 | 7 Oct | Chirag | "lets add ascii diagram for tech stack in the tech stack md file" | ASCII stack and CI diagram |
| 15 | 17 Oct | Chirag | "can we first update our docs etc. till now to cover findings from those new sample projects" | report-plan.md and quality bar in the docs |
| 16 | 21 Oct | Dhruv | "can you explain me the folders and docs inside this org, also can we reorganise it in a better manner?" … | Repo split into report/ and code/ |
| 17 | 21 Oct | Chirag | "lets setup skeleton code, create pr" | Spring Boot skeleton, Repository port, JSON adapter, ArchUnit, CI |
| 18 | 21 Oct | Chirag | "can we use interceptor architectural pattern too? u can find slides for it on brightspace" … | Interceptor framework, LoggingInterceptor, tests, notes |
| 20 | 24 Oct | Dhruv | "let's start with point 1" (the shared groundwork every feature needs) | `Money`, payment and notification ports and adapters, `InMemoryRepository` |
| 21 | 25 Oct | Chirag | "study the work done so far ... make sure those diagrams dont contradict each other …" | Contradictions between the six diagrams fixed; class-to-entity mapping |
| 22 | 25 Oct | Chirag | "help me suggest what rough sketches i can make", "show me this - The hexagon, first draft", "explain the parts of hexagon architecture" | Reference for the hand-drawn hexagon sketch; explanation |
| 23 | 25 Oct | Chirag | "lets just focus on ours now. whats left for us" … "code so far does seem a bit complicated, keep in mind i will have to read it all and explain it by hand" | Billing feature: domain, `DiscountPolicy` Strategy, `BillingService`, controller, tests |
| 24 | 25 Oct | Chirag | "what should i sketch next" … "explain this design pattern to me" | Reference for the hand-drawn Strategy sketch; explanation |
| 25 | 25 Oct | Chirag | "lets keep it postman, cleaner ui we can show, can it be git tracked?" … "can we add a mock server too?" | Postman workspace, collection from Javadoc via OpenAPI, mock server |
| 26 | 25 Oct | Chirag | "what is CK metrics" … "split the CI jobs, post metrics as a comment, sha pin all actions too" | CI jobs split, metrics PR comment, pinned actions |
| 27 | 25 Oct | Chirag | "how can we deploy the code we have so far, can we use railway?" … "okay lets do railway hobby" | Dockerfile and Railway deploy |
| 28 | 25 Oct | Chirag | "logging looks pretty bad, how can we improve?" | JSON logs with use-case fields |
| 29 | 25 Oct | Chirag | "give me an artifact showing draft for “A worked invoice”" … | Reference for the hand-drawn worked invoice |
| 30 | 25 Oct | Chirag | "okay we shoukd start work on the actual report now, what is its format?" … "yes lets do both" | Report skeleton, PDF build, Report CI check (no prose) |
| 31 | 25 Oct | Chirag | "do we have a reference for how reports should be? can we check on previous submissions on brightspace" | Sample report layouts compared; PDF template tweaks |
| 32 | 25 Oct | Chirag | "explain what is Interceptor design pattern and what we do" … "what can i write for the architecture page in report" … "can we fix this?" | Explanations and a §5 fact outline (no prose); `BillingJob` |
| 33 | 25 Oct | Chirag | "in the report genai prompt table looks way too big, help me reduce its size" | Column widths and smaller text for this table |
| 34 | 25 Oct | Chirag | "i want entries themselves to be compacted too" | Shorter entries in this log |
| 35 | 25 Oct | Dhruv | "this is my part and what i have to do" (the Membership task list: sign-up, freeze, cancel, daily checks, State, Factory) | Membership feature: `Plan` classes and `PlanFactory`, the `MembershipState` classes, the Membership use cases, `MembershipService`, REST endpoints, the daily checks job and their tests |
| 36 | 25 Oct | Dhruv | "verify these sketches with the work i'm doing, and tell me they need any polishing" | Reference layouts (State with the crossed-out if/else chain, the lifecycle, the plan Factory, UC1 sign-up)  |
| 37 | 25 Oct | Chirag | "professor encouraged including teachings in report …" then "note down points for me to include in reflection" | lessons.md: lessons with their evidence (no report prose) |
| 38 | 27–28 Oct | Chirag | Chose "§10 Blueprints", then "can u use computer use?", "give this task to codex", "do 1." (Community Edition) and "keep checking its work" | The Visual Paradigm Instant Reverse run, the Open API plugin that drew the package, component and deployment diagrams on the reversed model, and §10's subheadings, tables and hint comments with the facts from the code |
| 39 | 28 Oct | DineshReddy | How to prove the check-in rule and its notification, how it works and what its usage is | `CheckIn` domain/ports, `MemberService`/`MemberController`, unit and end-to-end tests |
| 40 | 28 Oct | Dhruv | "write some short sample material for section 6 which i can use as a reference to build on" | short reference draft of section 6 |
| 41 | 28 Oct | Chirag | "add diagram of how logging interceptor was implemented", "just follow a single request", "make it less words, more diagram like", then "can we use this diagram" | The Interceptor request diagram in report/architecture |
| 42 | 28 Oct | Dhruv | "write some short sample material for section 4 which i can use as a reference to build on" | A short reference draft of §4 (use case introduction, UC1 and UC2, the two quality attributes, the two GUI prototypes) |
| 43 | 31 Oct | Bharat | "explain what a port interface like `CheckInRepository` is for, what the generics mean, and what I need for `GymClassRepository` (key `classId`)" | Explanation of port interfaces and generics, and the `GymClassRepository` port |
| 44 | 31 Oct | DineshReddy | "run PMD and CK and check which classes are the worst offenders" | Before/after snapshots for `MembershipService` and `BillingService` (`a41800d`) |
| 45 | 31 Oct | DineshReddy | "examined MembershipService and BillingService class by class against the CK numbers before deciding what to pull out" | `MembershipDailyChecksService` and `BillingAccountFactory` extracted, each kept to one real responsibility (`d3c974d`, `5f1127a`, `f70c0ab`, `ca39927`, `6b7d857`) |
| 46 | 1 Nov | DineshReddy | "check MembershipState against the same shared-singleton pattern PlanFactory already uses" | MembershipStateFactory extracted plus a Locale.ROOT fix found along the way (`88fe500`, `7973fbb`, `dd15c70`) |
| 47 | 1 Nov | Bharat | "explain how an existing use case is built (`CheckInUseCase` → `MembershipService`), then what `BookClassUseCase.book` and `BookingService` need" | Explanation of how a use case is built, then `BookClassUseCase`, the first `BookingService` and `BookingServiceTest`: booking a place in a class |
| 48 | 1 Nov | Bharat | "for `book`: which exception (`RuleViolationException` vs `NotFoundException`) and check order, and the unit tests" | Explanation of the exceptions, check order and unit tests, then the UC2 checks in `BookingService` and `BookingServiceTest` |
| 49 | 1 Nov | Bharat | "how to count active BOOKED bookings from `BookingRepository.findAll()`, join the waitlist at capacity, and what `book` returns" | Explanation of counting bookings and the return options, then the BR-B1 waitlist in `BookingService`, `BookingResult` and its tests |
| 50 | 1 Nov | Bharat | "how Observer promotes the first waitlisted member on cancel — my own interface vs `ApplicationEventPublisher`" | Explanation of Observer and the two options, then waitlist promotion for BR-B2 in `BookingService`, the shared `takePlace` step and their tests |
| 51 | 2 Nov | Bharat | "explain the Command pattern with our booking code (interface, commands, invoker, receiver), and what `undo` should do" | `BookingCommand`, `BookClassCommand`, `CancelBookingCommand`, `BookingCommandService`, `UndoBookingUseCase`, `BookingCommandsUseCase`, the undo steps in `BookingService`, the domain methods behind them, and their tests, then an explanation of the Command pattern in the booking code |
| 52 | 2 Nov | Bharat | "how to write `BookingE2E` like the other E2E tests, add the OpenAPI Javadoc examples, regenerate the spec, and check `BookingServiceTest` covers BR-B1–B6 and BR-M2" | `BookingE2E`, the example responses captured from the running app, the regenerated OpenAPI spec, and the Bookings folder check in `OpenApiSpecIT`, then an explanation of the end-to-end setup, the examples and spec, and a check of the tests against each rule |
| 53 | 3 Nov | Bharat | Asked for the §7 Transparency tables (classes per package, class × author × LOC, LOC per member) with cloc and git blame, and how each number is produced | `report/metrics/transparency.py`, which runs cloc 2.02 and git blame to make the three §7 tables and their captions; an explanation of each number; the facts for the Responsibilities table and an example of its shape; my rows checked against the repo and spelling fixed |
| 54 | 3 Nov | Chirag | "my friend dhruv initially wrote section 6(requirements) which he gave me to edit now, help me start a pr to replace it my own words" | A guide in the PR description: the facts each §4 paragraph must keep and which parts read as AI; §4 cleared to an empty skeleton with hidden notes; later "i dont like this one huge table, lets use points etc.", so the use cases became lists |
| 55 | 3 Nov | Dhruv | "explain me the purpose of section 1,3,4,6,7,8,12" according to brief | An explanation of what each of these sections is for according to the brief, which I used to check what my parts of the report needed to cover |
| 56 | 3 Nov | Dhruv | "explain in 2-3 bullet points what should the content in each sections look like" | An outline of the content for each of my sections, used to rewrite my §6 text, the State and Factory descriptions in §8 and my §12 reflection in the same simple style as my own §1 (PRs #156–158) |
| 57 | 3 Nov | Dhruv | "fix the grammar and spacing mistakes in the writting" | Grammar, punctuation and spacing fixes to the §1 narrative I wrote myself, keeping my wording (PR #155) |

Table: GenAI prompts
