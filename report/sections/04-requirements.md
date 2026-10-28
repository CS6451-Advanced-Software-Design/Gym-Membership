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

### QA2: Testability

Most of the business rules depend on dates: a trial ends after 7 days, a membership lapses after two failed payments, and is cancelled after 60 days in Lapsed. We want every one of these rules to be tested quickly and repeatably, without waiting for real days to pass, charging real cards or writing real files.

| Part | QA2: testing a time-based rule |
| --- | --- |
| Source | A developer, or the CI pipeline on every pull request |
| Stimulus | A business rule is added or changed, for example the 60-day rule for Lapsed memberships (BR-M6) |
| Artefact | The services and domain classes behind the use cases |
| Environment | Development and CI, with no network, database or payment provider |
| Response | Unit tests move a test clock forward day by day, use in-memory repositories and a fake payment gateway, and check the outcome of the rule |
| Response measure | Every business rule built so far has a unit test; the 108 unit tests run with no files, network or real clock; a failing test blocks the merge |

| Tactic (Bass et al. 2019, testability) | Where it is applied |
| --- | --- |
| Limit nondeterminism | The services take a `java.time.Clock` instead of reading the system time, and tests pass a `TestClock` they can move forward, so "60 days later" takes no time and gives the same result every run |
| Abstract data sources | Services only see the repository ports in `port.out`, so tests swap the JSON files for `InMemoryRepository` |
| Specialised interfaces | `PaymentGateway` and `NotificationSender` are ports, so tests use a fake gateway that declines chosen cards and check the notifications sent |
| Limit structural complexity | `ArchitectureTest` (ArchUnit) keeps the dependencies pointing inwards, so a domain class can be tested on its own with only the JDK |
| Sandbox | The integration and end-to-end tests run the whole app against a temporary data folder, apart from the real data |

This is why the State pattern tests could follow every arrow of the state chart (`MembershipStateTest`) and the daily checks could be tested a day at a time (`MembershipServiceTest`, `MembershipE2E`).

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
