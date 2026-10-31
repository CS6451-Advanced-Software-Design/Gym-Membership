# After refactor #2: BillingService (2026-10-31)

Captured from commit `ca39927` on `Dinesh`, right after extracting `account()` and
its two private helpers out of `BillingService` into `BillingAccountFactory`.
Same PMD/CK steps as `before-refactor/`.

| Metric | BillingService before | BillingService after | BillingAccountFactory (new) |
| --- | --- | --- | --- |
| CBO | 25 | 22 | 9 |
| WMC | 54 | 44 | 11 |
| RFC | 76 | 64 | 18 |
| LOC | 192 | 171 | 31 |
| PMD violations | 3 (GodClass, TooManyMethods, ExcessiveImports) | 1 (TooManyMethods) | 0 |

The headline result: PMD no longer flags `BillingService` as a `GodClass` at all.

Full raw reports: `pmd.xml`, `ck-class.csv` in this folder. Combined with
`after-refactor-1/` (`MembershipService`), both worst classes picked in
`before-refactor/README.md` have now been refactored.
