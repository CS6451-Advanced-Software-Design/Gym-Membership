package gym.port.in;

import gym.domain.Invoice;
import java.util.List;

/**
 * UC11 View Invoices.
 */
public interface ViewInvoicesUseCase {

    /**
     * @return the membership's invoices, newest month first
     * @throws gym.domain.NotFoundException if the membership has no billing account
     */
    List<Invoice> invoicesFor(String membershipId);
}
