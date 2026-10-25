package gym.dto;

import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

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
