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
