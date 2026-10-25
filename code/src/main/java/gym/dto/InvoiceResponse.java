package gym.dto;

import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * A membership's bill for one month.
 *
 * @param id the invoice id
 * @param membershipId the membership it bills
 * @param period the month it bills, as <code>YYYY-MM</code>
 * @param charges what makes up the bill
 * @param discount what the best discount took off the plan fee, in euro
 * @param total what the member pays, in euro, never below zero
 * @param status <code>PAID</code> once the card payment went through, <code>UNPAID</code> if it was declined
 */
public record InvoiceResponse(
        String id,
        String membershipId,
        YearMonth period,
        List<ChargeResponse> charges,
        BigDecimal discount,
        BigDecimal total,
        InvoiceStatus status) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.id(),
                invoice.membershipId(),
                invoice.period(),
                invoice.charges().stream().map(ChargeResponse::from).toList(),
                invoice.discount().amount(),
                invoice.total().amount(),
                invoice.status());
    }
}
