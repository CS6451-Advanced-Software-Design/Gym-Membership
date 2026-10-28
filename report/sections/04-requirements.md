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

*To be written by Dhruv.*

## UC2: Book Class

*To be written by Dhruv.*

## Quality Attributes

*QA1 (extensibility) and QA2, each as a six-part scenario with its tactics, to be written by Dhruv.*

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
