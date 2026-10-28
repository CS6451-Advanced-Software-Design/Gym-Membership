# Narrative {#narrative}

<!--
Owner: Dhruv. Maximum 1 page (build.sh fails the build if it's longer).
The business problem in our own words, and the rules that make it compute heavy rather than a screen over a database:
plans and pricing, freezes, lapsing, discounts that don't stack, referral credits, waitlists, penalties.
Evidence: the business rules in report/requirements/README.md, the worked invoice sketch in report/sketches.
-->

KISS Solutions has been asked to build the membership system for a gym and fitness club. The club sells memberships on four plans, runs a timetable of classes with a limited number of places, and bills its members every month. Today most of this is done by hand at the front desk: staff work out what a new member owes for the rest of the month, keep track of who has frozen their membership, chase failed payments, and decide whether a member may still book a class. Each of these decisions follows a rule, and the rules depend on each other, which is where mistakes and arguments with members come from.
