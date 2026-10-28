# Reflection {#reflection}

<!--
Everyone writes their own part, in their own voice: what they learned, what worked and what didn't, and the value of
the project. Be specific, and write about yourself, not the team. Notes with evidence to draw on: report/lessons.md.
-->

## Chirag Aggarwal

*To be written by Chirag.*

## Bharat Doodi

*To be written by Bharat.*

## DineshReddy Mogili

*To be written by DineshReddy.*

## Dhruv Punj

As the Business Analyst I learned that requirements are mostly decisions. The first drafts of the use cases left questions open: does a trial member pay straight away, what happens to a membership that stays Lapsed, are family plans in scope? On 24 Oct I had to decide them (a pro-rated first charge, Annual starting Active with no trial, cancelling after 60 days in Lapsed, no family plans) and write them into the business rules, because the code couldn't be written until they were settled. Writing UC1 in the full template later showed me a gap I had missed while coding: on the Annual plan a declined payment still starts the membership Active. Going through every extension step by step is what found it, so I now see the template as a way of checking the design, not just documenting it.

Building Membership with the State pattern was where the course made the most sense to me. The state chart in §6 turned almost directly into classes: each state became a subclass, each arrow became a method, and the base class refusing everything by default meant that a Frozen membership can't book a class without a single `if` on the status. Because the states are so small, I could write a test for every arrow of the chart. I also learned to be careful with names: `PlanFactory` is a simple factory, a single place that picks the class for each plan, and not the GoF Factory Method, so I describe it that way.

Working inside the hexagon taught me to respect boundaries that other people own. Membership had to bill members through Billing's ports without changing `BillingService`, which Chirag had written, and ArchUnit failed my build when one of my DTOs reached into `port.in`, a rule I had read about but only understood when it stopped me. The most surprising bug was not in my logic at all: memberships already saved on Railway had no value for my new fields, so loading them failed until I made those fields optional. Data outlives code, and a change has to work with what is already stored.
