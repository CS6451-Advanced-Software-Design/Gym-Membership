# Reflection {#reflection}

<!--
Everyone writes their own part, in their own voice: what they learned, what worked and what didn't, and the value of
the project. Be specific, and write about yourself, not the team. Notes with evidence to draw on: report/lessons.md.
-->

## Chirag Aggarwal

As the "Architect" of the team, which I feel is a very heavy word, I mostly turned the overall architecture, which we all decided together, into diagrams the team could use for a consistent implementation.

1. Hosting: even though hosting was not required, I really wanted it there. At the same time, I really wanted to use Java. So even though I wanted to use Appwrite for hosting, it didn't support a long-running Java server, and we had to switch to Railway.
2. The Hexagonal pattern was very abstract on the surface, and it needed something more concrete to stay in place. So designing `ArchitectureTest` helped a lot in enforcing it.
3. While matching the documentation with the code, we saw a `BillingJob` described that our first implementation never had. So I found the importance of keeping docs up to date even more.

Diagrams were where most of our effort went. GenAI was very good at generating them, but sometimes they were too complicated, out of scope, or not what we had in mind. Because of this our implementation started a bit later than we expected, but I would still say the time spent there was well worth it.

Decisions that ended up being super valuable:

- Enforcing strict CI from the first line of code: tests should always pass, and each merge is deployed to Railway for everyone to see.
- Using Infrastructure as Code to define our configs meant we had a much clearer view, even though not everyone could see the Railway dashboard.
- Our GenAI usage, as you can see, was on the higher end, but most of our prompts were for our own understanding and for breaking down processes that would have taken hours or weeks to do manually.

At Appwrite I already follow a lot of these design and best practices, so I could enforce them here easily. As a learning challenge, I tried my best to concern myself with "boring" stuff like diagrams, which I now value more than I did before.

## Bharat Doodi

*To be written by Bharat.*

## DineshReddy Mogili

As Technical Lead and DevOps, most of what I learned was about the gap between a pipeline that runs and a pipeline that is actually trustworthy. CI failing a merge on a test failure is the easy half; the harder half is the pipeline failing *honestly*. Two small bugs taught me this directly: `curl -sLo` for the CK and JaCoCo jars had no `-f` flag, so a broken download URL would silently write an HTML error page to the `.jar` path and only surface, several steps later, as a cryptic `ZipException` with no obvious connection to the real cause. Neither CI job had a `timeout-minutes` either, so a single hung test could have burned our CI minutes for up to GitHub's six-hour default before anyone noticed. Neither bug ever fired on us, but that is exactly the point: the pipeline had been quietly trusting the happy path, and fixing both was about making a failure point at its real cause instead of three steps downstream.

Building the Member feature's check-in notifications taught me something about the Observer pattern that I would not have guessed from the slides: once it exists once, it is nearly free the second, third and fourth time. I expected to build a new notification channel for check-ins, but `NotificationSender`, the Spring event bus adapter and the notification log already existed from Membership and Billing. Calling `notifications.send(...)` from `MemberService` was the entire cost. A pattern earning its keep is not one diagram per feature; it is the fourth feature needing zero new plumbing.

Running PMD and CK as Technical Lead gave me the clearest lesson of the project, because the first fix I tried was the wrong one. My instinct with a flagged `GodClass` was to look for *something* to cut out of `BillingService` until the number moved. What actually worked was slower: reading where the class's own `port.in` interfaces already drew a boundary, and extracting along that line instead of an arbitrary one. The `GodClass` violation disappeared entirely once I did, and a repository dependency it had carried turned out to be used by nothing else once the boundary was followed correctly, so it dropped out rather than just moving somewhere else. The metric agreeing with the architecture, instead of fighting it, is what convinced me the number was measuring something real.

What I would do differently is sequencing: I treated SonarQube Cloud as a last-week task because the spec says to run it "once the features are implemented," but that meant it was still not wired up when I had already identified and fixed the worst PMD/CK classes. The honest fix is running SonarQube against the commit before each refactor rather than only against `main` today, so the before/after table is a true comparison and not just "whenever we got around to installing it" - but I would rather have set up the account in week 6 and never needed that workaround at all.

Overall, the project changed how I think about "added value": it is not a checklist of tools bolted onto working code at the end, it is whether the pipeline, the metrics and the refactoring actually feed each other while the code is still being written.

## Dhruv Punj

As the Business Analyst I learned that requirements are mostly decisions. The first drafts of the use cases left questions open: does a trial member pay straight away, what happens to a membership that stays Lapsed, are family plans in scope? On 24 Oct I had to decide them (a pro-rated first charge, Annual starting Active with no trial, cancelling after 60 days in Lapsed, no family plans) and write them into the business rules, because the code couldn't be written until they were settled. Writing UC1 in the full template later showed me a gap I had missed while coding: on the Annual plan a declined payment still starts the membership Active. Going through every extension step by step is what found it, so I now see the template as a way of checking the design, not just documenting it.

Building Membership with the State pattern was where the course made the most sense to me. The state chart in §6 turned almost directly into classes: each state became a subclass, each arrow became a method, and the base class refusing everything by default meant that a Frozen membership can't book a class without a single `if` on the status. Because the states are so small, I could write a test for every arrow of the chart. I also learned to be careful with names: `PlanFactory` is a simple factory, a single place that picks the class for each plan, and not the GoF Factory Method, so I describe it that way.

Working inside the hexagon taught me to respect boundaries that other people own. Membership had to bill members through Billing's ports without changing `BillingService`, which Chirag had written, and ArchUnit failed my build when one of my DTOs reached into `port.in`, a rule I had read about but only understood when it stopped me. The most surprising bug was not in my logic at all: memberships already saved on Railway had no value for my new fields, so loading them failed until I made those fields optional. Data outlives code, and a change has to work with what is already stored.

What didn't work was my start. I reviewed drafts in weeks 5 and 6 but made no commits until 21 Oct, so most of my code arrived in one large pull request (#38, 20 commits) in the last two weeks, with `main` already protected and the deadline close. Smaller pull requests from the first iteration would have been easier to review and would have shown my progress week by week. I also drew my hand sketches after the code instead of before it, so they record the design rather than help me find it; next time I would sketch first.

I used GenAI a lot in this project, and the main thing I learned was to check everything it produced against the code and the brief, because a confident sentence can still be wrong: one draft claimed the Loyalty discount could apply at sign-up, which it can't, as it needs 24 months. Overall the project showed me what the lectures meant by analysis feeding design: the business rules, the state chart and the use cases decided my classes before I wrote them, and the tests and CI made sure they kept doing what the requirements said.
