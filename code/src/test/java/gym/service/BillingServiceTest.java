package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.BestDiscount;
import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.CorporateDiscount;
import gym.domain.FamilyDiscount;
import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.LoyaltyDiscount;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Money;
import gym.domain.NotFoundException;
import gym.domain.Notification;
import gym.domain.PlanType;
import gym.domain.RuleViolationException;
import gym.port.out.ChargeRepository;
import gym.port.out.InvoiceRepository;
import gym.port.out.MemberRepository;
import gym.port.out.MembershipRepository;
import gym.support.InMemoryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BillingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    static class InMemoryMembershipRepository extends InMemoryRepository<Membership, String> implements MembershipRepository {
        InMemoryMembershipRepository() {
            super(Membership::membershipId);
        }
    }

    static class InMemoryMemberRepository extends InMemoryRepository<Member, String> implements MemberRepository {
        InMemoryMemberRepository() {
            super(Member::memberId);
        }
    }

    static class InMemoryChargeRepository extends InMemoryRepository<Charge, String> implements ChargeRepository {
        InMemoryChargeRepository() {
            super(Charge::id);
        }
    }

    static class InMemoryInvoiceRepository extends InMemoryRepository<Invoice, String> implements InvoiceRepository {
        InMemoryInvoiceRepository() {
            super(Invoice::id);
        }
    }

    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final InMemoryMemberRepository members = new InMemoryMemberRepository();
    private final InMemoryChargeRepository charges = new InMemoryChargeRepository();
    private final List<Money> payments = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final Set<String> declinedMembers = new HashSet<>();

    private final BillingService billing = new BillingService(
            memberships,
            members,
            charges,
            new InMemoryInvoiceRepository(),
            new BestDiscount(List.of(new CorporateDiscount(), new FamilyDiscount(), new LoyaltyDiscount())),
            (memberId, amount) -> {
                if (declinedMembers.contains(memberId)) {
                    return false;
                }
                payments.add(amount);
                return true;
            },
            notifications::add,
            Clock.fixed(Instant.parse("2026-11-01T09:00:00Z"), ZoneOffset.UTC));

    @Test
    void theFirstMonthIsProRatedByTheDaysLeft() {
        // BR-P4: joining on 20 Nov leaves 11 of 30 days, so 11/30 of €45 = €16.50
        join("ms1", PlanType.MONTHLY, TODAY);

        Invoice invoice = billing.billFirstMonth("ms1", LocalDate.of(2026, 11, 20));

        assertEquals(Money.euros("16.50"), invoice.total());
        assertEquals(InvoiceStatus.PAID, invoice.status());
        assertEquals(List.of(Money.euros("16.50")), payments);
    }

    @Test
    void theFirstMonthTakesTheBestDiscount() {
        // UC1 step 4: a full month at €45, minus the 15% corporate discount
        members.save(new Member("m1", "Ann", "ann@example.com", null, "Stripe", null));
        memberships.save(new Membership("ms1", "m1", PlanType.MONTHLY, TODAY, MembershipStatus.TRIAL));

        assertEquals(Money.euros("38.25"), billing.billFirstMonth("ms1", TODAY).total());
    }

    @Test
    void anAnnualPlanIsPaidUpfront() {
        // BR-M1: €450 for the year, not pro-rated
        join("ms1", PlanType.ANNUAL, TODAY);

        assertEquals(Money.euros(450), billing.billFirstMonth("ms1", LocalDate.of(2026, 11, 20)).total());
    }

    @Test
    void theMonthlyRunAddsPenaltiesAndTakesOffCredits() {
        // BR-P1: €45 plan fee + €5 no-show + €8 surcharge - €20 referral credit = €38
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        billing.record("ms1", ChargeType.NO_SHOW, Money.euros(5));
        billing.record("ms1", ChargeType.PREMIUM_SURCHARGE, Money.euros(8));
        billing.creditReferral("ms1");

        List<Invoice> issued = billing.run(NOVEMBER);

        assertEquals(1, issued.size());
        assertEquals(Money.euros(38), issued.getFirst().total());
        assertEquals(4, issued.getFirst().charges().size());
        assertTrue(charges.findAll().stream().allMatch(Charge::isBilled));
    }

    @Test
    void theSecondFamilyMemberGetsTheFamilyDiscount() {
        // BR-P2: Cara joined after her mother, so she gets 10% off and her mother doesn't
        members.save(new Member("mum", "Mary", "mary@example.com", null, null, "walsh"));
        members.save(new Member("cara", "Cara", "cara@example.com", null, null, "walsh"));
        memberships.save(new Membership("ms-mum", "mum", PlanType.MONTHLY, TODAY.minusMonths(6), MembershipStatus.ACTIVE));
        memberships.save(new Membership("ms-cara", "cara", PlanType.MONTHLY, TODAY.minusMonths(2), MembershipStatus.ACTIVE));

        List<Invoice> issued = billing.run(NOVEMBER);

        assertEquals(Money.euros(45), issued.get(0).total());
        assertEquals(Money.euros("40.50"), issued.get(1).total());
    }

    @Test
    void frozenAndCancelledMembershipsAreNotBilled() {
        // BR-M3
        join("ms1", PlanType.MONTHLY, TODAY.minusYears(1), MembershipStatus.FROZEN);
        join("ms2", PlanType.MONTHLY, TODAY.minusYears(1), MembershipStatus.CANCELLED);

        assertTrue(billing.run(NOVEMBER).isEmpty());
        assertTrue(payments.isEmpty());
    }

    @Test
    void runningTheSameMonthTwiceBillsOnce() {
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));

        billing.run(NOVEMBER);
        List<Invoice> second = billing.run(NOVEMBER);

        assertTrue(second.isEmpty());
        assertEquals(List.of(Money.euros(45)), payments);
    }

    @Test
    void anAnnualMemberIsOnlyBilledMonthlyForPenalties() {
        join("ms1", PlanType.ANNUAL, TODAY.minusYears(1));
        join("ms2", PlanType.ANNUAL, TODAY.minusYears(1));
        billing.record("ms1", ChargeType.LATE_CANCELLATION, Money.euros(5));

        List<Invoice> issued = billing.run(NOVEMBER);

        assertEquals(1, issued.size());
        assertEquals(Money.euros(5), issued.getFirst().total());
    }

    @Test
    void aDeclinedPaymentLeavesTheInvoiceUnpaidAndTellsTheMember() {
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        declinedMembers.add("member-ms1");

        Invoice invoice = billing.run(NOVEMBER).getFirst();

        assertEquals(InvoiceStatus.UNPAID, invoice.status());
        assertEquals("member-ms1", notifications.getFirst().memberId());
        assertEquals("Payment declined", notifications.getFirst().subject());
    }

    @Test
    void referralCreditsAreCappedAt60EurosAYear() {
        // BR-P3: €20 per referral, at most €60 per year
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));

        List<Money> credits = List.of(
                billing.creditReferral("ms1"),
                billing.creditReferral("ms1"),
                billing.creditReferral("ms1"),
                billing.creditReferral("ms1"));

        assertEquals(List.of(Money.euros(20), Money.euros(20), Money.euros(20), Money.ZERO), credits);
    }

    @Test
    void aCreditThatDoesNotFitTheBillWaitsForTheNextOne() {
        // Three €20 credits against a €45 bill: two fit (€5 left to pay), the third waits for December
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        billing.creditReferral("ms1");
        billing.creditReferral("ms1");
        billing.creditReferral("ms1");

        Invoice november = billing.run(NOVEMBER).getFirst();
        Invoice december = billing.run(NOVEMBER.plusMonths(1)).getFirst();

        assertEquals(Money.euros(5), november.total());
        assertEquals(Money.euros(25), december.total());
    }

    @Test
    void cancellingAnAnnualPlanWithinSixMonthsCostsTheEarlyExitFee() {
        // BR-M5: €50 early-exit fee plus the €5 no-show still owed
        join("ms1", PlanType.ANNUAL, TODAY.minusMonths(5));
        billing.record("ms1", ChargeType.NO_SHOW, Money.euros(5));

        assertEquals(Money.euros(55), billing.billFinal("ms1").total());
    }

    @Test
    void cancellingAnAnnualPlanAfterSixMonthsHasNoFee() {
        join("ms1", PlanType.ANNUAL, TODAY.minusMonths(6));

        assertEquals(Money.ZERO, billing.billFinal("ms1").total());
    }

    @Test
    void payingArrearsSettlesEveryUnpaidInvoiceInOnePayment() {
        // UC14, BR-M4: two declined months are paid off together
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        declinedMembers.add("member-ms1");
        billing.run(NOVEMBER);
        billing.run(NOVEMBER.plusMonths(1));
        declinedMembers.clear();

        List<Invoice> paid = billing.payArrears("ms1");

        assertEquals(2, paid.size());
        assertEquals(List.of(Money.euros(90)), payments);
        assertTrue(billing.invoicesFor("ms1").stream().allMatch(invoice -> invoice.status() == InvoiceStatus.PAID));
    }

    @Test
    void aDeclinedArrearsPaymentLeavesEveryInvoiceUnpaid() {
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        declinedMembers.add("member-ms1");
        billing.run(NOVEMBER);

        assertTrue(billing.payArrears("ms1").isEmpty());
        assertEquals(InvoiceStatus.UNPAID, billing.invoicesFor("ms1").getFirst().status());
    }

    @Test
    void thereAreNoArrearsWhenEveryInvoiceIsPaid() {
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        billing.run(NOVEMBER);

        assertThrows(RuleViolationException.class, () -> billing.payArrears("ms1"));
    }

    @Test
    void onlyPositiveFeesCanBeRecorded() {
        join("ms1", PlanType.MONTHLY, TODAY);

        assertThrows(RuleViolationException.class, () -> billing.record("ms1", ChargeType.NO_SHOW, Money.ZERO));
        assertThrows(RuleViolationException.class, () -> billing.record("ms1", ChargeType.REFERRAL_CREDIT, Money.euros(20)));
    }

    @Test
    void anUnknownMembershipIsNotFound() {
        assertThrows(NotFoundException.class, () -> billing.invoicesFor("nobody"));
    }

    @Test
    void invoicesAreListedNewestMonthFirst() {
        join("ms1", PlanType.MONTHLY, TODAY.minusMonths(3));
        billing.run(NOVEMBER);
        billing.run(NOVEMBER.plusMonths(1));

        List<YearMonth> periods = billing.invoicesFor("ms1").stream().map(Invoice::period).toList();

        assertEquals(List.of(NOVEMBER.plusMonths(1), NOVEMBER), periods);
    }

    private void join(String membershipId, PlanType plan, LocalDate since) {
        join(membershipId, plan, since, MembershipStatus.ACTIVE);
    }

    /**
     * A member with no discounts, whose id is "member-" plus the membership id.
     */
    private void join(String membershipId, PlanType plan, LocalDate since, MembershipStatus status) {
        String memberId = "member-" + membershipId;
        members.save(new Member(memberId, "Test member", memberId + "@example.com", null, null, null));
        memberships.save(new Membership(membershipId, memberId, plan, since, status));
    }
}
