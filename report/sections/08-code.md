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
| UC2 Book Class | `POST /classes/{classId}/bookings` | BookingCommands, BookClass | Booking­Command­Service (Command), BookingService | GymClass, Booking, Waitlist, Membership (`canBook`) |
| UC3 Cancel Booking | `POST /bookings/{bookingId}/cancel`, `POST /members/{memberId}/bookings/undo` | BookingCommands, CancelBooking, UndoBooking | Booking­Command­Service (Command), BookingService | Booking, Waitlist, Charge |
| UC4 Check In | `POST /members/{memberId}/check-ins` | CheckIn | MemberService | Member, Membership (`canCheckIn`), CheckIn |
| UC5 Freeze / Unfreeze | `POST …/freeze`, `…/unfreeze` | FreezeMembership | MembershipService | Membership, Freeze, FrozenState |
| UC6 Cancel Membership | `POST …/cancel` | CancelMembership | MembershipService | Membership, CancelledState; Billing's final invoice and early-exit fee |
| UC7 Run Monthly Billing | BillingJob (monthly), `POST /billing-runs` | RunMonthlyBilling | BillingService | BestDiscount (Strategy), Charge, Invoice |
| UC8 Record Attendance | `POST /bookings/{bookingId}/attended`, `…/no-show` | RecordAttendance | AttendanceService | Booking, NoShows, Charge |
| UC10 Manage Class Schedule | `GET /classes`, `POST /classes` | ManageSchedule | ScheduleService | GymClass |
| UC11 View Invoices | `GET …/invoices` | ViewInvoices | BillingService | Invoice, Charge |
| UC13 Daily Checks | MembershipChecksJob (daily), `POST /membership-checks` | RunDailyChecks | Membership­Daily­Checks­Service | TrialState, FrozenState, LapsedState |
| UC14 Pay Arrears | `POST …/pay-arrears` | RestoreMembership, PayArrears | MembershipService, BillingService | LapsedState, Invoice |

Table: Each use case built so far, from where it enters to the classes that carry it out

<!-- UC9 and UC12 (plans and prices, reports) aren't built yet. -->


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

### Observer

We used Observer Pattern which is basically a way to say "tell whoever is interested" without knowing who that is. The main reason for observer is that BookingService should not know how a member is told. We used it in our project for future feature addition, like a new Listener Classes - Email, SMS, etc.

1. When user cancel booking, it saves the cancelled booking and then calls the promoteFromWaitlist
2. If someone is waiting and can still book the class, they get a place saved first, and then a message sent and `EventNotificationSender` publish the Notification as an event.
3. The publisher does not know who the listener is, so future examples like email, sms can be added without changing the booking code. For now only `NotificationLog` exists which writes to the log

```java
// BookingService.promoteFromWaitlist
if (next.isPresent()) {
    classes.save(remaining);
    takePlace(next.get(), gymClass, now);
    // Sent last, so a member is only told once the place is really theirs
    notifications.send(gymClass.promotionNotice(memberId));
    return;
}

@Component
public class EventNotificationSender implements NotificationSender {
    @Override
    public void send(Notification notification) {
        events.publishEvent(notification);
    }
}

@Component
public class NotificationLog {
    @EventListener
    public void deliver(Notification notification) {
        LOGGER.info("notify member {}: {} - {}", notification.memberId(), notification.subject(), notification.message());
    }
}
```

*The code shows after place is saved (promotion), notification is sent, publisher (`EventNotificationSender`) publishes event (showed only key lines, not whole class) (`gym/service`, `gym/adapter/notification`)*

```java
@Test
void aPromotedMemberIsToldAboutTheirPlace() {
    // BR-B2
    spinClass(1);
    activeMembership("m1");
    activeMembership("m2");
    Booking booking = service.book("m1", "c1").booking();
    service.book("m2", "c1");

    service.cancel(booking.bookingId());

    assertEquals(1, notifications.size());
    Notification notice = notifications.getFirst();
    assertEquals("m2", notice.memberId());
    assertEquals("You have a place in Spin", notice.subject());
    assertTrue(notice.message().contains("2026-11-06 at 18:00"));
}
```

*The test that proves m1 has the only place and m2 waits. m1 cancels, so m2 gets place and exactly one notification addressed to m2 (`BookingServiceTest`)*

### Command

Command is a Design Pattern in which an action is stored as a small object so that future actions can be taken such as undo. It is used when a member book a class and cancels a booking, then perform an undo action, so to remember the members last action we use the command pattern. It helps to remember the member past action so undo functionality can be performed when required

1. When member cancels a Booking, BookingController gets triggered, which calls  the BookingCommandService (invoker) which stores and keeps the command in the member's history which only after command has run
2. BookingCommandService creates and run the BookClassCommand or CancelBookingCommand (concrete class) which has the functionality to invoke method acc.
3. The concrete class then finally calls BookingService which then does the real work acc. to use cases.
4. When member presses undo it does one of the two things, "undo a booking" - gives up the place or leaves the waiting list. "undo a cancel" - gives the same place back if the place is free, otherwise member from waiting list gets promoted when its canceled.

```java
interface BookingCommand<R> {
    R execute();
    void undo();
}

class CancelBookingCommand implements BookingCommand<Booking> {
    @Override
    public Booking execute() {
        return cancelling.cancel(bookingId);
    }

    @Override
    public void undo() {
        undoing.restoreBooking(bookingId);
    }
}

@Service
public class BookingCommandService implements BookingCommandsUseCase {
    private final Map<String, Deque<BookingCommand<?>>> histories = new HashMap<>();

    @Override
    public synchronized void undoLast(String memberId) {
        Deque<BookingCommand<?>> history = histories.getOrDefault(memberId, new ArrayDeque<>());
        if (history.isEmpty()) {
            throw new RuleViolationException("Member " + memberId + " has nothing to undo");
        }
        history.peek().undo();
        history.pop();
    }
}
```
*CancelBookingCommand performs cancel action - execute and undo, histories hold one pile per member, 'nothing to undo' message on pile empty. Undo goes one step at a time, newest first (`gym/service`, only key lines)*

```java
@Test
void undoGoesBackOneActionAtATime() {
    spinClass(10);
    activeMembership("m1");
    Booking booking = commands.book("m1", "c1").booking();
    commands.cancel(booking.bookingId());

    commands.undoLast("m1");
    assertEquals(BookingStatus.BOOKED, statusOf(booking));

    commands.undoLast("m1");
    assertEquals(BookingStatus.CANCELLED, statusOf(booking));
    assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
}
```
*m1 books and then cancel, first undo - takes back the cancel so booking is BOOKED again, second undo takes back booking, status CANCELLED, third undo is refused, due to nothing left to undo (`BookingCommandServiceTest`)*


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

What a membership may do depends on its state: a Trial can book only 2 classes, an Active one can be frozen, and a Frozen or Lapsed one can't book or check in. Each state is a stateless subclass that overrides only what it allows; everything else is refused once, in the base class, with a readable reason (a 422 over REST). `Membership` just hands each call to `state()`, so there is no `switch` on the status outside `of`.

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

Each plan is its own class with its own rules (price, trial, end date, access hours, eligibility). `PlanFactory` is the only class that maps a `PlanType` to its class; `MembershipService.signUp` calls `PlanFactory.create(type)` and only ever sees the abstract `Plan`. Adding a plan is one subclass, one `PlanType` value and one map entry, with no change to `MembershipService`, Billing or Booking.

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
| Unit | 254 | 81.5% |
| Integration | 23 | 37.1% |
| End-to-end | 23 | 81.5% |
| **All** | **300** | **97.3%** (1164 of 1196 lines) |

Table: Automated tests on main (CI run of 2 Nov)

## Version Control

We used the most popular version control system, Git, along with the most popular Git provider, GitHub. We enabled GitHub's own bot, Dependabot, to allow automatic version updates to our dependencies (2 of its pull requests already merged).

::: {.grid columns=2}
![Commits to main per week](../version-control/commits.png){width=100%}

![Pull requests merged into main over time](../version-control/pulls.png){width=100%}
:::
