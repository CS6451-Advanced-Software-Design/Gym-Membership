# Critique {#critique}

<!--
Owner: Chirag. Maximum 1 page.
Honest: where the code and the diagrams differ, what the metrics say (quote the final SonarQube Cloud numbers), the
usefulness of the diagrams and of UML, and how we would evaluate and document the architecture (e.g. ATAM).
-->

## Code Against Design

<!--
Your words, 2–3 sentences. Facts (from §10's table):
- The Hexagonal rule holds in the code: everything depends inwards on domain, and ArchitectureTest enforces it.
- Real drift: adapter.persistence stores domain objects directly as JSON (§5's admitted shortcut); adding a required
  field to Membership would break loading old files, since its constructor has 5 requireNonNull checks.
- Design too tidy: dto → domain (16 imports when recovered, 18 since the Member feature) and the composition root (BillingConfiguration,
  InterceptorConfiguration) aren't in §5's diagram, and Membership's dependency on Billing's use cases wasn't shown.
-->

## Metrics

<!--
Your take, 2–3 sentences. Facts:
- Coverage: 96.1% combined, but coverage shows which lines ran, not which results a test checked.
- The same three classes top both CK and PMD: BillingService is flagged as a GodClass (WMC 54, RFC 75, 192 lines) with
  too many methods; MembershipService couples to the most classes after its controller (CBO 28). The services are
  where the logic gathers, which Hexagonal allows but doesn't prevent.
- Most PMD violations are style, not design: 12 AvoidFieldNameMatchingMethodName (records' accessors) and
  11 ImplicitFunctionalInterface; 8 are size or coupling rules (GodClass, TooManyMethods, ExcessiveImports,
  CouplingBetweenObjects).
- What you'd do: split BillingService by use case (e.g. a separate class for the monthly run and for arrears).
-->

<!-- SonarQube Cloud: DineshReddy's final numbers (quality gate, bugs, code smells, duplication) can be added here. -->

| Tool | Result | Where it points |
| --- | --- | --- |
| JaCoCo | 96.1% line coverage (848 of 882) | Unit 75.2%, end-to-end 76.0%, integration 40.6% |
| PMD | 48 violations | BillingService: GodClass, TooManyMethods |
| CK | Highest CBO 29, highest WMC 54 | MembershipController (CBO), BillingService (WMC 54, RFC 75) |

Table: Code metrics on main (CI, 28 Oct)

## Diagrams and UML

<!--
Your words, 1–2 sentences. Facts:
- The recovered blueprints found real differences the design-time diagrams hid (§10), so they were worth making.
- But the tool silently dropped all 19 records, 22% of the types: a recovered diagram is only as current as the
  tool's parser, and has to be checked against the code before anyone trusts it.
- Stereotypes and intent («driving adapter», Hexagonal roles) can't be recovered from code.
-->

## Evaluating the Architecture

<!--
Your words, 2–3 sentences on how you'd evaluate and document it. Facts for ATAM (Bass et al.):
- ATAM takes the quality attribute scenarios (QA1: a new "Weekend Only" plan in under a day, changing no existing
  class), maps each to the architectural approaches (Strategy, State, Factory, Hexagonal ports), and looks for
  sensitivity points, trade-offs and risks.
- A risk it would find: the JSON persistence shortcut (schema change breaks old data).
- A trade-off: Hexagonal's extra interfaces (13 port.in, 8 port.out counting the generic Repository) buy testability (unit tests run on in-memory
  fakes) at the cost of more classes to read (§5's "complexity" liability).
- Documentation: we would keep the views (§5, §10) next to the scenarios, as in Views and Beyond.
-->
