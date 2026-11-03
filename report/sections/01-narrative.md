# Narrative {#narrative}

<!--
Owner: Dhruv. Maximum 1 page (build.sh fails the build if it's longer).
The business problem in our own words, and the rules that make it compute heavy rather than a screen over a database:
plans and pricing, freezes, lapsing, discounts that don't stack, referral credits, waitlists, penalties.
Evidence: the business rules in report/requirements/README.md, the worked invoice sketch in report/sketches.
-->

We have Built a membership system for Gym's because right now, The Staff at the front desk of the gym/fitness club is doing most of the work by hand. The gym sells 4 types of plans. After joining the member pays for 1 month and gets a 7-day free trial, after payment is completed, the member becomes active after the 7-day trial if not, it becomes lapsed. The membership can also be paused for 1-3 months (once a year), they don't pay when membership is frozen. If payment is failed 2 times in a row, membership is lapsed and member can't use the gym facilities or book classes until he/she pays what's owed. If membership stays lapsed for 60 days, it is cancelled. Also, different plans has some rules like annual membership fees is paid at once, comes with no free trail and cost €50 to cancel in the first 6 months; student plan need a student ID and off-peak plan only allows weekdays timings between 10:00-16:00.

Classes and billing bring more rules. A class has a fixed capacity, and when it is full members join a first-come, first-served waitlist that fills a place as soon as someone cancels. A late cancellation or a no-show costs €5, and three no-shows in 30 days stop a member booking for a week. Each month's bill is the plan price minus the best of the corporate (15%), family (10%) or loyalty (5%) discounts, which don't stack, plus penalties and premium surcharges, minus €20 for each friend referred, up to €60 a year. Almost every request therefore has to be worked out from the member's plan, state, history and the date, which is what makes the system compute heavy rather than screens over a database. Part 1 is the business tier for Membership, Billing, Booking and Member check-in, used through a REST API with Postman or the Swagger UI as the front end and JSON files as the database.
