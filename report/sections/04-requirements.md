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

*To be written by Dhruv.*

## Quality Attributes

*QA1 (extensibility) and QA2, each as a six-part scenario with its tactics, to be written by Dhruv.*

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
