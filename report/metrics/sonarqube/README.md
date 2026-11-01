# SonarQube Cloud snapshot (2026-11-01)

First real scan after wiring `sonar-maven-plugin` into `ci.yml`'s `metrics` job. No before-snapshot
exists: Sonar wasn't set up until after both §9 refactorings (`BillingAccountFactory`,
`MembershipDailyChecksService`) were already merged, so there's nothing honest to compare it against
from before that point - see `report/sections/09-added-value.md` for why.

Project: `CS6451-Advanced-Software-Design_Gym-Membership`, org `cs6451-advanced-software-design`.

| Metric | Value |
| --- | --- |
| Quality gate | Passed |
| Coverage | 95.8% |
| Duplication | 0.0% |
| Bugs | 2 |
| Vulnerabilities | 0 |
| Code smells | 0 |
| ncloc | 3,091 |
| Complexity / Cognitive complexity | 539 / 208 |
| Security rating | A |
| Security review rating | A |
| Maintainability rating | A |
| Reliability rating | D |

## The 2 remaining Reliability issues

Both are `OffPeakPlan.java:22` (rule `S8696`, "use `.equals()` not `==`/`!=`"), flagging
`day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY`. `DayOfWeek` is an enum, so `!=` on its
constants is safe and idiomatic (Effective Java's own recommendation) - this is a false positive from
`java.time`'s boilerplate "value-based class" Javadoc being copied onto its enums too. Left as `==` on
purpose; not applied, since it would make the code worse for a cosmetic rating change.

The other 5 issues from the first scan (`MembershipState.java:14-18`, rule `S2390` - the abstract state
class holding static references to all 5 of its own subclasses) were real and are fixed: extracted into
`MembershipStateFactory`, mirroring `PlanFactory`'s existing shape. See `dinesh-todo.md` and commit
`88fe500`/`dd15c70`.

Screenshots: `report/screenshots/sonarqube-overview.png`, `sonarqube-security.png`, `sonarqube-security-hotspot.png`, `sonarqube-reliability.png`, `sonarqube-maintainability.png`.
