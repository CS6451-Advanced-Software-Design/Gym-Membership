package gym.dto;

import gym.domain.Invoice;
import gym.domain.Membership;

/**
 * A cancelled membership and its final bill.
 *
 * @param membership the membership, now <code>CANCELLED</code>
 * @param finalInvoice everything still owed, with the €50 early-exit fee for an Annual plan cancelled within 6 months
 */
public record CancellationResponse(MembershipResponse membership, InvoiceResponse finalInvoice) {

    public static CancellationResponse from(Membership membership, Invoice finalInvoice) {
        return new CancellationResponse(MembershipResponse.from(membership), InvoiceResponse.from(finalInvoice));
    }
}
