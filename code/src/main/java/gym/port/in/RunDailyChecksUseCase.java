package gym.port.in;

import gym.domain.Membership;
import java.util.List;

/**
 * UC13 Run Daily Membership Checks, started by the Clock actor.
 */
public interface RunDailyChecksUseCase {

    /**
     * Ends trials after 7 days (BR-M2) and freezes whose months are up (BR-M3), makes a membership with two
     * unpaid invoices in a row Lapsed (BR-M4), and cancels one Lapsed for 60 days with a final bill (BR-M6).
     * Running it twice on one day changes nothing the second time.
     *
     * @return the memberships whose state changed
     */
    List<Membership> runDailyChecks();
}
