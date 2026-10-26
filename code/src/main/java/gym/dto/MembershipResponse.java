package gym.dto;

import gym.domain.Freeze;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.PlanType;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * A membership and where it is in its lifecycle.
 *
 * @param membershipId the membership id, which is also the code a member gives friends to refer them
 * @param memberId the member who holds it
 * @param plan the plan it is on
 * @param status <code>TRIAL</code>, <code>ACTIVE</code>, <code>FROZEN</code>, <code>LAPSED</code> or <code>CANCELLED</code>
 * @param startDate when it started
 * @param endDate when it ends, or null if it rolls on month by month
 * @param trialEndsOn when the trial ends, only while in <code>TRIAL</code>
 * @param trialBookingsLeft class bookings left in the trial, only while in <code>TRIAL</code>
 * @param frozenUntil when the freeze ends, only while <code>FROZEN</code>
 * @param lapsedSince when it lapsed, only while <code>LAPSED</code>
 * @param failedPayments card payments declined in a row. Two make it <code>LAPSED</code>
 */
public record MembershipResponse(
        String membershipId,
        String memberId,
        PlanType plan,
        MembershipStatus status,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        @Nullable LocalDate trialEndsOn,
        @Nullable Integer trialBookingsLeft,
        @Nullable LocalDate frozenUntil,
        @Nullable LocalDate lapsedSince,
        int failedPayments) {

    public static MembershipResponse from(Membership membership) {
        boolean trial = membership.status() == MembershipStatus.TRIAL;
        Freeze freeze = membership.currentFreeze();
        return new MembershipResponse(
                membership.membershipId(),
                membership.memberId(),
                membership.plan(),
                membership.status(),
                membership.startDate(),
                membership.endDate(),
                trial ? membership.trialEndsOn() : null,
                trial ? Membership.TRIAL_BOOKINGS - membership.trialBookingsUsed() : null,
                freeze == null ? null : freeze.endDate(),
                membership.status() == MembershipStatus.LAPSED ? membership.lapsedSince() : null,
                membership.failedPayments());
    }
}
