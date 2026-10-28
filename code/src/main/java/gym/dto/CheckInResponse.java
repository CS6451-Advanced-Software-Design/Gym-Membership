package gym.dto;

import gym.domain.CheckIn;
import java.time.LocalDateTime;

/**
 * One visit to the gym.
 *
 * @param checkInId the check-in id
 * @param memberId the member who checked in
 * @param membershipId the membership checked against
 * @param at when they checked in
 */
public record CheckInResponse(String checkInId, String memberId, String membershipId, LocalDateTime at) {

    public static CheckInResponse from(CheckIn checkIn) {
        return new CheckInResponse(checkIn.checkInId(), checkIn.memberId(), checkIn.membershipId(), checkIn.at());
    }
}
