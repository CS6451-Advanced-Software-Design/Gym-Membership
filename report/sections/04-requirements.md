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

Both are six-part scenarios (Bass et al. 2019), with the tactics that support them and the class that applies each one.

### QA1: Extensibility

The club will want new plans, each with its own price, trial, hours and eligibility, and adding one shouldn't mean editing sign-up, billing or booking.

| Part | QA1: adding a plan |
| --- | --- |
| Source / Stimulus | A developer; the manager asks for a Weekend plan at €25 a month |
| Artefact / Environment | The plans in `domain` and `PlanFactory`; design time |
| Response | One new `Plan` subclass, one `PlanType` value and one `PlanFactory` entry, with tests |
| Response measure | No change to `MembershipService`, Billing, Booking or controllers; the plan appears in `GET /plans`; all tests pass; under a day |

| Tactic (modifiability) | Where it is applied |
| --- | --- |
| Increase semantic coherence | Each plan's rules live in its own class (`StudentPlan`, `OffPeakPlan`, `AnnualPlan`) |
| Use an intermediary | Only `PlanFactory` maps a `PlanType` to its class; `MembershipService` never names one |
| Defer binding | The plan is chosen at run time and its rules run through polymorphism, not `switch` on the type |

The same holds for discounts (one more `DiscountPolicy`, Strategy) and states (one more `MembershipState`), and `PlanFactoryTest` fails if a `PlanType` has no class.

### QA2: Testability

Most rules depend on dates (a 7-day trial, 60 days in Lapsed), and we want each one tested quickly and repeatably without waiting days, charging cards or writing files.

| Part | QA2: testing a time-based rule |
| --- | --- |
| Source / Stimulus | A developer or CI; a rule changes, e.g. the 60-day Lapsed rule (BR-M6) |
| Artefact / Environment | Services and domain classes; no network, database or payment provider |
| Response | Unit tests move a test clock forward and use in-memory repositories and a fake gateway |
| Response measure | Every rule built so far has a unit test; 204 unit tests run with no files, network or real clock; a failure blocks the merge |

| Tactic (testability) | Where it is applied |
| --- | --- |
| Limit nondeterminism | Services take a `java.time.Clock`; tests pass a `TestClock`, so "60 days later" is instant and repeatable |
| Abstract data sources | Services only see `port.out` repositories, so tests use `InMemoryRepository` |
| Specialised interfaces | `PaymentGateway` and `NotificationSender` are ports, faked in tests to decline chosen cards and record messages |

## GUI Prototypes

The two prototypes are for the two key use cases. They are wireframes of screens the prototype doesn't have yet: today the same requests are made through Postman or the Swagger UI.

![Join screen for UC1 Sign Up: choose a plan, enter details, see the first payment](../requirements/gui-join.png){width=100%}

The Join screen follows UC1 from top to bottom. The four plans show their price and the rule that sets them apart (BR-M1), and the form asks only for what the rules need: the email to recognise a past member, the employer and family ID for discounts, the referral code, and the student ID for the Student plan. The first payment is worked out on the screen before the person pays, here Bob joining a Monthly plan on 25 October with 7 of 31 days left, €45 × 7/31 = €10.16 (BR-P4), with the trial it starts (BR-M2). Refusals from extensions 5a and 5b appear under the form before anything is charged.

![Book a Class screen for UC2: the week's classes with places left, booking and the waitlist](../requirements/gui-book-class.png){width=100%}

The Book a Class screen follows UC2. The bar at the top shows what the system checks before a booking: the plan, the membership's state, how many of the 5 future bookings are used (BR-B5) and whether the member is suspended (BR-B4). Each class shows its places left (BR-B1), with Book while there is space and Join waitlist once it is full (the «extend»), together with the number already waiting. Premium classes are marked with their €8 surcharge (BR-B6) and off-peak classes are marked for Off-Peak members (BR-B5). When a booking is refused, the reason the membership gives is shown at the bottom, as in extension 4a.
