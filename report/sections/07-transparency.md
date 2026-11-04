# Transparency {#transparency}

<!--
Owner: Bharat.
Every number comes from a tool, named next to the table: cloc for lines of code, git blame or a script for authorship.
Tables: classes per package with totals, every class with its author and LOC, LOC per member, and who did what for
patterns, testing and CI/CD. Then the diary, which comes from report/diary.md.
-->

## Classes per Package

| Package | Types | Lines of code |
| --- | --- | --- |
| `gym` | 4 | 219 |
| `gym.adapter.logging` | 3 | 51 |
| `gym.adapter.notification` | 3 | 31 |
| `gym.adapter.payment` | 3 | 46 |
| `gym.adapter.persistence` | 10 | 157 |
| `gym.controller` | 8 | 382 |
| `gym.domain` | 40 | 918 |
| `gym.dto` | 17 | 192 |
| `gym.interceptor` | 5 | 147 |
| `gym.port.in` | 20 | 132 |
| `gym.port.out` | 11 | 52 |
| `gym.service` | 14 | 1022 |
| **Main total** | **138** | **3349** |
| Tests (`code/src/test`) | 44 | 3831 |
| **All** | **182** | **7180** |

Table: Types and lines of code per package (cloc, commit 41bed5f)

## Classes, Authors and Lines of Code

| Package | Chirag | Bharat | DineshReddy | Dhruv |
| --- | --- | --- | --- | --- |
| gym | 4 / 219 | – | – | – |
| adapter | 8 / 173 | 2 / 22 | 1 / 11 | 4 / 75 |
| controller | 3 / 84 | 1 / 103 | 1 / 54 | 2 / 140 |
| domain | 15 / 182 | 6 / 170 | 2 / 27 | 16 / 538 |
| dto | 3 / 41 | 5 / 44 | 2 / 23 | 6 / 83 |
| interceptor | 4 / 146 | – | – | – |
| port | 9 / 55 | 8 / 49 | 4 / 24 | 8 / 54 |
| service | 1 / 227 | 8 / 445 | 3 / 202 | 1 / 147 |
| **Main code** | **47 / 1127** | **30 / 833** | **13 / 341** | **37 / 1037** |
| tests | 18 / 1057 | 10 / 1782 | 4 / 223 | 12 / 769 |

Table: Classes / lines of code per member and package (`git blame`, commit 41bed5f). Full list in [Appendix A](#appendix-classes)

## Lines of Code per Member

| Member | Main code lines | Test code lines | Total | Share |
| --- | --- | --- | --- | --- |
| Chirag | 1611 | 1111 | 2722 | 30.6% |
| Bharat | 1339 | 1857 | 3196 | 36.0% |
| DineshReddy | 466 | 251 | 717 | 8.1% |
| Dhruv | 1449 | 801 | 2250 | 25.3% |
| **All** | **4865** | **4020** | **8885** | **100%** |

Table: Lines per member (`git blame`, commit 41bed5f, comments included)

## Responsibilities

| Area | Who |
| --- | --- |
| Design patterns | Chirag worked on Strategy (discount) Interceptor, Hexagonal and MVC Skeleton and the ArchitectureTest. Bharat worked on Command Pattern (for undo functionality) and used Observer Pattern for waitlist notification. Dhruv worked on State (membership lifecycle part) and Factory (PlanFactory Part) and Observer (port, publisher and listener). DineshReddy used Observer for check-in notifications |
| Testing | Each member tested their own features. Chirag split the tests into 3 kinds (unit, integration, end-to-end), each as its own CI check. Bharat did the most test code, 1857 lines that includes - BookingServiceTest, BookingCommandServiceTest, BookingPersistenceIT and BookingE2E |
| CI/CD | DineshReddy created the CI pipeline and the metrics comment that posts PMD/CK numbers on every PR, and also set up the SonarQube Cloud scan, the Docker build on every PR, the live-app deploy check, and fixed several CI reliability issues (such as missing timeouts and unchecked download failures). The Railway deployment and the report PDF build were done by Chirag |

## Diary

::: {include="../diary.md" merge="true" widths="0.08 0.46 0.23 0.23" size="8pt"}
:::
