package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.BestDiscount;
import gym.domain.ChargeType;
import gym.domain.CorporateDiscount;
import gym.domain.FamilyDiscount;
import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.LoyaltyDiscount;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Money;
import gym.domain.Notification;
import gym.domain.PlanType;
import gym.domain.RuleViolationException;
import gym.port.in.CancelMembershipUseCase.Cancellation;
import gym.port.in.SignUpUseCase.Applicant;
import gym.port.in.SignUpUseCase.SignUp;
import gym.service.BillingServiceTest.InMemoryChargeRepository;
import gym.service.BillingServiceTest.InMemoryInvoiceRepository;
import gym.service.BillingServiceTest.InMemoryMemberRepository;
import gym.service.BillingServiceTest.InMemoryMembershipRepository;
import gym.support.TestClock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The Membership use cases, with Billing's real service behind them and in-memory repositories.
 */
class MembershipServiceTest {

    private static final LocalDate JOINED = LocalDate.of(2026, 11, 2);

    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final InMemoryMemberRepository members = new InMemoryMemberRepository();
    private final InMemoryChargeRepository charges = new InMemoryChargeRepository();
    private final Set<String> declinedEmails = new HashSet<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final TestClock clock = new TestClock(JOINED);

    private final BillingService billing = new BillingService(
            memberships,
            members,
            charges,
            new InMemoryInvoiceRepository(),
            new BestDiscount(List.of(new CorporateDiscount(), new FamilyDiscount(), new LoyaltyDiscount())),
            (memberId, amount) -> members.findById(memberId).map(member -> !declinedEmails.contains(member.email())).orElseThrow(),
            notifications::add,
            clock);

    private final MembershipService service = new MembershipService(
            memberships, members, billing, billing, billing, billing, notifications::add, clock);

    @Test
    void signingUpStartsATrialAndChargesTheRestOfTheMonth() {
        // BR-M2 and BR-P4: joining on 2 Nov leaves 29 of 30 days, so 29/30 of €45 = €43.50
        SignUp signUp = service.signUp(applicant("ann@example.com"), PlanType.MONTHLY, null);

        assertEquals(MembershipStatus.TRIAL, signUp.membership().status());
        assertEquals(Money.euros("43.50"), signUp.firstInvoice().total());
        assertEquals(InvoiceStatus.PAID, signUp.firstInvoice().status());
        assertTrue(members.findById(signUp.membership().memberId()).isPresent());
        assertEquals("Welcome", notifications.getLast().subject());
    }

    @Test
    void anAnnualPlanStartsActive() {
        SignUp signUp = service.signUp(applicant("ann@example.com"), PlanType.ANNUAL, null);

        assertEquals(MembershipStatus.ACTIVE, signUp.membership().status());
        assertEquals(JOINED.plusYears(1), signUp.membership().endDate());
        assertEquals(Money.euros(450), signUp.firstInvoice().total());
    }

    @Test
    void theStudentPlanIsRefusedWithoutAStudentIdAndNothingIsStored() {
        assertThrows(RuleViolationException.class, () -> service.signUp(applicant("cara@example.com"), PlanType.STUDENT, null));

        assertTrue(members.findAll().isEmpty());
        assertTrue(memberships.findAll().isEmpty());
    }

    @Test
    void nobodyHoldsTwoMembershipsButSomeoneWhoLeftCanComeBack() {
        Membership first = service.signUp(applicant("ann@example.com"), PlanType.MONTHLY, null).membership();

        assertThrows(RuleViolationException.class, () -> service.signUp(applicant("ANN@example.com"), PlanType.ANNUAL, null));

        service.cancel(first.membershipId());
        Membership second = service.signUp(applicant("ann@example.com"), PlanType.ANNUAL, null).membership();
        assertEquals(first.memberId(), second.memberId());
    }

    @Test
    void aReferralCreditsTheReferrer() {
        // BR-P3: €20 credit waiting for the referrer's next invoice
        Membership referrer = service.signUp(applicant("ann@example.com"), PlanType.MONTHLY, null).membership();

        service.signUp(applicant("bob@example.com"), PlanType.MONTHLY, referrer.membershipId());

        assertTrue(charges.findAll().stream().anyMatch(charge -> charge.membershipId().equals(referrer.membershipId())
                && charge.type() == ChargeType.REFERRAL_CREDIT));
    }

    @Test
    void anUnknownReferralCodeIsRefused() {
        assertThrows(RuleViolationException.class, () -> service.signUp(applicant("bob@example.com"), PlanType.MONTHLY, "ms-nobody"));
    }

    @Test
    void aDeclinedFirstChargeEndsTheTrialLapsed() {
        Membership trial = signUpDeclined("eve@example.com");
        assertEquals(1, trial.failedPayments());

        clock.advanceDays(7);
        List<Membership> changed = service.runDailyChecks();

        assertEquals(List.of(MembershipStatus.LAPSED), changed.stream().map(Membership::status).toList());
    }

    @Test
    void theDailyChecksEndAPaidTrialActiveAndChangeNothingWhenRunAgain() {
        Membership trial = service.signUp(applicant("ann@example.com"), PlanType.MONTHLY, null).membership();

        clock.advanceDays(7);
        service.runDailyChecks();

        assertEquals(MembershipStatus.ACTIVE, service.membership(trial.membershipId()).status());
        assertEquals(List.of(), service.runDailyChecks());
    }

    @Test
    void twoUnpaidInvoicesInARowLapseAnActiveMembership() {
        // BR-M4: the first charge is paid, then December's and January's are declined
        Membership membership = service.signUp(applicant("dan@example.com"), PlanType.MONTHLY, null).membership();
        clock.advanceDays(7);
        service.runDailyChecks();
        declinedEmails.add("dan@example.com");

        billing.run(YearMonth.of(2026, 12));
        service.runDailyChecks();
        assertEquals(MembershipStatus.ACTIVE, service.membership(membership.membershipId()).status());

        billing.run(YearMonth.of(2027, 1));
        service.runDailyChecks();
        assertEquals(MembershipStatus.LAPSED, service.membership(membership.membershipId()).status());
    }

    @Test
    void payingTheArrearsRestoresALapsedMembership() {
        Membership trial = signUpDeclined("eve@example.com");
        clock.advanceDays(7);
        service.runDailyChecks();

        declinedEmails.clear();
        Membership restored = service.payArrears(trial.membershipId());

        assertEquals(MembershipStatus.ACTIVE, restored.status());
        assertTrue(billing.invoicesFor(trial.membershipId()).stream().allMatch(invoice -> invoice.status() == InvoiceStatus.PAID));
    }

    @Test
    void aDeclinedArrearsPaymentLeavesTheMembershipLapsed() {
        Membership trial = signUpDeclined("eve@example.com");
        clock.advanceDays(7);
        service.runDailyChecks();

        assertThrows(RuleViolationException.class, () -> service.payArrears(trial.membershipId()));
        assertEquals(MembershipStatus.LAPSED, service.membership(trial.membershipId()).status());
    }

    @Test
    void aMembershipLapsedForSixtyDaysIsCancelledWithAFinalBill() {
        // BR-M6
        Membership trial = signUpDeclined("eve@example.com");
        clock.advanceDays(7);
        service.runDailyChecks();
        int invoicesBefore = billing.invoicesFor(trial.membershipId()).size();

        clock.advanceDays(60);
        service.runDailyChecks();

        assertEquals(MembershipStatus.CANCELLED, service.membership(trial.membershipId()).status());
        assertEquals(invoicesBefore + 1, billing.invoicesFor(trial.membershipId()).size());
    }

    @Test
    void aFreezeIsStoredAndEndedByTheDailyChecks() {
        Membership annual = service.signUp(applicant("ann@example.com"), PlanType.ANNUAL, null).membership();

        Membership frozen = service.freeze(annual.membershipId(), 1);
        assertEquals(MembershipStatus.FROZEN, service.membership(annual.membershipId()).status());
        assertEquals(JOINED.plusMonths(1), frozen.currentFreeze().endDate());

        clock.set(JOINED.plusMonths(1));
        service.runDailyChecks();
        assertEquals(MembershipStatus.ACTIVE, service.membership(annual.membershipId()).status());
    }

    @Test
    void cancellingAnAnnualPlanEarlyCostsFiftyEuro() {
        // BR-M5
        Membership annual = service.signUp(applicant("ann@example.com"), PlanType.ANNUAL, null).membership();
        clock.advanceDays(30);

        Cancellation cancellation = service.cancel(annual.membershipId());

        assertEquals(MembershipStatus.CANCELLED, cancellation.membership().status());
        assertEquals(Money.euros(50), cancellation.finalInvoice().total());
        assertThrows(RuleViolationException.class, () -> service.cancel(annual.membershipId()));
    }

    @Test
    void aRefusedOperationLeavesNothingBilled() {
        Membership trial = service.signUp(applicant("ann@example.com"), PlanType.MONTHLY, null).membership();
        service.cancel(trial.membershipId());
        List<Invoice> before = billing.invoicesFor(trial.membershipId());

        assertThrows(RuleViolationException.class, () -> service.cancel(trial.membershipId()));
        assertEquals(before, billing.invoicesFor(trial.membershipId()));
    }

    private Membership signUpDeclined(String email) {
        declinedEmails.add(email);
        return service.signUp(applicant(email), PlanType.MONTHLY, null).membership();
    }

    private static Applicant applicant(String email) {
        return new Applicant("Someone", email, null, null, null);
    }
}
