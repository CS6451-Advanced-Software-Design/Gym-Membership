# Before-refactoring metrics (2026-10-31)

Captured from commit `0a19ad8` on `Dinesh`, before any §9 refactoring, by running the same
PMD (`./mvnw pmd:pmd`) and CK (`ck-0.7.0`) steps CI runs on every PR.

## Worst classes (PMD + CK combined)

| Class | CBO | WMC | RFC | LOC | PMD violations |
| --- | --- | --- | --- | --- | --- |
| `gym.service.BillingService` | 25 | 54 | 76 | 192 | GodClass, TooManyMethods, ExcessiveImports |
| `gym.service.MembershipService` | 28 | 36 | 55 | 134 | ExcessiveImports, CouplingBetweenObjects, TooManyMethods, UseLocaleWithCaseConversions (x2) |
| `gym.controller.MembershipController` | 29 | 9 | 25 | 44 | ExcessiveImports, AvoidDuplicateLiterals (x3), AvoidFieldNameMatchingMethodName |

Full raw reports: `pmd.xml`, `ck-class.csv` in this folder.

Picked `BillingService` and `MembershipService` for refactoring (task #9/#10) — highest WMC/RFC,
and the only class PMD calls out as an actual GodClass.
