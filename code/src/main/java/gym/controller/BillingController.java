package gym.controller;

import gym.dto.BillingRunRequest;
import gym.dto.InvoiceResponse;
import gym.port.in.RunMonthlyBillingUseCase;
import gym.port.in.ViewInvoicesUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {

    private final RunMonthlyBillingUseCase billing;
    private final ViewInvoicesUseCase invoices;

    public BillingController(RunMonthlyBillingUseCase billing, ViewInvoicesUseCase invoices) {
        this.billing = billing;
        this.invoices = invoices;
    }

    /**
     * UC7, started by the Clock actor. Postman stands in for the clock in Part 1.
     */
    @PostMapping("/billing-runs")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InvoiceResponse> run(@Valid @RequestBody BillingRunRequest request) {
        return billing.run(request.period()).stream().map(InvoiceResponse::from).toList();
    }

    /**
     * UC11.
     */
    @GetMapping("/memberships/{membershipId}/invoices")
    public List<InvoiceResponse> invoices(@PathVariable String membershipId) {
        return invoices.invoicesFor(membershipId).stream().map(InvoiceResponse::from).toList();
    }
}
