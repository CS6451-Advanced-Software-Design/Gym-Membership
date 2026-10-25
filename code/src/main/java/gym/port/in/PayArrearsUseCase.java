package gym.port.in;

import gym.domain.Invoice;
import java.util.List;

/**
 * The payment step of UC14 Pay Arrears. Membership calls it, then restores a Lapsed membership
 * to Active only if the arrears were paid (BR-M4).
 */
public interface PayArrearsUseCase {

    /**
     * Charges the total of every unpaid invoice in one payment.
     *
     * <p>Pre: the membership has at least one unpaid invoice.
     * Post: either every unpaid invoice is paid, or none is and the member is told the payment was declined.
     *
     * @return the invoices now paid, or an empty list if the payment was declined
     * @throws gym.domain.NotFoundException if the membership has no billing account
     * @throws gym.domain.RuleViolationException if nothing is owed
     */
    List<Invoice> payArrears(String membershipId);
}
