package gym.port.in;

import gym.domain.Membership;
import gym.domain.Plan;
import java.util.List;

/**
 * Shows the plans on offer (UC1 step 2) and a membership's state.
 */
public interface ViewMembershipUseCase {

    List<Plan> plans();

    /**
     * @throws gym.domain.NotFoundException if there is no such membership
     */
    Membership membership(String membershipId);
}
