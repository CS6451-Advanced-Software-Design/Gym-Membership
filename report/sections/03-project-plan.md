# Project Plan {#project-plan}

<!--
Owner: Bharat.
An iterative plan: each iteration with its deadline, deliverables and owner. A short paragraph per member on their
role and contribution. The industry experience table (domains, languages, frameworks) for all four members.
Evidence: report/roles.md, the GitHub milestones, the plan table in the course README.
-->

| Iteration | Deadline | Deliverables | Owner |
| --- | --- | --- | --- |
| Week 3 | 27 Sep | Project docs, role proposal, first draft of requirements | Chirag |
| Week 4 | 4 Oct | Scenario chosen (gym membership), tech stack (Java, Spring Boot), monolith with package by layer; Iteration 0 (analysis and architecture) due, but late | All; Chirag (tech stack) |
| Week 5 | 11 Oct | Roles and features assigned, Hexagonal chosen, §5 and §6 diagrams drafted, licence added | All; Chirag (§5, §6); Bharat (licence) |
| Week 6 | 18 Oct | Quality rules: CI blocks bad merges, ArchUnit, numbers from tools, a test per pattern (PR #6) | Chirag |
| Week 7 | 25 Oct | Spring Boot skeleton, CI, Interceptor, Billing (Strategy), Membership (State, Factory), Railway deploy, report PDF build; release v0.1.0 | Chirag; Dhruv |
| Week 8 | 1 Nov | Member feature, Booking domain, ports, adapters and rules, SonarQube and deploy check, §4, §5 and §6 written; releases v0.2.0 and v0.3.0 | DineshReddy; Bharat; Chirag; Dhruv |
| Week 9 | 4 Nov | Command and Observer for Booking, Booking API, end-to-end tests, the full report; release v1.0.0 prepared | Bharat; All (report) |

## Roles and Contributions

**Chirag Aggarwal.** Chirag was the Architect and Designer of the team, and owned the Billing feature. As the Architect, Chirag turned the architecture we decided on together (MVC with Hexagonal) into the package diagram and wrote §5, and added ArchitectureTest so that the layering is enforced in the code. As the Designer, Chirag recovered the §10 blueprints from the code in Visual Paradigm and wrote the §11 critique. Chirag also drafted the first version of the §6 diagrams and rewrote §4 near the end. In the code, Chirag built the Spring Boot skeleton, the Repository port with its JSON adapter and the Interceptor. For Billing, Chirag built invoices, the best-discount Strategy, the referral cap, pro-rating, the early-exit fee, arrears and BillingJob. Chirag also set up the Railway deployment, the Postman workspace, the split of tests into unit, integration and end-to-end, and the report PDF build.

**Bharat Doodi.** Bharat was the Project Manager and Tester of the team, and owned the Booking feature, which lets members reserve a place in a gym class and enforces the rules about it. The features Bharat created include booking, the waitlist, cancelling, attendance and the schedule. Bharat worked on the Command pattern (undo) and Observer for the waitlist notification, and on the Booking API and end-to-end testing. Bharat wrote the most test code in the team, around 1,857 lines, and made the §7 tables with cloc and git blame. As the Project Manager, Bharat also handled the diary, §2 Software Lifecycle, which describes the lifecycle we followed, and §3 Project Plan, which includes the iteration table, the team members' roles and contributions, and their work experience.

**Dhruv Punj.** Dhruv was the Business and Systems Analyst of the team, and owned the Membership feature. As the BA, Dhruv settled the open business rules, like the pro-rated first charge, Annual starting Active and cancelling after 60 days in Lapsed, and wrote the narrative and the first version of the requirements. As the Systems Analyst, Dhruv found the candidate objects, redrew the class, sequence, state chart and ER diagrams to match our rules and wrote the notes under them. In the code, Dhruv built Membership using the State pattern for the five states and a Factory for the four plans, with sign-up, freeze, cancel, the daily checks and paying arrears, plus the shared Money type and the payment and notification ports.

**DineshReddy Mogili.** DineshReddy was the Documentation Manager and Technical Lead + DevOps of the team, and was responsible for the Member check-in feature. As Technical Lead, DineshReddy worked on the CI pipeline, SonarCloud, code quality checks, Docker builds for every pull request, and checking that the application was deployed correctly. DineshReddy also worked on improving BillingService and MembershipService using PMD and CK metrics, which helped reduce their complexity. For the Member feature, DineshReddy developed the check-in functionality and used the existing Observer notification system to send notifications. DineshReddy also created the sequence diagrams for successful and refused check-ins, added unit and end-to-end tests, and updated the API documentation. As Documentation Manager, DineshReddy helped prepare the final report, and checked the diagrams and formatting.

## Industry Experience

| Member | Domains | Languages | Frameworks and tools |
| --- | --- | --- | --- |
| Chirag Aggarwal | Cloud backend platform (Appwrite, Platform Engineer, Dec 2024 to now); ed-tech (Skillarena, Backend Developer, 2024); AI wellness (Clearmind AI, Full-stack Developer, 2023) | PHP, Go, TypeScript, JavaScript, Python | Swoole, Node.js, Express, React, MongoDB, FastAPI, PostgreSQL, Docker, Kubernetes, Grafana, OpenTelemetry, Stripe |
| Bharat Doodi | Salesforce platform development for internal and client-facing projects, including a retail application with a product catalog and payment page (Briskminds Software Solutions, Associate Software Engineer, Jan 2024 to Feb 2025); front-end web development (IBM SkillsBuild, Frontend Web Developer Intern, Jun to Jul 2023) | Apex, SOQL, JavaScript, HTML, CSS, Python, SQL, C++, C | Salesforce (Lightning Web Components, Aura Components, Visualforce, Salesforce Administration), Node.js, Express.js, MongoDB, MySQL, Pandas, NumPy |
| DineshReddy Mogili | Cloud-based software platforms (SJ Digitech, Full Stack Developer, Aug 2025 to now); e-commerce and marketplace development (Oruphones, Full Stack Developer Intern, Feb to Aug 2025); blockchain-based healthcare systems (NIT Warangal); AI-powered skincare and skin analysis application (Ayoka) | JavaScript, TypeScript, C++, Solidity, SQL, Bash, HTML, CSS | Next.js, React.js, Node.js, Express.js, Hono.js, Cloudflare Workers, Cloudflare D1, Cloudflare R2, Drizzle ORM, Prisma, MongoDB, PostgreSQL, MySQL, SQLite, Docker, Tailwind CSS, Shadcn UI, Web3.js, Ethers.js, Hardhat, Truffle, Ganache, Pinata IPFS |
| Dhruv Punj | AI and data science for a media company's web portal: recommendation engine and virtual assistant (Admix Media, AI and Data Science Intern, remote, Feb to Jun 2025) | Python, SQL, JavaScript, PHP, C++ | TensorFlow, Keras, NumPy, Pandas, Matplotlib, Streamlit; Agile |
