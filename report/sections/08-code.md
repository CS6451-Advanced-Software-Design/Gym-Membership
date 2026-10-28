# Code {#code}

<!--
Everyone. One captioned snippet per pattern with the test that proves it, a table mapping each use case to the classes
that implement it, Hexagonal shown by ArchitectureTest, and the version control visualisations (commit graph,
contributors chart). Each pattern's owner writes its part.
Evidence: the Patterns table in the root README.md.
-->

## Use Cases and Their Classes

We designed all our use cases to enter through a controller (or a scheduled job), going through a `port.in` interface and then landing on a service. It's easy to read this flow from left to right following the request path.

| Use case | Entry point | Port (`port.in`, without "UseCase") | Service | Domain classes |
| --- | --- | --- | --- | --- |
| UC1 Sign Up | `POST /memberships` | SignUp | MembershipService | PlanFactory, Plan, Member, Membership; Billing's CalculateCharges |
| UC5 Freeze / Unfreeze | `POST …/freeze`, `…/unfreeze` | FreezeMembership | MembershipService | Membership, Freeze, FrozenState |
| UC6 Cancel Membership | `POST …/cancel` | CancelMembership | MembershipService | Membership, CancelledState; Billing's final invoice and early-exit fee |
| UC7 Run Monthly Billing | BillingJob (monthly), `POST /billing-runs` | RunMonthlyBilling | BillingService | BestDiscount (Strategy), Charge, Invoice |
| UC11 View Invoices | `GET …/invoices` | ViewInvoices | BillingService | Invoice, Charge |
| UC13 Daily Checks | MembershipChecksJob (daily), `POST /membership-checks` | RunDailyChecks | MembershipService | TrialState, FrozenState, LapsedState |
| UC14 Pay Arrears | `POST …/pay-arrears` | RestoreMembership, PayArrears | MembershipService, BillingService | LapsedState, Invoice |

Table: Each use case built so far, from where it enters to the classes that carry it out ("…" is `/memberships/{membershipId}`)

<!-- UC2–UC4, UC8–UC10 and UC12 (bookings, attendance, plans, schedule, reports) aren't built yet. -->


## Architectural Patterns

*MVC, Hexagonal and Interceptor, each with a snippet and its test.*

```java
@ArchTest
static final ArchRule layersOnlyDependInwards = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .withOptionalLayers(true)
        // ... one layer per package
        .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
        .whereLayer("Inbound port").mayOnlyBeAccessedByLayers("Controller", "Service")
        .whereLayer("Outbound port").mayOnlyBeAccessedByLayers("Service", "Adapter")
        .whereLayer("Service").mayNotBeAccessedByAnyLayer()
        .whereLayer("Adapter").mayNotBeAccessedByAnyLayer();

@ArchTest
static final ArchRule domainOnlyDependsOnTheJdk = classes()
        .that().resideInAPackage("gym.domain..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("gym.domain..", "java.lang..", "java.util..", "java.time..", "java.math..");
```

```java
@Bean
public UseCaseDispatcher useCaseDispatcher() {
    UseCaseDispatcher dispatcher = new UseCaseDispatcher();
    dispatcher.register(new LoggingInterceptor(), 0);
    return dispatcher;
}
```

## Design Patterns

*State and Factory (Dhruv), Observer (Bharat), each with a snippet and its test.*

### Strategy

We used the Strategy design pattern for designing the discounting logic in our application.

1. Discounts cannot be stacked. If a member qualifies for more than one discount, we use the best one out of them.
2. Each new discount strategy can be easily added like the current ones - CorporateDiscount, FamilyDiscount and LoyaltyDiscount.
3. There is a shared policy called BestDiscount.
4. No change is needed to BillingService when adding a discount strategy, only one entry in BillingConfiguration.

```java
public interface DiscountPolicy {
    Money discountFor(BillingAccount account, Money amount);
}

public class FamilyDiscount implements DiscountPolicy {
    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        return account.familyMember() ? amount.percent(10) : Money.ZERO;
    }
}

public class BestDiscount implements DiscountPolicy {
    private final List<DiscountPolicy> policies;

    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        Money best = Money.ZERO;
        for (DiscountPolicy policy : policies) {
            best = best.max(policy.discountFor(account, amount));
        }
        return best;
    }
}
```

*The Strategy and two of its strategies (`gym/domain`, constructor and comments left out)*

```java
@Bean
public DiscountPolicy discountPolicy() {
    return new BestDiscount(List.of(
            new CorporateDiscount(), new FamilyDiscount(), new LoyaltyDiscount()));
}
```

*Choosing the strategies (`gym/BillingConfiguration.java`)*

```java
@Test
void theBestDiscountWinsAndDiscountsDontStack() {
    // BR-P2: corporate (15%), family (10%) and loyalty (5%) all apply,
    // but only the 15% is taken
    BillingAccount everything = account(true, true, 36);

    assertEquals(Money.euros("6.75"), best.discountFor(everything, FEE));
}
```

*The test that proves discounts don't stack (`DiscountPolicyTest`, fee €45)*

## Automated Tests

We divided the testing strategy into 3 popular ones that most of the industry follows - unit, integration and end-to-end. JUnit 5 was used for all three, and the unit tests use in-memory fakes of ports like `InMemoryRepository`. All three strategies run in their own lane in CI/CD. Since 25 Oct, we also enforced that a new PR cannot be merged without having all testing CI lanes pass first.

Coverage of the tests is measured by JaCoCo (Java Code Coverage), and reported by its own CI job.

| Kind | Tests | Line coverage |
| --- | --- | --- |
| Unit | 101 | 75.4% |
| Integration | 20 | 40.6% |
| End-to-end | 11 | 74.0% |
| **All** | **132** | **95.8%** (801 of 836 lines) |

Table: Automated tests on main (CI run of 28 Oct)

## Version Control

We used the most popular version control system, Git, along with the most popular Git provider, GitHub. We enabled GitHub's own bot, Dependabot, to allow automatic version updates to our dependencies (2 of its pull requests already merged).

::: {.grid columns=2}
![Commits to main per week](../version-control/commits.png){width=100%}

![Pull requests merged into main over time](../version-control/pulls.png){width=100%}
:::
