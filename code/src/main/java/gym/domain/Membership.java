package gym.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A member's membership of one plan.
 *
 * @param membershipId the membership id
 * @param memberId the member who holds it
 * @param plan the plan it is on
 * @param startDate when it started
 * @param status where it is in the state chart
 */
public record Membership(String membershipId, String memberId, PlanType plan, LocalDate startDate, MembershipStatus status) {

    public Membership {
        Objects.requireNonNull(membershipId, "membershipId");
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(status, "status");
    }
}
