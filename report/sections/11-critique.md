# Critique {#critique}

<!--
Owner: Chirag. Maximum 1 page.
Honest: where the code and the diagrams differ, what the metrics say (quote the final SonarQube Cloud numbers), the
usefulness of the diagrams and of UML, and how we would evaluate and document the architecture (e.g. ATAM).
-->

## Code Against Design

The Hexagonal architecture we aimed for held: every dependency points inwards to the `domain` package.

We took a small shortcut in persisting data: instead of mapping the domain objects to separate persistence models, we saved them directly as JSON. As a consequence, a change to the domain, for example a new required parameter in the `Membership` constructor, would stop the data saved so far from loading.

Our architecture diagram also left out things we only found we needed during implementation. For example, it has no place for `BillingConfiguration` and `InterceptorConfiguration`, the classes that plug the discount strategies into Billing and the logging interceptor into every use case.

## Metrics

JaCoCo reports 97.0% coverage, but this overstates how well the code is tested, because it counts the lines a test ran, not the results it checked. PMD and CK both point at the same weak spot: `BillingService` does too much, implementing five use cases in one class, which Hexagonal does not prevent. The §9 refactoring removed PMD's `GodClass` warning, but it is still the most complex class. Most of PMD's other findings are style rather than design, for example `AvoidFieldNameMatchingMethodName`, which every Java record triggers.

To improve this, we would split `BillingService` into one class per use case, along the lines its `port.in` interfaces already draw.

| Tool | Result | Where it points |
| --- | --- | --- |
| JaCoCo | 97.0% line coverage (1029 of 1061) | Unit 79.2%, end-to-end 64.9%, integration 39.1% |
| PMD | 47 violations | TooManyMethods: BillingService, BookingService, MembershipService, MembershipState |
| CK | Highest CBO 29, highest WMC 44 | MembershipController (CBO), BillingService (WMC 44, RFC 64) |
| SonarQube Cloud | Gate passed: 2 bugs, 0 code smells, 0% duplication | OffPeakPlan (`DayOfWeek` compared with `!=`) |

Table: Code metrics on main (CI, 1 Nov)

## Diagrams and UML

Recovering the blueprints from the code was worth it, because it found real differences between our design and the implementation (§10). The tool was not fully reliable, though: Visual Paradigm silently dropped all 19 of our `record` types, every DTO and some core domain classes, and we only caught it by checking against the code and reversing the compiled classes instead. Recovery also cannot bring back design intent, such as which package is a «driving adapter», so a recovered diagram is best read next to the design one.

## Evaluating the Architecture

To evaluate our architecture, we would use the Architecture Tradeoff Analysis Method (ATAM), checking whether it actually delivers the quality attribute scenarios we set in §4:

1. QA1 Extensibility: adding a Weekend plan without changing any service.
2. QA2 Testability: testing time-based rules, such as a trial ending, against a test clock instead of the system clock.

ATAM would most likely flag the risk we noted above: a change to the domain would break the data already saved as JSON.
