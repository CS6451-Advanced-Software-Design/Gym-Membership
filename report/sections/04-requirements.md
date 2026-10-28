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

The diagram shows who uses the system and what they use it for. There are five human actors and two non-human ones:

- **Prospective Member:** someone who isn't a member yet and can only sign up. **Member** specialises Prospective Member, so a member can sign up again after cancelling, but a prospective member can't book or check in.
- **Front Desk Staff:** checks members in and handles freezes and cancellations for them.
- **Instructor:** runs classes, records attendance and helps with the schedule.
- **Manager:** looks after the plans, prices, class schedule and reports.
- **Clock:** a time actor. It starts the monthly billing run (UC7) and the daily membership checks (UC13), so these happen without anyone pressing a button.
- **Payment Gateway:** the outside system that charges cards, used by sign-up, billing, cancelling and paying arrears.

The 14 use cases cover the whole club, but the business rules sit in a few of them. Shared steps are pulled out with «include»: Calculate Charges is used by sign-up, billing, cancelling and the daily checks, and Verify Membership Status by booking and check-in. Optional steps that only happen under a condition are «extend», each with its condition on the arrow, for example Join Waitlist when a class is full and Charge Early-Exit Fee when an Annual plan is cancelled within 6 months.

UC1 Sign Up and UC2 Book Class are the two key use cases, described in full below, because between them they touch most of the business rules: plans and eligibility, pro-rating, discounts and referrals on the way in, and membership state, plan restrictions, capacity and the waitlist when booking. The prototype implements UC1, UC4, UC5, UC6, UC7, UC11, UC13 and UC14; the Booking use cases (UC2, UC3 and UC8) are being built, and the Manager's use cases (UC9, UC10 and UC12) are left for a later release.

## UC1: Sign Up for Membership

| USE CASE 1 | Sign Up for Membership |
| --- | --- |
| Goal in Context | A person joins the club on one of the four plans and pays for the rest of the first month, or for the whole year on the Annual plan |
| Scope & Level | Gym membership system, user goal |
| Preconditions | The plans and their prices are set up (BR-M1). The person doesn't already hold a membership that isn't cancelled |
| Success End Condition | The person is a member with a membership in Trial (Active for the Annual plan), the first invoice is issued and a welcome message is sent. If they gave a valid referral code and paid, the referrer gets their credit |
| Failed End Condition | Nothing is saved and nothing is charged |
| Primary, Secondary Actors | Prospective Member (or a Member signing up again after cancelling); Payment Gateway |
| Trigger | The person chooses Join |

| DESCRIPTION | Step | Action |
| --- | --- | --- |
| | 1 | The person asks to see the plans |
| | 2 | The system lists the four plans with their price, trial and access hours (BR-M1) |
| | 3 | The person chooses a plan and enters their name and email, plus a student ID, employer, family ID or referral code if they have one |
| | 4 | The system looks up the email, and if it belongs to a past member, signs that member up again rather than making a new one |
| | 5 | The system checks that the person may take the plan and that any referral code belongs to a current membership |
| | 6 | The system saves the member and starts the membership: a 7-day Trial with 2 class bookings, or Active straight away on the Annual plan (BR-M2) |
| | 7 | The system bills the first period, pro-rated by the days left in the month (BR-P4) minus the best discount (BR-P2), «include» Calculate Charges, and charges the card through the Payment Gateway |
| | 8 | The payment goes through. If a referral code was given, the referrer gets €20 credit, «extend» Apply Referral Credit (BR-P3) |
| | 9 | The system sends a welcome message and shows the new membership with its first invoice |

| EXTENSIONS | Step | Branching Action |
| --- | --- | --- |
| | 4a | The past member still holds a membership that isn't cancelled: 4a1. The system refuses and names that membership. Nothing is saved |
| | 5a | The Student plan is chosen without a valid 8-digit student ID: 5a1. The system refuses and the person returns to step 3. Nothing is saved |
| | 5b | The referral code doesn't belong to a current membership: 5b1. The system refuses and the person returns to step 3. Nothing is saved |
| | 8a | The card is declined: 8a1. The membership is still created, the first invoice stays unpaid and a failed payment is recorded. 8a2. No referral credit is given, and the welcome message says the payment failed. 8a3. When the trial ends, the membership becomes Lapsed (BR-M2), and the member can pay what they owe to make it Active again (UC14) |

| VARIATIONS | Step | Branching Action |
| --- | --- | --- |
| | 1 | The person signs up themselves online, or at the front desk with a member of staff entering their details |
| | 3 | Any of the four plans: Monthly (€45), Student (€30), Off-Peak (€30), or Annual (€450 for the year, paid upfront, with no trial) |
| | 7 | The discount is Corporate 15% when an employer is given, or Family 10% for the second member of a family onward; Loyalty 5% needs 24 months, so it never applies at sign-up |

| RELATED INFORMATION | Sign Up for Membership |
| --- | --- |
| Priority | Top: without it nobody becomes a member |
| Performance | The whole sign-up, including the card payment, answers in under 2 seconds |
| Frequency | About 20 a day, more in January |
| Channel to actors | REST API (`POST /memberships`), tried through Postman or the Swagger UI; the Join screen in the first GUI prototype |
| OPEN ISSUES | On the Annual plan a declined payment still starts the membership Active, and it only lapses after a second failure. Should sign-up refuse it instead? |
| | Card details are left to the Payment Gateway, which the prototype fakes |
| | Family memberships with one payer for several members were ruled out of scope on 24 Oct; the family ID is only used for the Family discount |
| Due Date | Release 0.2.0 (done) |
| Superordinates | Manage memberships |
| Subordinates | Calculate Charges, Apply Referral Credit |

## UC2: Book Class

| USE CASE 2 | Book Class |
| --- | --- |
| Goal in Context | A member reserves a place in a class on the timetable, or a place on its waitlist when the class is full |
| Scope & Level | Gym membership system (Booking), user goal |
| Preconditions | The member holds a membership. The class is on the timetable and hasn't started |
| Success End Condition | The member has a confirmed booking or a waitlist position. Any premium surcharge is recorded for the next bill, and a Trial booking is counted against the limit of 2 |
| Failed End Condition | No booking or waitlist place is created, nothing is charged, and the member is told why |
| Primary, Secondary Actors | Member; Front Desk Staff when booking for a member |
| Trigger | The member picks a class and chooses Book |

| DESCRIPTION | Step | Action |
| --- | --- | --- |
| | 1 | The member opens the timetable and picks a class |
| | 2 | The system checks that the member isn't suspended for no-shows (3 in 30 days, BR-B4) |
| | 3 | The system checks that the member holds fewer than 5 future bookings (BR-B5) |
| | 4 | The system asks the membership whether it may book this class, «include» Verify Membership Status. The membership asks its state (Trial while fewer than 2 bookings are used, Active yes, Frozen, Lapsed and Cancelled no) and its plan (Off-Peak only books off-peak classes, BR-B5) |
| | 5 | The system checks that the class has a free place (BR-B1) |
| | 6 | The system shows any premium surcharge (€8, BR-B6) and the member confirms |
| | 7 | The system creates the booking, records the surcharge for the next bill, counts the booking if the membership is in Trial, and confirms |

| EXTENSIONS | Step | Branching Action |
| --- | --- | --- |
| | 2a | The member is suspended: 2a1. The system refuses and says when the suspension ends |
| | 3a | The member already holds 5 future bookings: 3a1. The system refuses |
| | 4a | The state or the plan says no, for example a Frozen membership or an Off-Peak member choosing an evening class: 4a1. The system refuses and gives the reason |
| | 5a | The class is full: 5a1. «extend» Join Waitlist: the member goes to the end of the waitlist and is told their position. 5a2. When someone cancels, the first person waiting is booked automatically and notified (BR-B2, UC3) |

| VARIATIONS | Step | Branching Action |
| --- | --- | --- |
| | 1 | The member books online, or Front Desk Staff book for them |
| | 6 | Annual members pay no premium surcharge, and an ordinary class has none |

| RELATED INFORMATION | Book Class |
| --- | --- |
| Priority | Top: booking is what members do most |
| Performance | Answers in under 1 second. When two members try to book the last place at the same time, only one of them gets it |
| Frequency | About 500 a day, with a peak when the next week's timetable opens |
| Channel to actors | REST API (`POST /bookings`); the Book a Class screen in the second GUI prototype |
| OPEN ISSUES | Can a member book two classes that overlap in time? |
| | Should a member on the waitlist of a class that starts within 2 hours still be promoted, when they may not see the notification in time? |
| | Booking is still being implemented, so this description and the sequence diagram in §6 are the design it follows |
| Due Date | Release 1.0 |
| Superordinates | Manage bookings |
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
