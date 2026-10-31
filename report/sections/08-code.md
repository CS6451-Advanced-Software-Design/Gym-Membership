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
| UC4 Check In | `POST /members/{memberId}/check-ins` | CheckIn | MemberService | Member, Membership (`canCheckIn`), CheckIn |
| UC5 Freeze / Unfreeze | `POST …/freeze`, `…/unfreeze` | FreezeMembership | MembershipService | Membership, Freeze, FrozenState |
| UC6 Cancel Membership | `POST …/cancel` | CancelMembership | MembershipService | Membership, CancelledState; Billing's final invoice and early-exit fee |
| UC7 Run Monthly Billing | BillingJob (monthly), `POST /billing-runs` | RunMonthlyBilling | BillingService | BestDiscount (Strategy), Charge, Invoice |
| UC11 View Invoices | `GET …/invoices` | ViewInvoices | BillingService | Invoice, Charge |
| UC13 Daily Checks | MembershipChecksJob (daily), `POST /membership-checks` | RunDailyChecks | MembershipService | TrialState, FrozenState, LapsedState |
| UC14 Pay Arrears | `POST …/pay-arrears` | RestoreMembership, PayArrears | MembershipService, BillingService | LapsedState, Invoice |

Table: Each use case built so far, from where it enters to the classes that carry it out ("…" is `/memberships/{membershipId}`)

<!-- UC2, UC3, UC8–UC10 and UC12 (bookings, attendance, plans, schedule, reports) aren't built yet. -->


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

*Observer (Bharat), with a snippet and its test.*

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

### State

We used the State design pattern for the membership lifecycle: Trial, Active, Frozen, Lapsed and Cancelled, the states in the §6 state chart.

1. What a membership may do depends on its state. A Trial can book only 2 classes, an Active one can be frozen, and a Frozen or Lapsed one can't book or check in.
2. Each state is its own class, and overrides only the operations it allows. Everything else is refused once, in the base class, with a reason the member can read (a 422 over REST).
3. The states keep no fields, so one instance of each is shared, chosen from the membership's status.
4. Adding a state means one new subclass. There is no `if` or `switch` on the status in `Membership` or in any service.

```java
public abstract class MembershipState {
    public static MembershipState of(MembershipStatus status) {
        return switch (status) {
            case TRIAL -> TRIAL;
            case ACTIVE -> ACTIVE;
            case FROZEN -> FROZEN;
            case LAPSED -> LAPSED;
            case CANCELLED -> CANCELLED;
        };
    }

    public boolean canBook(Membership membership) {
        return false;
    }

    public Membership freeze(Membership membership, int months, LocalDate today) {
        throw refused(membership, "be frozen");
    }
    // ... endTrial, recordPayment, payArrears, cancel, expire, checkOn
}

public class ActiveState extends MembershipState {
    @Override
    public boolean canBook(Membership membership) {
        return true;
    }

    @Override
    public Membership freeze(Membership membership, int months, LocalDate today) {
        // BR-M3: 1 to 3 months, at most once in any 12 months
        // ... both checks, each throwing a RuleViolationException
        return membership.moveTo(MembershipStatus.FROZEN).withFreeze(new Freeze(today, months));
    }
}
```

*The State and one of its five states (`gym/domain`, the shared instances and other operations left out)*

```java
public MembershipState state() {
    return MembershipState.of(status);
}

public boolean canBook() {
    return state().canBook(this);
}

public Membership freeze(int months, LocalDate today) {
    return state().freeze(this, months, today);
}
```

*The context: `Membership` hands every operation to its current state (`gym/domain/Membership.java`)*

```java
@Test
void aTrialCannotBeFrozen() {
    RuleViolationException refused = assertThrows(RuleViolationException.class,
            () -> trial().freeze(1, JOINED));

    assertEquals("Membership ms1 is Trial, so it can't be frozen", refused.getMessage());
}

@Test
void freezingStopsBookingAndCheckInAndMovesTheEndDate() {
    // BR-M3: an Annual plan frozen for 2 months ends 2 months later
    Membership annual = Membership.signUp("ms2", "m2", new AnnualPlan(), JOINED);

    Membership frozen = annual.freeze(2, JOINED.plusMonths(1));

    assertEquals(MembershipStatus.FROZEN, frozen.status());
    assertFalse(frozen.canBook());
    assertFalse(frozen.canCheckIn(MONDAY_EVENING));
    assertEquals(JOINED.plusYears(1).plusMonths(2), frozen.endDate());
}
```

*The same request, freeze, refused by one state and carried out by another (`MembershipStateTest`, which has a test for every arrow of the state chart)*

### Factory

We used a Factory for creating the four plans: Monthly, Annual, Student and Off-Peak.

1. Each plan is its own class with its own rules: its price, whether it has a trial, its end date, when it allows check-in and classes, and who is eligible.
2. `PlanFactory` is the only class that knows which class goes with which plan type. The sign-up request names a `PlanType`, and the service asks the factory for the plan.
3. Callers only ever see the abstract `Plan`, so they never name a plan class.
4. Adding a plan means one new `Plan` subclass, one `PlanType` value with its price and one entry in the factory's map. No change is needed to `MembershipService`, Billing or Booking.

```java
public final class PlanFactory {
    private static final Map<PlanType, Supplier<Plan>> PLANS = new EnumMap<>(Map.of(
            PlanType.MONTHLY, MonthlyPlan::new,
            PlanType.ANNUAL, AnnualPlan::new,
            PlanType.STUDENT, StudentPlan::new,
            PlanType.OFF_PEAK, OffPeakPlan::new));

    public static Plan create(PlanType type) {
        Supplier<Plan> plan = PLANS.get(type);
        if (plan == null) {
            throw new IllegalArgumentException("No plan is registered for " + type);
        }
        return plan.get();
    }

    public static List<Plan> all() {
        return PLANS.keySet().stream().map(PlanFactory::create).toList();
    }
}
```

*The factory (`gym/domain/PlanFactory.java`, comments left out)*

```java
public class OffPeakPlan extends Plan {
    private static final LocalTime OPENS = LocalTime.of(10, 0);
    private static final LocalTime CLOSES = LocalTime.of(16, 0);

    @Override
    public boolean allowsCheckIn(LocalDateTime at) {
        DayOfWeek day = at.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        LocalTime time = at.toLocalTime();
        return weekday && !time.isBefore(OPENS) && time.isBefore(CLOSES);
    }
}

// in MembershipService.signUp
Plan plan = PlanFactory.create(type);
```

*One of the products, with only the rule it changes, and where the factory is used (`gym/domain/OffPeakPlan.java`, `gym/service/MembershipService.java`)*

```java
@Test
void createsThePlanClassForEachType() {
    assertInstanceOf(MonthlyPlan.class, PlanFactory.create(PlanType.MONTHLY));
    assertInstanceOf(AnnualPlan.class, PlanFactory.create(PlanType.ANNUAL));
    assertInstanceOf(StudentPlan.class, PlanFactory.create(PlanType.STUDENT));
    assertInstanceOf(OffPeakPlan.class, PlanFactory.create(PlanType.OFF_PEAK));
}

@Test
void offersEveryPlanType() {
    assertEquals(Arrays.asList(PlanType.values()),
            PlanFactory.all().stream().map(Plan::type).toList());
}
```

*The tests that prove the factory (`PlanFactoryTest`). The second one fails if a `PlanType` is added without its plan class*

## Automated Tests

We divided the testing strategy into 3 popular ones that most of the industry follows - unit, integration and end-to-end. JUnit 5 was used for all three, and the unit tests use in-memory fakes of ports like `InMemoryRepository`. All three strategies run in their own lane in CI/CD. Since 25 Oct, we also enforced that a new PR cannot be merged without having all testing CI lanes pass first.

Coverage of the tests is measured by JaCoCo (Java Code Coverage), and reported by its own CI job.

| Kind | Tests | Line coverage |
| --- | --- | --- |
| Unit | 128 | 76.5% |
| Integration | 21 | 38.5% |
| End-to-end | 14 | 72.0% |
| **All** | **163** | **96.3%** (897 of 931 lines) |

Table: Automated tests on main (CI run of 28 Oct)

## Version Control

We used the most popular version control system, Git, along with the most popular Git provider, GitHub. We enabled GitHub's own bot, Dependabot, to allow automatic version updates to our dependencies (2 of its pull requests already merged).

::: {.grid columns=2}
![Commits to main per week](../version-control/commits.png){width=100%}

![Pull requests merged into main over time](../version-control/pulls.png){width=100%}
:::
