# SonarQube Cloud snapshot (2026-11-03)

First real scan after wiring `sonar-maven-plugin` into `ci.yml`'s `metrics` job. No before-snapshot
exists: Sonar wasn't set up until after both §9 refactorings (`BillingAccountFactory`,
`MembershipDailyChecksService`) were already merged, so there's nothing honest to compare it against
from before that point - see `report/sections/09-added-value.md` for why.

Project: `CS6451-Advanced-Software-Design_Gym-Membership`, org `cs6451-advanced-software-design`.

| Metric | Value |
| --- | --- |
| Quality gate | Passed |
| Coverage | 96.3% |
| Duplication | 0.0% |
| Bugs | 0 |
| Vulnerabilities | 0 |
| Code smells | 0 |
| ncloc | 3,525 |
| Security rating | A |
| Security review rating | A |
| Maintainability rating | A |
| Reliability rating | A |

## How Reliability got to A

The scan first found 7 High severity issues. 5 were `MembershipState.java:14-18` (rule `S2390` - the
abstract state class holding static references to all 5 of its own subclasses); real, and fixed by
extracting `MembershipStateFactory`, mirroring `PlanFactory`'s existing shape. Commit
`88fe500`/`dd15c70`.

The other 2 were both `OffPeakPlan.java:22` (rule `S8696`, "use `.equals()` not `==`/`!=`"), flagging
`day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY`. Checked properly before touching anything:
`DayOfWeek` is an enum, so `!=` on its constants is safe and idiomatic (Effective Java's own
recommendation) - this is a false positive from `java.time`'s boilerplate "value-based class" Javadoc
being copied onto its enums too. Changing the code to `.equals()` would have made it read worse for a
cosmetic rating change, so instead both were resolved as `False Positive` directly in SonarCloud
(2026-11-03), which is what that status exists for once an issue is actually verified, not just
dismissed. Confirmed via the public API afterwards: `bugs: 0`, `reliability_rating: 1.0`, both issues
show `status: RESOLVED`, `resolution: FALSE-POSITIVE`.

Screenshots: `report/screenshots/sonarqube-overview.png`, `sonarqube-security.png`, `sonarqube-security-hotspot.png`, `sonarqube-reliability.png`, `sonarqube-maintainability.png`.
