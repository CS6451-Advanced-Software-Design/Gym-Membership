package gym.domain;

import java.time.LocalDate;

/**
 * State pattern: what a {@link Membership} may do depends on where it is in its lifecycle. Each state
 * overrides only the operations it allows. The rest are refused here, so a frozen membership can't book a
 * class without any {@code if} on the status in Membership or the services.
 *
 * <p>The states keep no fields, so one instance of each is shared.
 */
public abstract class MembershipState {

    private static final TrialState TRIAL = new TrialState();
    private static final ActiveState ACTIVE = new ActiveState();
    private static final FrozenState FROZEN = new FrozenState();
    private static final LapsedState LAPSED = new LapsedState();
    private static final CancelledState CANCELLED = new CancelledState();

    public static MembershipState of(MembershipStatus status) {
        return switch (status) {
            case TRIAL -> TRIAL;
            case ACTIVE -> ACTIVE;
            case FROZEN -> FROZEN;
            case LAPSED -> LAPSED;
            case CANCELLED -> CANCELLED;
        };
    }

    public abstract MembershipStatus status();

    public boolean canBook(Membership membership) {
        return false;
    }

    public boolean canCheckIn(Membership membership) {
        return false;
    }

    /**
     * BR-B3: every state pays the fee, except a Trial.
     */
    public boolean paysLateCancellationFee(Membership membership) {
        return true;
    }

    public Membership recordBooking(Membership membership) {
        throw refused(membership, "book a class");
    }

    public Membership freeze(Membership membership, int months, LocalDate today) {
        throw refused(membership, "be frozen");
    }

    public Membership unfreeze(Membership membership) {
        throw refused(membership, "be unfrozen");
    }

    public Membership endTrial(Membership membership, LocalDate today) {
        throw refused(membership, "end a trial");
    }

    /**
     * Payments taken while a membership isn't billed change nothing.
     */
    public Membership recordPayment(Membership membership, boolean paid, LocalDate today) {
        return membership;
    }

    public Membership payArrears(Membership membership) {
        throw refused(membership, "pay arrears");
    }

    public Membership cancel(Membership membership) {
        throw refused(membership, "be cancelled");
    }

    public Membership expire(Membership membership, LocalDate today) {
        throw refused(membership, "expire");
    }

    /**
     * Nothing is due by default.
     */
    public Membership checkOn(Membership membership, LocalDate today) {
        return membership;
    }

    protected static RuleViolationException refused(Membership membership, String action) {
        String state = membership.status().name().charAt(0) + membership.status().name().substring(1).toLowerCase();
        return new RuleViolationException("Membership " + membership.membershipId() + " is " + state + ", so it can't " + action);
    }
}
