package gym.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * BR-M4: no booking or check-in until the arrears are paid. BR-M6: cancelled after 60 days.
 */
public class LapsedState extends MembershipState {

    static final int DAYS_TO_CANCEL = 60;

    @Override
    public MembershipStatus status() {
        return MembershipStatus.LAPSED;
    }

    @Override
    public Membership payArrears(Membership membership) {
        return membership.withFailedPayments(0).withLapsedSince(null).moveTo(MembershipStatus.ACTIVE);
    }

    @Override
    public Membership cancel(Membership membership) {
        return membership.moveTo(MembershipStatus.CANCELLED);
    }

    @Override
    public Membership expire(Membership membership, LocalDate today) {
        long days = daysLapsed(membership, today);
        if (days < DAYS_TO_CANCEL) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " has been Lapsed for "
                    + days + " days. It expires after " + DAYS_TO_CANCEL);
        }
        return membership.moveTo(MembershipStatus.CANCELLED);
    }

    @Override
    public Membership checkOn(Membership membership, LocalDate today) {
        return daysLapsed(membership, today) >= DAYS_TO_CANCEL ? expire(membership, today) : membership;
    }

    /**
     * A membership stored as Lapsed without a date counts from today.
     */
    private static long daysLapsed(Membership membership, LocalDate today) {
        return membership.lapsedSince() == null ? 0 : ChronoUnit.DAYS.between(membership.lapsedSince(), today);
    }
}
