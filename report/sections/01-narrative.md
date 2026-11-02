# Narrative {#narrative}

<!--
Owner: Dhruv. Maximum 1 page (build.sh fails the build if it's longer).
The business problem in our own words, and the rules that make it compute heavy rather than a screen over a database:
plans and pricing, freezes, lapsing, discounts that don't stack, referral credits, waitlists, penalties.
Evidence: the business rules in report/requirements/README.md, the worked invoice sketch in report/sketches.
-->

KISS Solutions has been asked to build the membership system for a gym and fitness club that today runs mostly by hand at the front desk. The club sells four plans, and each membership changes over time. A new member pays a pro-rated first charge and starts a 7-day trial with two class bookings, then becomes Active if that payment went through or Lapsed if it didn't. An Active membership can be frozen for one to three months once a year and isn't billed while frozen. Two failed payments make it Lapsed, which blocks booking and check-in until the member pays what they owe, and after 60 days in Lapsed it is cancelled. Each plan adds its own rules: Annual is paid upfront with no trial and costs €50 to leave in the first six months, Student needs a valid student ID, and Off-Peak only allows weekdays between 10:00 and 16:00.

Classes and billing bring more rules. A class has a fixed capacity, and once it is full members join a first-come, first-served waitlist; when someone cancels, the first person waiting gets the place and is told. Cancelling less than two hours before a class costs €5, as does not turning up, and three no-shows in 30 days stop a member booking for a week. At the end of each month every member's bill is worked out from their plan price, minus the best of the corporate (15%), family (10%) or loyalty (5%) discounts, since they don't stack, plus any penalties and premium class surcharges, minus €20 for each friend they referred, up to €60 a year.

This is why the system is compute heavy rather than a set of screens over a database: almost every request has to be worked out from the member's plan, the state of their membership, their history and the date. The system has to give the same answer every time, explain a refusal, and let the club add a new plan or discount without rewriting what already works. Part 1 is a business tier for the four main areas, Membership, Billing, Booking and Member check-in, used through a REST API, with Postman or the Swagger UI standing in for the front end and JSON files standing in for the database.
