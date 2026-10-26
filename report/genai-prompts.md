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
| 56 | 26 Oct | Chirag | "lets use full form" (for EIS), "can u add subheadings so i can write from there", "code snippets and tables too? word will be mine", "add helping comments what can i write", then "u can ask me for guidance, lets fill in proper sentences too" | §5 subheadings, tables, code snippets and hint comments; Claude asked questions and arranged Chirag's own answers into the Overview and MVC paragraphs, then fixed spelling and grammar in his Hexagonal paragraphs and grouped them under bold labels |

Table: GenAI prompts, all to Claude (Claude Code)
