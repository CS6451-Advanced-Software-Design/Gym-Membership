# GenAI Prompt Log

Report §13. The spec requires the **list of prompts** in the main body of the report. GenAI may bootstrap artefacts such as diagrams and code. It must **not** be used to improve the writing: the student voice must be audible.

Every prompt went to Claude through Claude Code. Each row keeps the prompts that shaped the output, in the member's own words; "…" marks follow-ups left out.

| # | Date | Member | Prompt | Used for |
| --- | --- | --- | --- | --- |
| 1 | 23 Sep | Chirag | "list 4-5 ideas" (for the scenario) | Scenario shortlist; the team chose gym membership |
| 2 | 23 Sep | Chirag | "help me with Assign roles, set up GitHub, look at existing projects, start requirements" | Repo scaffold, role split, first draft of actors, use cases and business rules |
| 3 | 30 Sep | Chirag | "make sure we use monolithic, not microservice" (with the package-by-layer slides) | Package by layer in roles.md |
| 4 | 30 Sep | Chirag | "whats the tech stack?" then "yep sounds good, document this" | tech-stack.md |
| 5 | 7 Oct | Chirag | "help me continue the gym membership project i am working on, first study whats done so far" | Status review; nothing used |
| 6 | 7 Oct | Chirag | "lets start on this - Iteration 0 … the architecture section (§5) and all analysis sketches (§6 …) haven't been started." Then chose Hexagonal | First PlantUML diagrams, candidate objects, architecture notes |
| 7 | 7 Oct | Chirag | "diagrams are way too complicated, should be simple, human made" | Simpler diagrams with fewer classes |
| 8 | 7 Oct | Chirag | "no dont use unprofessional lines... it should be normal diagrams" | Plain straight-line UML |
| 9 | 7 Oct | Chirag | "lets remove the code added for the diagrams, we only need the diagram images" | Use case diagram image; PlantUML sources removed |
| 10 | 7 Oct | Chirag | "assign random roles" | Random role split in roles.md |
| 11 | 7 Oct | Chirag | "research how hosting of this tech stack will take place, are there tools we can use to init the stack and maintain it" | Setup, hosting and maintenance notes in tech-stack.md |
| 12 | 7 Oct | Chirag | "can we use appwrite?" … "okay lets use it, but only for TablesDB" | Appwrite as a TablesDB adapter (dropped in #14) |
| 13 | 7 Oct | Chirag | "lets not add \"appwrite\" which is vendor name in the architecture code", "do we still keep it monolith if we use appwrite?" | Vendor-neutral names; note on staying a monolith |
| 14 | 7 Oct | Chirag | "can we still deploy with just this?" then "lets remove appwrite, keep it simple as it was" | Back to JSON files only |
| 15 | 7 Oct | Chirag | "lets add ascii diagram for tech stack in the tech stack md file" | ASCII stack and CI diagram |
| 16 | 15 Oct | Chirag | "help me continue my team based assignment for cs6451 … use playwright to actually see the assignment details" | Summary of the new sample projects; nothing used |
| 17 | 17 Oct | Chirag | "can we first update our docs etc. till now to cover findings from those new sample projects" | report-plan.md and quality bar in the docs |
| 18 | 21 Oct | Dhruv | "can you explain me the folders and docs inside this org, also can we reorganise it in a better manner?" … | Repo split into report/ and code/ |
| 19 | 21 Oct | Chirag | "lets add a diagram overview in readme" | Diagrams section in the README |
| 20 | 21 Oct | Chirag | "raise a pr to add a new quality point - include rough work/sketches photos" | report/sketches/ and its README |
| 21 | 21 Oct | Chirag | "add point to add code metrics after implementation is done, like solarq" | SonarQube Cloud before and after refactoring, in the plan |
| 22 | 21 Oct | Chirag | "lets setup skeleton code, create pr" | Spring Boot skeleton, Repository port, JSON adapter, ArchUnit, CI |
| 23 | 21 Oct | Chirag | "can we use interceptor architectural pattern too? u can find slides for it on brightspace" … | Interceptor framework, LoggingInterceptor, tests, notes |
| 24 | 21 Oct | Chirag | "in readme lets add a small section listing design patterns used so far and where, also include whats in plan" | Patterns section in the README |
| 25 | 24 Oct | Dhruv | "edit/update/remake all the diagrams with this project idea in mind and commit them to github one by one, not all at once" | All six diagrams redrawn to match the rules and code |
| 26 | 24 Oct | Dhruv | "let's start with point 1" (the shared groundwork every feature needs) | `Money`, payment and notification ports and adapters, `InMemoryRepository` |
| 28 | 25 Oct | Chirag | "study the work done so far ... make sure those diagrams dont contradict each other …" | Contradictions between the six diagrams fixed; class-to-entity mapping |
| 29 | 25 Oct | Chirag | "help me suggest what rough sketches i can make", "show me this - The hexagon, first draft", "explain the parts of hexagon architecture" | Reference for the hand-drawn hexagon sketch; explanation |
| 30 | 25 Oct | Chirag | "lets just focus on ours now. whats left for us" … "code so far does seem a bit complicated, keep in mind i will have to read it all and explain it by hand" | Billing feature: domain, `DiscountPolicy` Strategy, `BillingService`, controller, tests |
| 31 | 25 Oct | Chirag | "what should i sketch next" … "explain this design pattern to me" | Reference for the hand-drawn Strategy sketch; explanation |
| 32 | 25 Oct | Chirag | "suggest what improvements we can make to our ci" then "start pr" | JaCoCo, PMD and CK in CI |
| 33 | 25 Oct | Chirag | "can we make all the images in readme be of similar size?" | README image widths |
| 34 | 25 Oct | Chirag | "lets keep it postman, cleaner ui we can show, can it be git tracked?" … "can we add a mock server too?" | Postman workspace, collection from Javadoc via OpenAPI, mock server |
| 35 | 25 Oct | Chirag | "what is CK metrics" … "lets create separate pr for splitting jobs into multiple. posting metrics as a comment, and fixing these logs" … "lets sha pin all our actions too" | CI jobs split, metrics PR comment, pinned actions |
| 36 | 25 Oct | Chirag | "see if we can add end to end tests in a separate pr" … "can we split the tests to be defined clearly as unit tests, integration tests and e2e tests" | Unit, integration and end-to-end test split; `BillingE2E` |
| 37 | 25 Oct | Chirag | "how can we deploy the code we have so far, can we use railway?" … "okay lets do railway hobby" | Dockerfile and Railway deploy |
| 38 | 25 Oct | Chirag | "can we update docs etc. to use this live link …" then "can we make the Swagger UI the default root url" | Live link in docs and Postman; Swagger UI at the root |
| 39 | 25 Oct | Chirag | "u can merge" (then checking the live app) | Fix for the https redirect behind Railway |
| 40 | 25 Oct | Chirag | "can we follow a specific format to show this so its easier to exactly see where a pattern is implemented?" … | Patterns table in the README |
| 41 | 25 Oct | Chirag | "logging looks pretty bad, how can we improve?" | JSON logs with use-case fields |
| 42 | 25 Oct | Chirag | "can we make a github release? what would that include?" then "yes" | Release workflow and `v0.1.0` |
| 43 | 25 Oct | Chirag | "give me an artifact showing draft for “A worked invoice”" … | Reference for the hand-drawn worked invoice |
| 44 | 25 Oct | Chirag | "okay we shoukd start work on the actual report now, what is its format?" … "yes lets do both" | Report skeleton, PDF build, Report CI check (no prose) |
| 45 | 25 Oct | Chirag | "do we have a reference for how reports should be? can we check on previous submissions on brightspace" | Sample report layouts compared; PDF template tweaks |
| 46 | 25 Oct | Chirag | "explain what is Interceptor design pattern and what we do" … "what can i write for the architecture page in report" … "can we fix this?" | Explanations and a §5 fact outline (no prose); `BillingJob` |
| 47 | 25 Oct | Chirag | "in the report genai prompt table looks way too big, help me reduce its size" | Column widths and smaller text for this table |
| 48 | 25 Oct | Chirag | "i want entries themselves to be compacted too" | Shorter entries in this log |
| 49 | 25 Oct | Dhruv | "this is my part and what i have to do" (with the task list: sign-up, freeze, cancel, daily checks, State on Membership, Factory for plans) | Membership feature: `Plan` classes and `PlanFactory`, the `MembershipState` classes, the Membership use cases, `MembershipService`, REST endpoints, the daily checks job and their tests |
| 50 | 25 Oct | Dhruv | "make some sketches related to my work and save that in mac's folder" | Reference layouts (State with the crossed-out if/else chain, the lifecycle, the plan Factory, UC1 sign-up) that Dhruv redrew by hand |
| 51 | 25 Oct | Chirag | "oh this reminds me, professor encouraged including teachings in report …" then "perhaps note down points for me to include when writing reflection etc." | lessons.md: lessons with their evidence (no report prose) |
| 52 | 26 Oct | Chirag | "can we similarly compact diary notes?" | Shorter diary entries and smaller diary tables |
| 53 | 26 Oct | Chirag | "can we sync with main, dhruv added more code it seems, we might need to update postman etc. to reflect that" | Memberships in the Postman collection, Railway auto-deploy fixed, new Swagger screenshot |
| 54 | 26 Oct | Chirag | "memberships folder should come before billing", a screenshot of Postman flagging `null` as "expected string" and `/memberships` as not a "uri" with "fix", then "lets use more real looking ids in examples" | Nullable fields and URI references in the spec, folder order, generated-style example ids |
| 55 | 26 Oct | Chirag | "why does readme stil show previous image?" | New Postman and Swagger UI screenshots in the READMEs |
| 56 | 26 Oct | Chirag | "lets use full form" (for EIS), "can u add subheadings so i can write from there", "code snippets and tables too? word will be mine", "add helping comments what can i write", then "u can ask me for guidance, lets fill in proper sentences too" | §5 subheadings, tables, code snippets and hint comments; Claude asked questions and arranged Chirag's own answers into the Overview and MVC paragraphs, then fixed spelling and grammar in his Hexagonal paragraphs and grouped them under bold labels; the package tree ("show me file org we can show"), varied sentence openings ("all sentences start with we, lets change"), the two snippets moved to §8 for the page limit, and spelling, grammar and fact fixes in his Interceptor paragraphs and the line after its table; grammar fixes in his Alternatives paragraphs; the Hosting Stack sentence replacing the table ("lets do this", choosing it from Claude's options to fit the page limit); grammar fixes in his Liabilities bullets, which build on Claude's explanation of where the persistence adapter bends Hexagonal ("where did we bent the pattern?") |
| 57 | 27–28 Oct | Chirag | Chose "§10 Blueprints" to start, then "can u use computer use to do this?", "can u give this task to codex, which does have computer use", "do 1." (Community Edition for the exports) and "keep checking its work" | The Visual Paradigm Instant Reverse run, the Open API plugin that drew the package, component and deployment diagrams on the reversed model, and §10's subheadings, tables and hint comments with the facts from the code |
| 58 | 28 Oct | Chirag | "lets remove/trim down on more text that is needed in these blueprints pages, we mainly want to show diagrams", "yes choose one and do it", "do fix the facts too if u can" and "make fixes as needed" | Grammar and factual fixes to my §10 prose, shorter §10 tables, and a landscape page for the component diagram |
| 59 | 28 Oct | Chirag | "yes prepare no. 8" | §8's use case → classes table, the Strategy snippets and test, the test counts and coverage table, the version control charts (`report/version-control/charts.py`), and hint comments with the facts from the code |
| 60 | 28 Oct | Chirag | "was it mentioned to add this commits graph?" then "yeah lets do that create pr" | Team-level version control charts in §8 (commits per week, pull requests merged over time) instead of the per-member ones |
| 61 | 28 Oct | Chirag | "added more words", "what is JaCoCo", "added more lines" and "added last line" | Grammar and factual fixes to my §8 prose, and the version control charts laid out side by side |
| 62 | 27 Oct | DineshReddy | What's actually been built for the Member feature so far, and what's left under my name | Status review of the repo (found zero prior commits); a personal task list kept outside the repo |
| 63 | 28 Oct | DineshReddy | How to prove the check-in rule and its notification, how it works and what its usage is | `CheckIn` domain/ports, `MemberService`/`MemberController`, unit and end-to-end tests |
| 64 | 28 Oct | DineshReddy | Why the OpenAPI spec and Postman collection kept drifting out of sync, and why PMD kept failing locally | Regenerated the spec/collection after a stale-spec build failure; traced the PMD failures to a JDK mismatch, fixed with an enforcer rule |
| 65 | 28 Oct | DineshReddy | Figuring out what counts as a real lesson for §11/§12 versus just a note about a git mistake | A lesson on reusing the existing Observer notification plumbing, in place of a first-draft workflow note |
| 66 | 28 Oct | Chirag | "lets exclude those for now, witha  small comment those can bea dded too" (the SonarQube Cloud numbers) and "i think DineshReddy merged in changes", then "lets do 2 for now" (label §10's diagrams as a snapshot) | §11's subheadings, metrics table and hint comments with the facts from the code and CI; §8's test counts and use case table brought up to date with the Member feature (#58); DineshReddy's rows renumbered 62–65, as they clashed with 57–60; §10's diagrams labelled as recovered before #58 |
| 67 | 28 Oct | Chirag | "lets create next pr to update postman screenshots deployed link, swagger etc. everything related to add new endpoints dinesh added" | Deploying the Member endpoints (#58) to Railway, their Javadoc and path examples fixed, the spec and Postman collection regenerated and published, and new Swagger UI and Postman screenshots |
| 68 | 28 Oct | Dhruv | Asked for §6 to be written for me, with the lecturer's permission to use AI for writing: how we found the candidate objects, a note under each analysis diagram, and an introduction to the rough work photos | §6: the candidate objects method and noun table, the notes under the class, sequence, state chart and ER diagrams, and the rough work introduction |
| 69 | 28 Oct | Chirag | "add diagram of how logging interceptor was implemented in our gym project", "instead of complicated diagram, can we just follow a single request", "make it less words, more diagram like" and "i like the diagram, can we raise a pr to add it to our project", then "i meant if we can use this diagram" | The Interceptor request diagram in report/architecture |
| 70 | 26 Oct | Dhruv | "commit all of my rough work sketches to github" | My six hand-drawn Membership sketches in `report/sketches`, with a README row for each saying what changed afterwards (#44) |
| 71 | 26 Oct | Dhruv | "update my roles in roles.md" | My row in the role split in `roles.md` (#45) |
| 72 | 28 Oct | Dhruv | "fill and commit the changes in diary week from 5-8 on all the work i have done" | My diary rows for Weeks 5 to 8, from my commits and pull requests (#59) |
| 73 | 28 Oct | Dhruv | "use this resume to fill in industry experience in the report" | My row in the §3 industry experience table (#61) |
| 74 | 28 Oct | Dhruv | "the AI log is showing text lines after log no 61, how to fix that?" | Removed the blank line that split this table after row 61 (#67) |
| 75 | 28 Oct | Dhruv | "write some material for section 4 for reference which i will edit later" | A full draft of §4 (use case introduction, UC1 and UC2, the two quality attributes, the two GUI prototypes), merged as written, for me to rewrite in my own words before submission (#68–#72) |
| 76 | 28 Oct | Dhruv | "do the same with section 1 ad sone for section 4" | A full draft of the §1 narrative, merged as written, for me to rewrite in my own words before submission (#74) |
| 77 | 28 Oct | Dhruv | "do the same with section 8 and add code snippets and test snippets with short description below" | The State and Factory parts of §8: the code and test snippets from my feature and the text around them, merged as written, for me to rewrite in my own words before submission (#76, #77) |
| 78 | 28 Oct | Dhruv | "give some outline for section 12:reflection, i will write it in my own words" | A full draft of my §12 reflection from what happened in the project, merged as written, which I am rewriting in my own words before submission (#78) |
| 79 | 31 Oct | Chirag | "see my https://github.com/CS6451-Advanced-Software-Design/Gym-Membership work so far, my friends added lot more work, so i need handle syncing of it etc. for eg. updating screenshots, approving the railway deployment so everything is live" | The Booking deploy approved on Railway, the version control charts redrawn and the §8 test table brought up to date with the Booking tests |
| 80 | 31 Oct | Chirag | "help me start work on §11 Critique" | The §11 metrics table and fact notes brought up to date with the CI run on 89e9630, then explanations of each subsection's facts and grammar and factual fixes to the §11 lines I wrote |
| 81 | 31 Oct | Chirag | "help me start reflection", "i want mine in more of a points format", then "added my lines, refine and create pr" and "try keep my original language as much as possible" | Facts to draw on from the lessons, roles and diary, a points layout, and grammar and factual fixes to the reflection I wrote |
| 82 | 31 Oct | Bharat | "get all my tasks on name Bharat and tell me what tasks are completed and what need to be done, go through complete folder" | Status review of the repo; a personal task list kept outside the repo |
| 83 | 31 Oct | Bharat | "can you run this in locally" | The app run locally and its endpoints tried; nothing committed |
| 84 | 31 Oct | Bharat | "In our hexagonal Java project, CheckInRepository extends `Repository<CheckIn, String>` and has no body. Explain in simple words what a port interface like this is for, what the generics mean, and what I need to write for GymClassRepository for my GymClass record (key is classId)" | Explanation of port interfaces and generics, and the `GymClassRepository` port |

| 85 | 31 Oct | Bharat | "I wrote GymClassRepository. What is different for BookingRepository, whose record is Booking and key is bookingId? Describe the steps and check my file when I paste it." | The steps for the `BookingRepository` port and a check of the file |

| 86 | 31 Oct | Bharat | "Explain line by line what JsonFileCheckInRepository does: extends JsonFileRepository, implements, @Repository, and each argument of super(...). Then tell me which four values I need for classes.json with classId as the key" | Explanation of the JSON adapter, and `JsonFileGymClassRepository` |

Table: GenAI prompts, all to Claude (Claude Code)
