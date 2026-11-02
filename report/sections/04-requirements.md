# Requirements {#requirements}

<!--
Owner: Dhruv.
The use case diagram, UC1 and UC2 in the Week 2 template (with extensions, variations and open issues), two quality
attributes as six-part scenarios (one must be extensibility) with named Bass et al. (2019) tactics and the class that
applies each one, and two GUI prototypes.
The use case drafts in report/requirements/use-cases came from GenAI (prompt #2): rewrite them in your own words.
-->

## Use Cases

![Use case diagram](../requirements/use-case-diagram.png){height=13cm}

The diagram shows five human actors: Prospective Member, who can only sign up, and Member, which specialises it so a past member can join again, then Front Desk Staff, Instructor and Manager. It also has two non-human ones: the Clock, a time actor that starts monthly billing (UC7) and the daily checks (UC13), and the Payment Gateway that charges cards. Shared steps are pulled out with «include» (Calculate Charges, Verify Membership Status), and optional ones are «extend» with their condition, such as Join Waitlist when a class is full.

UC1 Sign Up and UC2 Book Class are described in full because between them they touch most of the business rules. The prototype implements UC1, UC4–UC7, UC11, UC13 and UC14; Booking (UC2, UC3, UC8) is being built and the Manager's use cases are left for a later release.

## UC1: Sign Up for Membership

| USE CASE 1 | Sign Up for Membership |
| --- | --- |
| Goal in Context | A person joins on one of the four plans and pays for the rest of the first month (the whole year on Annual) |
| Scope & Level | Gym membership system, user goal |
| Preconditions | The plans and prices are set up (BR-M1); the person holds no membership that isn't cancelled |
| Success End Condition | A membership in Trial (Active on Annual), a first invoice and a welcome message; a valid referrer gets their credit |
| Failed End Condition | Nothing is saved and nothing is charged |
| Primary, Secondary Actors | Prospective Member; Payment Gateway |
| Trigger | The person chooses Join |

| DESCRIPTION | Step | Action |
| --- | --- | --- |
| | 1 | The system lists the four plans with price, trial and access hours (BR-M1) |
| | 2 | The person picks a plan and enters name and email, plus student ID, employer, family ID or referral code if they have one |
| | 3 | The system checks eligibility and the referral code, and reuses the member record if the email belongs to a past member |
| | 4 | The system starts the membership: a 7-day Trial with 2 bookings, or Active on Annual (BR-M2) |
| | 5 | The system bills the first period, pro-rated (BR-P4) minus the best discount (BR-P2), «include» Calculate Charges, and charges the card |
| | 6 | The payment goes through; a referrer gets €20, «extend» Apply Referral Credit (BR-P3), and a welcome message is sent |

| EXTENSIONS | Step | Branching Action |
| --- | --- | --- |
| | 3a | Student plan without a valid 8-digit ID, an unknown referral code, or a past member who still holds a live membership: the system refuses with the reason and nothing is saved |
| | 6a | The card is declined: the membership is still created with the invoice unpaid and no referral credit; when the trial ends it becomes Lapsed until the member pays (UC14) |

| VARIATIONS | Step | Branching Action |
| --- | --- | --- |
| | 2 | Monthly €45, Student €30, Off-Peak €30, or Annual €450 paid upfront with no trial |
| | 5 | Corporate 15% or Family 10%; Loyalty 5% needs 24 months, so never applies here |

| RELATED INFORMATION | Sign Up for Membership |
| --- | --- |
| Priority / Performance / Frequency | Top; under 2 seconds including payment; about 20 a day |
| Channel to actors | REST `POST /memberships` (Postman, Swagger UI); the Join screen prototype |
| OPEN ISSUES | Should a declined Annual payment be refused instead of starting Active? Family memberships with one payer are out of scope (24 Oct) |
| Due Date / Superordinates | Release 0.2.0 (done) / Manage memberships |
| Subordinates | Calculate Charges, Apply Referral Credit |

## UC2: Book Class

| USE CASE 2 | Book Class |
| --- | --- |
| Goal in Context | A member reserves a place in a class, or a waitlist place when it is full |
| Scope & Level | Gym membership system (Booking), user goal |
| Preconditions | The member holds a membership; the class is on the timetable and hasn't started |
| Success End Condition | A confirmed booking or waitlist position; any premium surcharge is recorded and a Trial booking is counted |
| Failed End Condition | Nothing is booked or charged, and the member is told why |
| Primary, Secondary Actors | Member; Front Desk Staff when booking for a member |
| Trigger | The member picks a class and chooses Book |

| DESCRIPTION | Step | Action |
| --- | --- | --- |
| | 1 | The member picks a class from the timetable |
| | 2 | The system checks the member isn't suspended (3 no-shows in 30 days, BR-B4) and holds fewer than 5 future bookings (BR-B5) |
| | 3 | The system asks the membership whether it may book, «include» Verify Membership Status: its state (Trial under 2 bookings, Active) and its plan (Off-Peak only off-peak classes) |
| | 4 | The system checks the class has a free place (BR-B1) and shows any €8 premium surcharge (BR-B6) |
| | 5 | The member confirms; the system creates the booking, records the surcharge and confirms |

| EXTENSIONS | Step | Branching Action |
| --- | --- | --- |
| | 2a, 3a | Suspended, at 5 bookings, or the state or plan says no (e.g. Frozen, or Off-Peak choosing an evening class): the system refuses and gives the reason |
| | 4a | The class is full: «extend» Join Waitlist; the member is told their position and is booked automatically when a place frees up (BR-B2, UC3) |

| VARIATIONS | Step | Branching Action |
| --- | --- | --- |
| | 4 | Annual members and ordinary classes pay no surcharge; Front Desk Staff may book for a member |

| RELATED INFORMATION | Book Class |
| --- | --- |
| Priority / Performance / Frequency | Top; under 1 second, and only one of two members racing for the last place gets it; about 500 a day |
| Channel to actors | REST `POST /bookings`; the Book a Class screen prototype |
| OPEN ISSUES | May a member book two overlapping classes? Should the waitlist still promote within 2 hours of the start? |
| Due Date / Superordinates | Release 1.0 / Manage bookings |
| Subordinates | Verify Membership Status, Join Waitlist |

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
| Response measure | Every business rule built so far has a unit test; the 204 unit tests run with no files, network or real clock; a failing test blocks the merge |

| Tactic (Bass et al. 2019, testability) | Where it is applied |
| --- | --- |
| Limit nondeterminism | The services take a `java.time.Clock` instead of reading the system time, and tests pass a `TestClock` they can move forward, so "60 days later" takes no time and gives the same result every run |
| Abstract data sources | Services only see the repository ports in `port.out`, so tests swap the JSON files for `InMemoryRepository` |
| Specialised interfaces | `PaymentGateway` and `NotificationSender` are ports, so tests use a fake gateway that declines chosen cards and check the notifications sent |
| Limit structural complexity | `ArchitectureTest` (ArchUnit) keeps the dependencies pointing inwards, so a domain class can be tested on its own with only the JDK |
| Sandbox | The integration and end-to-end tests run the whole app against a temporary data folder, apart from the real data |

This is why the State pattern tests could follow every arrow of the state chart (`MembershipStateTest`) and the daily checks could be tested a day at a time (`MembershipServiceTest`, `MembershipE2E`).

## GUI Prototypes

The two prototypes are for the two key use cases. They are wireframes of screens the prototype doesn't have yet: today the same requests are made through Postman or the Swagger UI.

![Join screen for UC1 Sign Up: choose a plan, enter details, see the first payment](../requirements/gui-join.png){width=100%}

The Join screen follows UC1 from top to bottom. The four plans show their price and the rule that sets them apart (BR-M1), and the form asks only for what the rules need: the email to recognise a past member, the employer and family ID for discounts, the referral code, and the student ID for the Student plan. The first payment is worked out on the screen before the person pays, here Bob joining a Monthly plan on 25 October with 7 of 31 days left, €45 × 7/31 = €10.16 (BR-P4), with the trial it starts (BR-M2). Refusals from extensions 5a and 5b appear under the form before anything is charged.

![Book a Class screen for UC2: the week's classes with places left, booking and the waitlist](../requirements/gui-book-class.png){width=100%}

The Book a Class screen follows UC2. The bar at the top shows what the system checks before a booking: the plan, the membership's state, how many of the 5 future bookings are used (BR-B5) and whether the member is suspended (BR-B4). Each class shows its places left (BR-B1), with Book while there is space and Join waitlist once it is full (the «extend»), together with the number already waiting. Premium classes are marked with their €8 surcharge (BR-B6) and off-peak classes are marked for Off-Peak members (BR-B5). When a booking is refused, the reason the membership gives is shown at the bottom, as in extension 4a.
