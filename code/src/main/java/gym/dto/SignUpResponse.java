package gym.dto;

import gym.domain.Invoice;
import gym.domain.Membership;

/**
 * A new membership and its first bill.
 *
 * @param membership the new membership, in <code>TRIAL</code>, or <code>ACTIVE</code> for an Annual plan
 * @param firstInvoice the first charge. <code>UNPAID</code> if the card was declined, and then the trial ends <code>LAPSED</code>
 */
public record SignUpResponse(MembershipResponse membership, InvoiceResponse firstInvoice) {

    public static SignUpResponse from(Membership membership, Invoice firstInvoice) {
        return new SignUpResponse(MembershipResponse.from(membership), InvoiceResponse.from(firstInvoice));
    }
}
