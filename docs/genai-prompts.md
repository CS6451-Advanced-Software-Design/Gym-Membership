# GenAI Prompt Log

Report §13. The spec requires the **list of prompts** in the main body of the report. GenAI may bootstrap artefacts such as diagrams and code. It must **not** be used to improve the writing: the student voice must be audible.

| # | Date | Member | Tool | Prompt | Used for |
| --- | --- | --- | --- | --- | --- |
| 1 | 2026-09-23 | Chirag | Claude (Claude Code) | "list 4-5 ideas" (for the scenario, after asking what makes a good idea) | Scenario shortlist. The team chose gym membership |
| 2 | 2026-09-23 | Chirag | Claude (Claude Code) | "help me with Assign roles, set up GitHub, look at existing projects, start requirements" | Repo scaffold, role-split proposal, bootstrap draft of actors, use cases, business rules and the use case diagram |
| 3 | 2026-09-30 | Chirag | Claude (Claude Code) | "make sure we use monolithic, not microservice" (with the two lecture slides on package-by-layer vs package-by-feature) | Rewrote the package structure in roles.md to package by layer |
| 4 | 2026-09-30 | Chirag | Claude (Claude Code) | "whats the tech stack?" then "yep sounds good, document this" | Tech stack recommendation, written up in tech-stack.md |
| 5 | 2026-10-07 | Chirag | Claude (Claude Code) | "help me continue the gym membership project i am working on, first study whats done so far" | Status review of the repo against the spec. No artefact used |
| 6 | 2026-10-07 | Chirag | Claude (Claude Code) | "lets start on this - Iteration 0 (due 4 Oct): the architecture section (§5) and all analysis sketches (§6: class, sequence, state chart, ER) haven't been started." Then chose Hexagonal from four suggested patterns | Bootstrap PlantUML diagrams (package, ports and adapters, class, UC2 sequence, Membership state chart, ER), candidate object table and architecture notes |
| 7 | 2026-10-07 | Chirag | Claude (Claude Code) | "diagrams are way too complicated, should be simple, human made" | Redrew all diagrams as small hand-drawn sketches with fewer classes |
| 8 | 2026-10-07 | Chirag | Claude (Claude Code) | "no dont use unprofessional lines... it should be normal diagrams" | Switched the diagrams back to plain straight-line UML |
| 9 | 2026-10-07 | Chirag | Claude (Claude Code) | "lets remove the code added for the diagrams, we only need the diagram images" | Rendered the use case diagram to an image with a cleaner layout and removed all PlantUML sources |
| 10 | 2026-10-07 | Chirag | Claude (Claude Code) | "assign random roles" | Randomly matched members to the four role sets in roles.md |
| 11 | 2026-10-07 | Chirag | Claude (Claude Code) | "research how hosting of this tech stack will take place, are there tools we can use to init the stack and maintain it" then "yes lets add those notes" | Project setup, hosting options and maintenance tooling in tech-stack.md |
| 13 | 2026-10-07 | Chirag | Claude (Claude Code) | "lets not add \"appwrite\" which is vendor name in the architecture code", "do we still keep it monolith if we use appwrite?" then "okay lets use it, but only for TablesDB" | Vendor-neutral adapter, profile and setting names, Appwrite limited to TablesDB, and a note on why it stays a monolith |
