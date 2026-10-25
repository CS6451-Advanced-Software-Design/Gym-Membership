package gym.domain;

/**
 * The states in the Membership state chart, stored as the {@code state} column.
 */
public enum MembershipStatus {
    TRIAL,
    ACTIVE,
    FROZEN,
    LAPSED,
    CANCELLED
}
