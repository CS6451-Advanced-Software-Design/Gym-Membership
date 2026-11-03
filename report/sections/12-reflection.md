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

As the Business Analyst, I learned that requirements are mostly decisions. The first use case drafts left some questions open, and on 24 Oct I had to settle them before any code could be written:

- a trial member pays a pro-rated first charge straight away;
- the Annual plan starts Active, with no trial;
- a membership that stays Lapsed for 60 days is cancelled;
- family plans are out of scope.

Later, when I wrote UC1 in the full template, I found a gap that I had missed while coding: a declined Annual payment still starts the membership as Active. I only found it by going through every extension step by step, so now I see the template as a way of checking the design, not just documenting it.

Building Membership with the State pattern was where the course made the most sense to me:

- each state in the §6 state chart became a subclass and each arrow a method;
- the base class refuses everything by default, so a Frozen membership can't book without a single `if` on the status;
- the states are so small that I could test every arrow of the chart.

I also learned to be careful with names: `PlanFactory` is a simple factory, not the GoF Factory Method.

Working inside the hexagon taught me to respect boundaries other people own. Membership had to bill through Billing's ports without changing Chirag's `BillingService`, and ArchUnit failed my build when one of my DTOs reached into `port.in`. The most surprising bug wasn't in my logic: memberships already saved on Railway had no value for my new fields, so they failed to load until I made those fields optional. Data outlives code.

What didn't work, and what I would do differently:

- **I started late.** I reviewed drafts in weeks 5 and 6 but made no commits until 21 Oct, so most of my code arrived in one large pull request (#38, 20 commits). Smaller pull requests from the first iteration would have been easier to review and shown my progress week by week.
- **I sketched after coding.** My hand sketches record the design rather than help me find it; next time I would sketch first.
- **GenAI needed checking.** I used it a lot, and one draft claimed the Loyalty discount could apply at sign-up, which it can't, so now I check everything it produces against the code and the brief.

Overall the project showed me what the lectures meant by analysis feeding design: the business rules, state chart and use cases decided my classes before I wrote them, and the tests and CI kept them doing what the requirements said.
