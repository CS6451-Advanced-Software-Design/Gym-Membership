package gym.domain;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory pattern: turns a {@link MembershipStatus} into the shared {@link MembershipState} singleton that
 * knows its rules, the same role {@link PlanFactory} plays for {@link Plan}. Keeps {@link MembershipState}
 * itself free of static references to its own subclasses.
 */
public final class MembershipStateFactory {

    private static final Map<MembershipStatus, MembershipState> STATES = new EnumMap<>(Map.of(
            MembershipStatus.TRIAL, new TrialState(),
            MembershipStatus.ACTIVE, new ActiveState(),
            MembershipStatus.FROZEN, new FrozenState(),
            MembershipStatus.LAPSED, new LapsedState(),
            MembershipStatus.CANCELLED, new CancelledState()));

    private MembershipStateFactory() {
    }

    /**
     * Pre: {@code status} is not null. Post: the single shared {@link MembershipState} for {@code status}.
     */
    public static MembershipState of(MembershipStatus status) {
        return STATES.get(status);
    }
}
