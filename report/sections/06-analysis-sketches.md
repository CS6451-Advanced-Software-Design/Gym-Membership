# Analysis Sketches {#analysis-sketches}

<!--
Owner: Dhruv.
Candidate objects and how we found them, the class diagram (interfaces with pre- and postconditions), the sequence
diagram, the Membership state chart (the object in the sequence diagram), and the ER diagram. Put each photo of our
rough work next to the clean diagram it led to, with what changed.
Evidence: report/analysis/README.md, report/sketches/README.md.
-->

## Candidate Objects

I found the candidate classes by noun identification, the method from Lecture B. I went through the UC1 and UC2 descriptions and the business rules, listed every noun, and kept a noun as a class only if it has its own state, behaviour and identity. A noun that is only a value of something else became an attribute or an enumeration, an outside system became a port, and a noun that only names a person's role or a screen was dropped.

| Noun (where found) | Decision | Why |
| --- | --- | --- |
| Member, prospective member (UC1, UC2) | Class `Member` | A prospective member is the same person before sign-up |
| Membership (UC1, BR-M2 to M5) | Class `Membership` | It has a lifecycle, so it gets the state chart |
| Trial, Active, Frozen, Lapsed, Cancelled (BR-M2 to M4) | Subclasses of `MembershipState` | What a membership may do depends on its state (State pattern) |
| Monthly, Annual, Student, Off-Peak plan (BR-M1) | `Plan` with four subclasses, made by `PlanFactory` | Each plan has its own price, trial, hours and eligibility rule |
| Freeze (BR-M3) | Class `Freeze` | Needed to check 1 to 3 months, at most once every 12 months |
| Class on the timetable (UC2) | Class `GymClass` | `Class` clashes with Java's `java.lang.Class` |
| Booking, waitlist (UC2, BR-B1 to B6) | Classes `Booking` and `Waitlist` | A booking links a member to a class and has its own status; the waitlist keeps an order |
| Invoice, fee, penalty, credit (BR-P1) | `Invoice`, and `Charge` with a `ChargeType` | One `Charge` class with many kinds, instead of a class per fee |
| Corporate, family, loyalty discount (BR-P2) | `DiscountPolicy` interface | Interchangeable rules where the best one wins (Strategy) |
| Payment gateway, notification (UC1) | Ports `PaymentGateway` and `NotificationSender` | Outside systems, so they sit behind an interface |
| Instructor (actors) | Class `Instructor` | Teaches classes and records attendance |
| Timetable (UC2) | Dropped | Only a search over `GymClass` by date |
| Payment (UC1) | Dropped | Only an outcome, paid or declined; `Membership` keeps `failedPayments` |
| No-show, late cancellation (BR-B3, BR-B4) | Values, not classes | They are `BookingStatus` and `ChargeType` values |
| Student ID, referral code, employer | Attributes of `Member` | No behaviour of their own |
| Front desk staff, manager | Dropped | Actors with no business state to store |

The biggest change from the first list was the membership states. They started as one status field on `Membership`, but freezing, booking and checking in each needed a different answer in every state, so each state became its own class. Payment went the other way: it looked like an entity, but nothing about a payment is kept except whether it went through.

## Class Diagram

![Class diagram](../analysis/class-diagram.png)

The class diagram keeps the candidates from the table above, with only the attributes and operations the business rules need. It shows generalisation (`Plan` and its four plans, `MembershipState` and its five states), realisation (the three discounts implement `DiscountPolicy`), composition (`Membership` owns its `MembershipState` and its `Freeze` records, and a `GymClass` owns its `Waitlist`), aggregation (`Invoice` collects `Charge`s), associations with multiplicities, dependencies (`PlanFactory` creates `Plan`s), visibility and enumerations. The interfaces carry their pre- and postconditions as notes: a `DiscountPolicy` never gives back more than the amount it was given, and the `PaymentGateway` charges a positive amount exactly once or not at all. `noShowsLast30Days` is a derived attribute, counted from the `NO_SHOW` bookings, so no attribute or operation appears twice. The two patterns the rules called for are already visible: the `MembershipState` hierarchy (State) and `PlanFactory` (Factory). The `Membership`, `MembershipState`, `Plan` and `PlanFactory` boxes were redrawn after the Membership code was written, so they match it.

## Sequence Diagram: Book Class

![UC2 Book Class sequence diagram](../analysis/sequence-book-class.png)

The sequence diagram follows UC2 Book Class through the hexagon: the request comes in through `BookingController`, passes the logging interceptor, and reaches `BookingService` behind the `BookClassUseCase` port. The service loads the class, the member and the membership through their repositories and checks each booking rule in turn: a member with 3 no-shows in 30 days is suspended (BR-B4), and nobody may hold more than 5 future bookings (BR-B5). It then asks the `Membership` whether it may book. The membership asks its current `MembershipState` and its `Plan`, so the service never checks the status itself. If the class has space the member is booked (201); if it is full they join the `Waitlist` and are told their position. `Membership` is in this diagram on purpose, because it is the object the state chart describes. Booking is not implemented yet, so this diagram is the design the Booking feature will follow.

## State Chart: Membership

![Membership state chart](../analysis/membership-statechart.png)

The state chart is for `Membership`, the object from the sequence diagram and the one the State pattern implements. Every transition is written as `event [guard] / action`. A new membership starts in Trial, except an Annual one, which is paid for the year and starts Active. The trial ends after 7 days: Active if the first charge was paid, Lapsed if it failed (BR-M2). An Active membership can be frozen for 1 to 3 months, at most once in 12 months (BR-M3), and a second failed payment in a row makes it Lapsed (BR-M4). Paying the arrears brings it back to Active, and 60 days in Lapsed cancels it (BR-M6). Cancelling an Annual plan within 6 months adds the €50 early exit fee. The Clock drives `endTrial()`, the unfreeze at the end of a freeze and `expire()` through UC13. In the code each state is a class (`TrialState`, `ActiveState`, `FrozenState`, `LapsedState`, `CancelledState`), each event is a method on `Membership` that it hands to its current state, and `MembershipStateTest` follows every arrow of this chart.

## Entity Relationship Diagram

![ER diagram](../analysis/er-diagram.png)

The ER diagram has fewer boxes than the class diagram because only stored data becomes an entity. Each stored class has one entity with the same attributes and the same cardinality, shown in crow's foot notation: a member holds many memberships over time, a membership has many freezes, invoices and charges, and a charge belongs to at most one invoice until it is billed. The referral is a reflexive relationship on `MEMBER`. The four plans share one `PLAN` table told apart by its type, and the five states are a single `state` column on `MEMBERSHIP`, since a state has behaviour but no data of its own. The waitlist becomes one `WAITLIST_ENTRY` row per member, ordered by position. Things that aren't stored have no entity: the discount strategies, `PlanFactory`, and the payment and notification ports. In the prototype these entities are saved as JSON files through the repository ports, not in a database.

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
