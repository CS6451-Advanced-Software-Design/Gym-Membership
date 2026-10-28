# Narrative {#narrative}

<!--
Owner: Dhruv. Maximum 1 page (build.sh fails the build if it's longer).
The business problem in our own words, and the rules that make it compute heavy rather than a screen over a database:
plans and pricing, freezes, lapsing, discounts that don't stack, referral credits, waitlists, penalties.
Evidence: the business rules in report/requirements/README.md, the worked invoice sketch in report/sketches.
-->

KISS Solutions has been asked to build the membership system for a gym and fitness club. The club sells memberships on four plans, runs a timetable of classes with a limited number of places, and bills its members every month. Today most of this is done by hand at the front desk: staff work out what a new member owes for the rest of the month, keep track of who has frozen their membership, chase failed payments, and decide whether a member may still book a class. Each of these decisions follows a rule, and the rules depend on each other, which is where mistakes and arguments with members come from.

The membership itself changes over time. A new member pays a pro-rated first charge and starts a 7-day trial with two class bookings; at the end of the trial the membership becomes Active if that payment went through, or Lapsed if it didn't. An Active membership can be frozen for one to three months, at most once a year, and is not billed while frozen. Two failed payments in a row make it Lapsed, which blocks booking and check-in until the member pays what they owe, and a membership left Lapsed for 60 days is cancelled with a final bill. Each plan adds its own rules on top: the Annual plan is paid upfront with no trial and costs €50 to leave in the first six months, the Student plan needs a valid student ID, and the Off-Peak plan only allows check-in and classes on weekdays between 10:00 and 16:00.
