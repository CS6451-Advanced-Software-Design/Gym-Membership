# Roles

**Status: proposal, to be agreed by the team.**

The spec (Table 2) lists 10 roles. Everyone must contribute **equally to code and to the report**, so each member takes one report-facing role and one technical role, **owns one package end to end** (controller → service → domain → repository, plus its tests), and writes their own reflection.

## Role split

| Member | Report-facing role | Technical role | Owns package |
| --- | --- | --- | --- |
| A | Project Manager (§3 plan, diary, transparency tables §7) | Tester (test strategy, JUnit/xUnit setup, §8 tests) | `booking` |
| B | Business Analyst / Requirements Engineer (§4) | Systems Analyst (§6 analysis sketches) | `membership` |
| C | Architect (§5 architecture, tech pipeline) | Designer (§10 recovered blueprints in the UML workbench) | `billing` |
| D | Documentation Manager (assembles the report, presentation checks) | Technical Lead + DevOps (repo, CI/CD, metrics → refactoring §9) | `member` |

Everyone: Programmer (spec Table 2, row 8), reviews other members' PRs, and writes their own reflection (§12).

Chirag's platform and DevOps background suits role D. The other allocations are open, so match them to experience and interest.

## Proposed packages (vertical slices)

| Package | Scope | Likely design pattern(s) |
| --- | --- | --- |
| `membership` | Plans, sign-up, freeze and cancel, membership lifecycle | State (membership lifecycle), Factory (plan creation) |
| `billing` | Monthly billing run, discounts, penalties, referral credits, invoices | Strategy (pricing and discount policies) |
| `booking` | Class schedule, bookings, capacity, waitlist, no-shows | Command (book and cancel with undo), Observer (waitlist promotion) |
| `member` | Member profiles, check-in, referrals, notifications | Observer (notifications) |

Shared (built together in Iteration 1): the MVC/REST controller base, the repository interface with a file-backed implementation, and the domain exceptions.
