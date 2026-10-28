# Analysis Sketches {#analysis-sketches}

<!--
Owner: Dhruv.
Candidate objects and how we found them, the class diagram (interfaces with pre- and postconditions), the sequence
diagram, the Membership state chart (the object in the sequence diagram), and the ER diagram. Put each photo of our
rough work next to the clean diagram it led to, with what changed.
Evidence: report/analysis/README.md, report/sketches/README.md.
-->

## Candidate Objects

*To be written by Dhruv.*

## Class Diagram

![Class diagram](../analysis/class-diagram.png)

The class diagram keeps the candidates from the table above, with only the attributes and operations the business rules need. It shows generalisation (`Plan` and its four plans, `MembershipState` and its five states), realisation (the three discounts implement `DiscountPolicy`), composition (`Membership` owns its `MembershipState` and its `Freeze` records, and a `GymClass` owns its `Waitlist`), aggregation (`Invoice` collects `Charge`s), associations with multiplicities, dependencies (`PlanFactory` creates `Plan`s), visibility and enumerations. The interfaces carry their pre- and postconditions as notes: a `DiscountPolicy` never gives back more than the amount it was given, and the `PaymentGateway` charges a positive amount exactly once or not at all. `noShowsLast30Days` is a derived attribute, counted from the `NO_SHOW` bookings, so no attribute or operation appears twice. The two patterns the rules called for are already visible: the `MembershipState` hierarchy (State) and `PlanFactory` (Factory). The `Membership`, `MembershipState`, `Plan` and `PlanFactory` boxes were redrawn after the Membership code was written, so they match it.

## Sequence Diagram: Book Class

![UC2 Book Class sequence diagram](../analysis/sequence-book-class.png)

## State Chart: Membership

![Membership state chart](../analysis/membership-statechart.png)

## Entity Relationship Diagram

![ER diagram](../analysis/er-diagram.png)

## Rough Work

::: {.grid columns=2}
![The hexagon, first draft (Chirag)](../sketches/2026-10-25-hexagon-chirag.jpg){width=100%}

![Worked invoice (Chirag)](../sketches/2026-10-25-worked-invoice-chirag.jpg){width=100%}

![Discount Strategy, with the if/else chain it replaced (Chirag)](../sketches/2026-10-25-discount-strategy-chirag.jpg){width=100%}

![Membership life, a year of Ann's membership and its five states (Dhruv)](../sketches/2026-10-26-membership-life-dhruv.jpg){width=100%}

![State pattern, Membership asking its state whether it may freeze (Dhruv)](../sketches/2026-10-26-state-pattern-dhruv.jpg){width=100%}

![Daily checks (UC13) (Dhruv)](../sketches/2026-10-26-daily-checks-dhruv.jpg){width=100%}

![Plan Factory (Dhruv)](../sketches/2026-10-26-plan-factory-dhruv.jpg){width=100%}

![Booking and check-in, how other features ask Membership (Dhruv)](../sketches/2026-10-26-booking-checkin-dhruv.jpg){width=100%}

![Sign-up (UC1), with a worked first payment (Dhruv)](../sketches/2026-10-26-sign-up-dhruv.jpg){width=80%}
:::
