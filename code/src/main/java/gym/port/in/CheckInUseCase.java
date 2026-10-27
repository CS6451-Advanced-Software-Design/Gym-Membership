package gym.port.in;

import gym.domain.CheckIn;
import java.util.List;

/**
 * UC: Check In. Records a visit against a member's current membership.
 */
public interface CheckInUseCase {

    /**
     * Checks a member in, if their current membership's state and plan allow it right now
     * ({@link gym.domain.Membership#canCheckIn}). Sends the member a notification once recorded.
     *
     * <p>Pre: the member holds a membership that isn't cancelled. Post: the check-in is stored and the member
     * is notified.
     *
     * @throws gym.domain.NotFoundException if there is no such member, or they hold no current membership
     * @throws gym.domain.RuleViolationException if the membership's state or plan refuses the check-in
     */
    CheckIn checkIn(String memberId);

    /**
     * @return every check-in by this member, oldest first
     * @throws gym.domain.NotFoundException if there is no such member
     */
    List<CheckIn> history(String memberId);
}
