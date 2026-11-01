# After refactor #1: MembershipService (2026-10-31)

Captured from commit `5f1127a` on `Dinesh`, right after extracting the daily-checks
batch job out of `MembershipService` into `MembershipDailyChecksService`. Same
PMD/CK steps as `before-refactor/`.

| Metric | MembershipService before | MembershipService after | MembershipDailyChecksService (new) |
| --- | --- | --- | --- |
| CBO | 28 | 26 | 10 |
| WMC | 36 | 25 | 12 |
| RFC | 55 | 49 | 17 |
| LOC | 134 | 96 | 51 |
| PMD violations | 5 (ExcessiveImports, CouplingBetweenObjects, TooManyMethods, UseLocaleWithCaseConversions x2) | 1 (TooManyMethods) | 0 |

Full raw reports: `pmd.xml`, `ck-class.csv` in this folder. `BillingService` (the
other worst class picked in `before-refactor/README.md`) is still untouched here —
that's task #10, its own before/after pair.
