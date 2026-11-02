# Narrative {#narrative}

<!--
Owner: Dhruv. Maximum 1 page (build.sh fails the build if it's longer).
The business problem in our own words, and the rules that make it compute heavy rather than a screen over a database:
plans and pricing, freezes, lapsing, discounts that don't stack, referral credits, waitlists, penalties.
Evidence: the business rules in report/requirements/README.md, the worked invoice sketch in report/sketches.
-->

KISS Solutions has been asked to build the membership system for a gym and fitness club that today runs mostly by hand at the front desk. The club sells four plans, and each membership changes over time. A new member pays a pro-rated first charge and starts a 7-day trial with two class bookings, then becomes Active if that payment went through or Lapsed if it didn't. An Active membership can be frozen for one to three months once a year and isn't billed while frozen. Two failed payments make it Lapsed, which blocks booking and check-in until the member pays what they owe, and after 60 days in Lapsed it is cancelled. Each plan adds its own rules: Annual is paid upfront with no trial and costs €50 to leave in the first six months, Student needs a valid student ID, and Off-Peak only allows weekdays between 10:00 and 16:00.

Classes and billing bring more rules. A class has a fixed capacity, and when it is full members join a first-come, first-served waitlist that fills a place as soon as someone cancels. A late cancellation or a no-show costs €5, and three no-shows in 30 days stop a member booking for a week. Each month's bill is the plan price minus the best of the corporate (15%), family (10%) or loyalty (5%) discounts, which don't stack, plus penalties and premium surcharges, minus €20 for each friend referred, up to €60 a year. Almost every request therefore has to be worked out from the member's plan, state, history and the date, which is what makes the system compute heavy rather than screens over a database. Part 1 is the business tier for Membership, Billing, Booking and Member check-in, used through a REST API with Postman or the Swagger UI as the front end and JSON files as the database.
