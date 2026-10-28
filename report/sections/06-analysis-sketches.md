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

## Sequence Diagram: Book Class

![UC2 Book Class sequence diagram](../analysis/sequence-book-class.png)

## State Chart: Membership

![Membership state chart](../analysis/membership-statechart.png)

## Entity Relationship Diagram

![ER diagram](../analysis/er-diagram.png)

## Rough Work

These are photos of our rough work, drawn by hand on paper or on an iPad, with crossings-out left in. Chirag's three sketches worked out the hexagon, the discount Strategy and an invoice by hand. Mine follow the Membership feature: one member's year through the five states, the State pattern answering whether a membership may freeze, the Plan Factory, sign-up with a worked first payment, the daily checks, and how booking and check-in ask a membership what it allows. The layout of each was copied from a GenAI reference page and redrawn by hand. What changed between each sketch and the final diagram or code is recorded next to the photo in `report/sketches/README.md`; for example, the sign-up sketch writes prices in pounds instead of euros, and the State sketch shows only a yes or no where the code also checks the 1 to 3 month limit.

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
