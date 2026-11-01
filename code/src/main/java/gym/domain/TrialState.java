package gym.domain;

import java.time.LocalDate;

/**
 * BR-M2: 7 days with 2 class bookings. Ends Active if the first charge was paid, Lapsed if not.
 */
public class TrialState extends MembershipState {

    @Override
    public MembershipStatus status() {
        return MembershipStatus.TRIAL;
    }

    @Override
    public boolean canBook(Membership membership) {
        return membership.trialBookingsUsed() < Membership.TRIAL_BOOKINGS;
    }

    @Override
    public boolean canCheckIn(Membership membership) {
        return true;
    }

    @Override
    public boolean paysLateCancellationFee(Membership membership) {
        return false;
    }

    @Override
    public Membership recordBooking(Membership membership) {
        if (!canBook(membership)) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " has used the "
                    + Membership.TRIAL_BOOKINGS + " bookings of its trial");
        }
        return membership.withTrialBookingsUsed(membership.trialBookingsUsed() + 1);
    }

    @Override
    public Membership endTrial(Membership membership, LocalDate today) {
        if (membership.failedPayments() > 0) {
            return membership.moveTo(MembershipStatus.LAPSED).withLapsedSince(today);
        }
        return membership.moveTo(MembershipStatus.ACTIVE);
    }

    /**
     * The first charge: a declined one is remembered, so the trial ends Lapsed.
     */
    @Override
    public Membership recordPayment(Membership membership, boolean paid, LocalDate today) {
        return membership.withFailedPayments(paid ? 0 : membership.failedPayments() + 1);
    }

    /**
     * Paying a declined first charge during the trial lets it end Active.
     */
    @Override
    public Membership payArrears(Membership membership) {
        return membership.withFailedPayments(0);
    }

    @Override
    public Membership cancel(Membership membership) {
        return membership.moveTo(MembershipStatus.CANCELLED);
    }

    @Override
    public Membership checkOn(Membership membership, LocalDate today) {
        return today.isBefore(membership.trialEndsOn()) ? membership : endTrial(membership, today);
    }
}
