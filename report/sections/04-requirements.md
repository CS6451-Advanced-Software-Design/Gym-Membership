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

## UC2: Book Class

*To be written by Dhruv.*

## Quality Attributes

*QA1 (extensibility) and QA2, each as a six-part scenario with its tactics, to be written by Dhruv.*

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
