package gym.domain;

import java.time.LocalDate;

public class ActiveState extends MembershipState {

    static final int MIN_FREEZE_MONTHS = 1;
    static final int MAX_FREEZE_MONTHS = 3;
    static final int FAILED_PAYMENTS_TO_LAPSE = 2;

    @Override
    public MembershipStatus status() {
        return MembershipStatus.ACTIVE;
    }

    @Override
    public boolean canBook(Membership membership) {
        return true;
    }

    @Override
    public boolean canCheckIn(Membership membership) {
        return true;
    }

    @Override
    public Membership recordBooking(Membership membership) {
        return membership;
    }

    /**
     * BR-M3: 1 to 3 months, at most once in any 12 months.
     */
    @Override
    public Membership freeze(Membership membership, int months, LocalDate today) {
        if (months < MIN_FREEZE_MONTHS || months > MAX_FREEZE_MONTHS) {
            throw new RuleViolationException("A freeze lasts 1 to 3 months, not " + months);
        }
        for (Freeze earlier : membership.freezes()) {
            if (earlier.startDate().isAfter(today.minusMonths(12))) {
                throw new RuleViolationException("Membership " + membership.membershipId() + " was already frozen on "
                        + earlier.startDate() + ". It can be frozen once in 12 months");
            }
        }
        return membership.moveTo(MembershipStatus.FROZEN).withFreeze(new Freeze(today, months));
    }

    /**
     * BR-M4: the second declined payment in a row makes it Lapsed.
     */
    @Override
    public Membership recordPayment(Membership membership, boolean paid, LocalDate today) {
        if (paid) {
            return membership.withFailedPayments(0);
        }
        int failed = membership.failedPayments() + 1;
        if (failed >= FAILED_PAYMENTS_TO_LAPSE) {
            return membership.withFailedPayments(failed).moveTo(MembershipStatus.LAPSED).withLapsedSince(today);
        }
        return membership.withFailedPayments(failed);
    }

    @Override
    public Membership payArrears(Membership membership) {
        return membership.withFailedPayments(0);
    }

    /**
     * BR-M5's early-exit fee is added by Billing's final bill.
     */
    @Override
    public Membership cancel(Membership membership) {
        return membership.moveTo(MembershipStatus.CANCELLED);
    }
}
