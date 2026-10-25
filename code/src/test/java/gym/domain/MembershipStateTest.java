package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * The State pattern: each test follows one arrow of the Membership state chart in report/analysis.
 */
class MembershipStateTest {

    private static final LocalDate JOINED = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime MONDAY_EVENING = LocalDateTime.of(2026, 11, 2, 19, 0);

    private static Membership trial() {
        return Membership.signUp("ms1", "m1", new MonthlyPlan(), JOINED);
    }

    private static Membership active() {
        return trial().endTrial(JOINED.plusDays(7));
    }

    private static Membership lapsed(LocalDate since) {
        return active().recordPayment(false, since).recordPayment(false, since);
    }

    @Test
    void eachStatusHasItsOwnStateObject() {
        assertInstanceOf(TrialState.class, trial().state());
        assertInstanceOf(ActiveState.class, active().state());
        assertInstanceOf(FrozenState.class, active().freeze(1, JOINED).state());
        assertInstanceOf(LapsedState.class, lapsed(JOINED).state());
        assertInstanceOf(CancelledState.class, active().cancel().state());
    }

    @Test
    void aMonthlyPlanStartsInTrialAndAnAnnualPlanStartsActive() {
        // BR-M2
        assertEquals(MembershipStatus.TRIAL, trial().status());
        assertEquals(MembershipStatus.ACTIVE, Membership.signUp("ms2", "m2", new AnnualPlan(), JOINED).status());
    }

    @Test
    void aTrialAllowsTwoBookings() {
        Membership twoBooked = trial().recordBooking().recordBooking();

        assertFalse(twoBooked.canBook());
        assertThrows(RuleViolationException.class, twoBooked::recordBooking);
    }

    @Test
    void aTrialEndsActiveWhenTheFirstChargeWasPaid() {
        Membership paid = trial().recordPayment(true, JOINED);

        assertEquals(MembershipStatus.ACTIVE, paid.endTrial(JOINED.plusDays(7)).status());
    }

    @Test
    void aTrialEndsLapsedWhenTheFirstChargeWasDeclined() {
        Membership declined = trial().recordPayment(false, JOINED);

        Membership ended = declined.endTrial(JOINED.plusDays(7));

        assertEquals(MembershipStatus.LAPSED, ended.status());
        assertEquals(JOINED.plusDays(7), ended.lapsedSince());
    }

    @Test
    void theClockEndsATrialAfterSevenDaysAndNotBefore() {
        assertEquals(MembershipStatus.TRIAL, trial().checkOn(JOINED.plusDays(6)).status());
        assertEquals(MembershipStatus.ACTIVE, trial().checkOn(JOINED.plusDays(7)).status());
    }

    @Test
    void aTrialCannotBeFrozen() {
        RuleViolationException refused = assertThrows(RuleViolationException.class, () -> trial().freeze(1, JOINED));

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
        assertEquals(JOINED.plusMonths(3), frozen.currentFreeze().endDate());
    }

    @Test
    void aFreezeLastsOneToThreeMonths() {
        assertThrows(RuleViolationException.class, () -> active().freeze(0, JOINED));
        assertThrows(RuleViolationException.class, () -> active().freeze(4, JOINED));
    }

    @Test
    void aMembershipCanBeFrozenOnceInTwelveMonths() {
        LocalDate firstFreeze = JOINED.plusDays(10);
        Membership thawed = active().freeze(1, firstFreeze).unfreeze();

        assertThrows(RuleViolationException.class, () -> thawed.freeze(1, firstFreeze.plusMonths(11)));
        assertEquals(MembershipStatus.FROZEN, thawed.freeze(1, firstFreeze.plusMonths(12)).status());
    }

    @Test
    void theClockEndsAFreezeWhenItsMonthsAreUp() {
        Membership frozen = active().freeze(2, JOINED);

        assertEquals(MembershipStatus.FROZEN, frozen.checkOn(JOINED.plusMonths(2).minusDays(1)).status());
        assertEquals(MembershipStatus.ACTIVE, frozen.checkOn(JOINED.plusMonths(2)).status());
    }

    @Test
    void oneDeclinedPaymentIsForgivenButTwoInARowLapse() {
        // BR-M4
        Membership once = active().recordPayment(false, JOINED);
        assertEquals(MembershipStatus.ACTIVE, once.status());
        assertEquals(0, once.recordPayment(true, JOINED).failedPayments());

        Membership twice = once.recordPayment(false, JOINED.plusMonths(1));
        assertEquals(MembershipStatus.LAPSED, twice.status());
        assertFalse(twice.canBook());
        assertFalse(twice.canCheckIn(MONDAY_EVENING));
    }

    @Test
    void payingArrearsRestoresALapsedMembership() {
        Membership restored = lapsed(JOINED).payArrears();

        assertEquals(MembershipStatus.ACTIVE, restored.status());
        assertEquals(0, restored.failedPayments());
        assertNull(restored.lapsedSince());
    }

    @Test
    void aLapsedMembershipExpiresAfterSixtyDays() {
        // BR-M6
        Membership lapsed = lapsed(JOINED);

        assertThrows(RuleViolationException.class, () -> lapsed.expire(JOINED.plusDays(59)));
        assertEquals(MembershipStatus.LAPSED, lapsed.checkOn(JOINED.plusDays(59)).status());
        assertEquals(MembershipStatus.CANCELLED, lapsed.checkOn(JOINED.plusDays(60)).status());
    }

    @Test
    void everyStateButCancelledCanBeCancelled() {
        assertEquals(MembershipStatus.CANCELLED, trial().cancel().status());
        assertEquals(MembershipStatus.CANCELLED, active().cancel().status());
        assertEquals(MembershipStatus.CANCELLED, active().freeze(1, JOINED).cancel().status());
        assertEquals(MembershipStatus.CANCELLED, lapsed(JOINED).cancel().status());
        assertThrows(RuleViolationException.class, () -> active().cancel().cancel());
    }

    @Test
    void aCancelledMembershipRefusesEverything() {
        Membership cancelled = active().cancel();

        assertFalse(cancelled.canBook());
        assertFalse(cancelled.canCheckIn(MONDAY_EVENING));
        assertThrows(RuleViolationException.class, () -> cancelled.freeze(1, JOINED));
        assertThrows(RuleViolationException.class, cancelled::payArrears);
        assertEquals(cancelled, cancelled.checkOn(JOINED.plusYears(1)));
    }

    @Test
    void checkInAlsoDependsOnThePlan() {
        Membership offPeak = Membership.signUp("ms3", "m3", new OffPeakPlan(), JOINED);

        assertTrue(offPeak.canCheckIn(LocalDateTime.of(2026, 11, 3, 11, 0)));
        assertFalse(offPeak.canCheckIn(MONDAY_EVENING));
    }
}
