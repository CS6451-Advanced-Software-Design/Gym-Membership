# Lessons from the Sample Projects

Brightspace provides past submissions as samples. These notes are our own summary. The samples themselves are not in this repository.

- **Older samples**: AY19/20 (Django waste-collection manager) and AY21/22 (FastAPI zoo management).
- **AY25/26 samples** (released 6 Oct 2026, reviewed 15 Oct 2026): a route and fleet management system (Spring Boot, 74 pages) and a hotel reservation system (Spring Boot, 64 pages). These are the closest to this year's spec and to our stack.

## What they did well
- **Full use case template** (AY19/20): extensions, variations and open issues, with a traceable path from noun identification → CRC → boundary/control/entity → merged class diagram.
- **A real algorithm at the core** (AY19/20: weighted request scoring driving Command and Mediator. AY25/26 route: A* and Dijkstra behind a `PathfindingStrategy`). The patterns felt earned rather than bolted on.
- **Self-researched patterns visible in the code** (AY21/22: Domain Model and Data Mapper from Fowler, with a mapper interface plus per-entity mappers, and an Abstract Factory that makes the persistence layer swappable and mockable in tests).
- **Architecture justified by business context**, with candidate architectures compared before one was chosen (AY21/22).
- **Recovered package diagrams compared with the analysis versions**, with the differences explained (AY21/22).
- **Owner and due week for every deliverable**, a branch per developer, reviewed PRs, and an industry experience table (AY21/22).
- **Transparency tables** (AY25/26 route): classes per package, every class with author and LOC, LOC per member, and a responsibility matrix (area × member). The diary had contribution, issues and next-week columns.
- **Use case → implementing class mapping** in the code section (AY25/26 route), and a captioned code figure for each pattern.
- **Interfaces with pre/postcondition notes** on the analysis class diagram, plus enumerations and multiplicities (AY25/26 hotel).
- **A test for each design pattern** (AY25/26 hotel: decorator, state context and observer tests), about 30 test files in all.
- **A short note under each recovered diagram** saying which patterns it shows (AY25/26 hotel).
- **CI with service containers** (AY25/26 hotel): tests against real dependencies, then an image build and a Trivy scan.

## Where they fell short (don't repeat)
- **No real CI/CD** (AY19/20 or AY21/22 had only pre-commit hooks, pointing at wrong paths. AY25/26 route claimed "CI readiness" with no pipeline at all. AY25/26 hotel's workflow ran tests with `continue-on-error: true`, so failures never blocked anything, while the report said they did).
- **No metrics and no refactoring evidence in any of the four samples.** That is 2 marks (§9) this year. Their §9 sections filled the space with features, security, ORM and login, which the spec says don't count.
- **Architecture breaks the spec, or the claim isn't in the code.** AY25/26 route built 8 microservices (Eureka, Kafka) for Part 1, which must be monolithic, and never discussed it. AY25/26 hotel claimed Redis publish-subscribe as its self-researched pattern, but the only publish call is commented out. Its §8 then quietly switched to other patterns.
- **State chart for a different object than the State pattern** (AY25/26 hotel: chart for room status, State pattern for reservations).
- **Patterns mislabelled or overused**: a strategy selector called "Factory Method" (AY25/26 route), and three separate factories (AY25/26 hotel).
- **Tests were empty stubs, or a few end-to-end tests on hard-coded data** (older samples). AY25/26 route was mostly `contextLoads`. Write unit tests for business rules plus a few integration tests, and keep them repeatable.
- **Quality attributes were prose only.** Extensibility got two sentences, with no named Bass tactics (all samples). Write measurable scenarios with named tactics.
- **No interfaces with pre/postconditions** on the class diagram (older samples and AY25/26 route).
- **Invented numbers**: AY25/26 route listed repositories at 50 LOC each when they were 7–12 lines. AY25/26 hotel had only `git blame` totals, no per-class authorship.
- **History contradicting the diary**: 32 of 33 commits on submission day (AY25/26 route). No diary at all (AY25/26 hotel). Uneven contributions (264 vs 1,100 LOC in an older sample).
- **GenAI log thin or missing**: 13 one-line prompts (AY25/26 route). "We did not keep a complete prompt log" (AY25/26 hotel).
- **Recovered blueprints not from a UML workbench**: hand-drawn in older samples, and hand-written PlantUML in both AY25/26 samples. The route class boxes were empty, and the hotel component diagram was unreadable at page width.
- **Thin critique and reflection** (one generic paragraph, "me" inside a team report), and **presentation slips** (broken table-of-contents references, generic textbook prose, the wrong module code on the cover, the same decorator listed twice).

## Takeaways for us
1. Follow this year's section order exactly (§1–14).
2. Put the business rules (billing, waitlist, lifecycle) at the heart of the design, with patterns where they naturally fit. One pattern per problem; don't repeat Factory three times.
3. Make Hexagonal real in the package structure and enforce it with an ArchUnit test. Show it in the analysis and recovered package diagrams, and compare the two. Never claim anything in the report that isn't in the code.
4. Draw the state chart for `Membership`, the same object the State pattern implements, and keep it in the sequence diagram.
5. Set up CI (build, test, coverage) in Iteration 1, with failing tests blocking the merge. Run PMD and CK, refactor what they flag, and record the before and after numbers. This alone beats every sample on §9.
6. Generate every §7 number with a tool (`cloc`, `git blame`, a script) and say which. Commit every week so the history matches the diary.
7. Write a test for each design pattern, plus the business-rule unit tests.
8. Use a real UML workbench (Visual Paradigm) for §10 and keep the exported project as evidence. Check every diagram is readable at page width.
9. Log every prompt as we go, and write the critique and reflection in our own voice, each member separately.
