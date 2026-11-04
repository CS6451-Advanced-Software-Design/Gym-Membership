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

During this project, I took on the roles of Documentation Manager and Technical Lead, while also developing the Member check-in feature. I mainly worked on feature development, CI/CD, code quality, and project documentation. One thing I learned was that understanding the existing code is just as important as writing new code.

Before developing the Member check-in feature, I drew some rough diagrams to understand how the feature should work. After understanding the flow, I created clearer sequence diagrams for successful and refused check-ins and updated them to match the final implementation. I then developed the feature across the backend logic, APIs, data storage, and testing. I added seven unit tests and three end-to-end tests to cover different situations. Initially, I thought I needed a new notification system, but I found that the existing Membership and Billing features already had one, so I reused it instead.

I also worked on improving BillingService and MembershipService by separating some responsibilities into smaller classes. This reduced the complexity of BillingService, with CBO going from 25 to 22, WMC from 54 to 44, and RFC from 76 to 64. This taught me that good refactoring is about organising code better, not just removing it.

I spent time checking the CI/CD pipeline and code quality tools such as PMD, CK metrics, and SonarCloud. I fixed workflow and Docker build issues and investigated seven high-severity SonarCloud issues, of which five were genuine issues and two were false positives. I also added a Java version check in Maven to prevent build problems.

As Documentation Manager, I helped prepare the project report, checked the diagrams, table of contents, page numbers, and final PDF. I also updated the OpenAPI documentation after adding the Member API. This helped me understand the importance of keeping documentation up to date with the actual code.

One thing I would do differently next time is set up SonarCloud earlier. I treated it as a final task, but setting it up around week 6 would have helped me check code quality throughout development.

Overall, this project helped me improve my understanding of software development, from planning and implementation to testing, code quality, and documentation. Taking responsibility for both technical work and documentation also helped me understand how all these parts come together in a software project.


## Dhruv Punj

As the Business Analyst, I learned that requirements are mostly decisions. The first use case drafts left some questions open, and on 24 Oct I had to settle them before any code could be written:

- a trial member pays a pro-rated first charge straight away;
- the Annual plan starts Active, with no trial;
- a membership that stays Lapsed for 60 days is cancelled;
- family plans are out of scope.

Later, when I wrote UC1 in the full template, I found a gap that I had missed while coding: a declined Annual payment still starts the membership as Active. I only found it by going through every extension step by step, so now I see the template as a way of checking the design, not just documenting it.

Building Membership with the State pattern was the part where the course made the most sense to me:

- each state in the state chart became a subclass, and each arrow became a method;
- the base class refuses everything by default, so a Frozen membership can't book a class without a single `if` on the status;
- the states are so small that I could write a test for every arrow of the chart.

I also learned to be careful with names, because `PlanFactory` is a simple factory and not the GoF Factory Method.

Working inside the hexagon taught me to respect the boundaries of code that other people own. Membership had to bill members through Billing's ports without changing Chirag's `BillingService`, and ArchUnit failed my build when one of my DTOs reached into `port.in`. The most surprising bug was not in my logic at all: memberships that were already saved on Railway had no value for my new fields, so they failed to load until I made those fields optional. So, data lives longer than code.

What didn't work, and what I would do differently next time:

- **I started late.** I reviewed drafts in weeks 5 and 6 but didn't make any commits until 21 Oct, so most of my code came in one big pull request (#38, 20 commits). Smaller pull requests from the first iteration would have been easier to review and would have shown my progress week by week.

Overall, this project showed me what the lectures meant by analysis feeding design: the business rules, the state chart and the use cases decided my classes before I wrote them, and the tests and CI made sure they kept doing what the requirements said.
