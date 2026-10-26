package gym.controller;

import gym.dto.BillingRunRequest;
import gym.dto.InvoiceResponse;
import gym.port.in.RunMonthlyBillingUseCase;
import gym.port.in.ViewInvoicesUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Charges members for their memberships and shows them what they paid.
 *
 * <p>Each month a membership's invoice is the <b>plan fee</b>, minus the <b>best discount</b>,
 * plus <b>penalties</b>, minus <b>referral credits</b>:
 * <ul>
 *   <li><b>Plan fee:</b> Monthly €45, Student €30, Off-Peak €30. Annual is €450 for the year, paid upfront
 *       at sign-up. A member who joins mid-month pays only for the days left in that month.</li>
 *   <li><b>Best discount:</b> discounts don't stack, only the largest applies. Corporate 15%, Family 10%
 *       (someone in the family joined earlier), Loyalty 5% (a member for more than 24 months).</li>
 *   <li><b>Penalties:</b> €5 for a late cancellation or a no-show, €8 for a premium class.</li>
 *   <li><b>Referral credits:</b> €20 per friend referred, up to €60 a year. A credit that doesn't fit
 *       within the bill waits for the next one.</li>
 * </ul>
 *
 * <p>The member's card is charged as soon as the invoice is issued. A declined card leaves the
 * invoice <code>UNPAID</code> until the member pays their arrears.
 */
@Tag(name = "Billing")
@Order(2)
@RestController
public class BillingController {

    private final RunMonthlyBillingUseCase billing;
    private final ViewInvoicesUseCase invoices;

    public BillingController(RunMonthlyBillingUseCase billing, ViewInvoicesUseCase invoices) {
        this.billing = billing;
        this.invoices = invoices;
    }

    /**
     * Runs the monthly billing.
     *
     * <p>Bills every active membership for one month and charges each member's card. The gym's clock
     * starts this on the first of every month. For each membership that isn't cancelled:
     * <ol>
     *   <li>It is skipped if it's frozen or already has an invoice for the month, so repeating a run
     *       never bills anyone twice.</li>
     *   <li>The monthly plan fee is added. Annual plans have no monthly fee, so an Annual member is billed
     *       only when penalties are waiting.</li>
     *   <li>Penalties and referral credits waiting since the last invoice are added.</li>
     *   <li>The best discount comes off the plan fee and the total is charged to the member's card.</li>
     *   <li>The member is told what was charged, or that the payment was declined.</li>
     * </ol>
     *
     * @param request the month to bill
     * @return The invoices this run issued
     */
    @PostMapping("/billing-runs")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "The period is missing or isn't YYYY-MM",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public List<InvoiceResponse> run(@Valid @RequestBody BillingRunRequest request) {
        return billing.run(request.period()).stream().map(InvoiceResponse::from).toList();
    }

    /**
     * Lists a membership's invoices.
     *
     * <p>Every invoice of one membership, newest month first. An <code>UNPAID</code> invoice is one whose card payment was declined. It stays unpaid until the
     * member pays their arrears.
     *
     * @param membershipId the membership whose invoices to list
     * @return The membership's invoices, newest first
     */
    @GetMapping("/memberships/{membershipId}/invoices")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public List<InvoiceResponse> invoices(@Parameter(example = "ms-ann") @PathVariable String membershipId) {
        return invoices.invoicesFor(membershipId).stream().map(InvoiceResponse::from).toList();
    }
}
