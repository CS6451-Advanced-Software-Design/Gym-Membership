package gym.port.in;

import gym.domain.Invoice;
import java.time.YearMonth;
import java.util.List;

/**
 * UC7 Run Monthly Billing, started by the Clock actor.
 */
public interface RunMonthlyBillingUseCase {

    /**
     * Bills every member for the month and charges their card. Frozen memberships are skipped (BR-M3),
     * and a member already billed for the month isn't billed again, so a run can safely be repeated.
     *
     * @return the invoices issued by this run
     */
    List<Invoice> run(YearMonth period);
}
