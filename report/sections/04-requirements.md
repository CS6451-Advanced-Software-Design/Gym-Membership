# Requirements {#requirements}

<!--
Owner: Dhruv.
The use case diagram, UC1 and UC2 in the Week 2 template (with extensions, variations and open issues), two quality
attributes as six-part scenarios (one must be extensibility) with named Bass et al. (2019) tactics and the class that
applies each one, and two GUI prototypes.
The use case drafts in report/requirements/use-cases came from GenAI (prompt #2): rewrite them in your own words.
-->

## Use Cases

![Use case diagram](../requirements/use-case-diagram.png)

*Introduction to the actors and use cases, to be written by Dhruv.*

## UC1: Sign Up for Membership

*To be written by Dhruv.*

## UC2: Book Class

*To be written by Dhruv.*

## Quality Attributes

We wrote both quality attributes as six-part scenarios (Bass et al. 2019), so each one says exactly what has to happen and how we can tell it worked, and named the tactics that support it with the class that applies each one.

### QA1: Extensibility

The club will want new plans, such as a weekend-only plan, and each plan has its own price, trial, hours and eligibility rule. We want a new plan to be added without editing the code that signs members up, bills them or books classes.

| Part | QA1: adding a plan |
| --- | --- |
| Source | A developer on the team |
| Stimulus | The manager asks for a new Weekend plan: €25 a month, weekends only |
| Artefact | The plans in `domain` and `PlanFactory` |
| Environment | Design time, between releases |
| Response | The developer adds one `Plan` subclass with the plan's rules, one value in `PlanType` with its price, and one line in `PlanFactory`, plus their tests |
| Response measure | No change to `MembershipService`, Billing, Booking or any controller; the new plan appears in `GET /plans`; all existing tests still pass; done in under a day |

| Tactic (Bass et al. 2019, modifiability) | Where it is applied |
| --- | --- |
| Increase semantic coherence | Each plan's rules live in its own class: `StudentPlan` checks the student ID, `OffPeakPlan` the hours, `AnnualPlan` the end date and the missing trial |
| Abstract common services | `Plan` holds what every plan shares (its price, trial and end date), so a new plan only overrides what is different |
| Use an intermediary | `PlanFactory` is the only class that knows which class goes with which `PlanType`; `MembershipService` asks it for a plan and never names a plan class |
| Encapsulate | Check-in and booking ask the membership (`canCheckIn`, `canBook`), which asks its own `Plan`, so they don't change when a plan is added |
| Defer binding | The plan is chosen at run time from the request, through the factory's map, and its rules run through polymorphism instead of `if` or `switch` statements on the plan type |

The same tactics make the other rules extensible. A new discount is one more `DiscountPolicy` class added to `BestDiscount` (Strategy), and a new membership state is one more `MembershipState` subclass (State). `PlanFactoryTest` fails if any `PlanType` has no plan class, so a half-added plan can't be released.

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
