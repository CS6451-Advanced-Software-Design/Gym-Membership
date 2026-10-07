# GenAI Prompt Log

Report §13. The spec requires the **list of prompts** in the main body of the report. GenAI may bootstrap artefacts such as diagrams and code. It must **not** be used to improve the writing: the student voice must be audible.

The **Request** column summarises what was asked. The **Verbatim prompt** column records the exact wording typed, as the spec requires.

| # | Date | Member | Tool | Request | Verbatim prompt | Output and how it was used |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 2026-09-23 | Chirag | Claude (Claude Code) | Propose candidate scenarios that meet the spec's criteria for a business-rule-heavy system | "list 4-5 ideas" (asked after a question on what makes a good scenario) | A shortlist of scenarios. The team discussed it and chose gym and fitness club membership |
| 2 | 2026-09-23 | Chirag | Claude (Claude Code) | Bootstrap the Week 3 tasks: a role allocation, the GitHub repository, a review of the sample projects, and a first requirements draft | "help me with Assign roles, set up GitHub, look at existing projects, start requirements" | Repository scaffold, a role-split proposal, and draft actors, use cases, business rules and a use case diagram. The drafts are marked for the team to review and rewrite |
| 3 | 2026-09-30 | Chirag | Claude (Claude Code) | Check that the package structure fits the monolithic constraint of Part 1, against the two lecture slides on package by layer vs package by feature | "make sure we use monolithic, not microservice" | The package structure in roles.md was revised from package by feature to package by layer |
| 4 | 2026-09-30 | Chirag | Claude (Claude Code) | Recommend a technology stack for the business tier, tests, CI/CD and metrics, then write up the agreed choice | "whats the tech stack?", followed by "yep sounds good, document this" | A Java 21 + Spring Boot recommendation. The team reviewed and agreed it on 30 Sep, and it is recorded in tech-stack.md |
