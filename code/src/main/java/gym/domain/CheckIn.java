package gym.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One visit to the gym, recorded against a member's current membership.
 *
 * @param checkInId the check-in id
 * @param memberId the member who checked in
 * @param membershipId the membership checked against
 * @param at when they checked in
 */
public record CheckIn(String checkInId, String memberId, String membershipId, LocalDateTime at) {

    public CheckIn {
        Objects.requireNonNull(checkInId, "checkInId");
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(membershipId, "membershipId");
        Objects.requireNonNull(at, "at");
    }
}
