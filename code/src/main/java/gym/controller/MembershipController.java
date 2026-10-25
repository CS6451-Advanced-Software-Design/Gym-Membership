package gym.controller;

import gym.dto.CancellationResponse;
import gym.dto.FreezeRequest;
import gym.dto.MembershipResponse;
import gym.dto.PlanResponse;
import gym.dto.SignUpRequest;
import gym.dto.SignUpResponse;
import gym.port.in.CancelMembershipUseCase;
import gym.port.in.CancelMembershipUseCase.Cancellation;
import gym.port.in.FreezeMembershipUseCase;
import gym.port.in.RestoreMembershipUseCase;
import gym.port.in.RunDailyChecksUseCase;
import gym.port.in.SignUpUseCase;
import gym.port.in.SignUpUseCase.Applicant;
import gym.port.in.SignUpUseCase.SignUp;
import gym.port.in.ViewMembershipUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Signs members up and takes their memberships through their lifecycle.
 *
 * <p>A membership is always in one of five states:
 * <ul>
 *   <li><b>TRIAL:</b> the first 7 days, with 2 class bookings. Annual plans are paid upfront and skip it.</li>
 *   <li><b>ACTIVE:</b> book classes and check in as the plan allows.</li>
 *   <li><b>FROZEN:</b> 1 to 3 months, once in any 12 months. Not billed, and the end date moves on by the
 *       same time.</li>
 *   <li><b>LAPSED:</b> the first charge or two payments in a row were declined. No booking or check-in
 *       until the arrears are paid. Cancelled after 60 days.</li>
 *   <li><b>CANCELLED:</b> with a final bill. Cancelling an Annual plan within 6 months costs €50.</li>
 * </ul>
 *
 * <p>An operation the state doesn't allow, such as freezing a trial, is refused with a 422 problem.
 */
@Tag(name = "Memberships")
@RestController
public class MembershipController {

    private static final String PROBLEM = "application/problem+json";

    private final SignUpUseCase signUp;
    private final ViewMembershipUseCase view;
    private final FreezeMembershipUseCase freezes;
    private final CancelMembershipUseCase cancellations;
    private final RestoreMembershipUseCase restore;
    private final RunDailyChecksUseCase checks;

    public MembershipController(
            SignUpUseCase signUp,
            ViewMembershipUseCase view,
            FreezeMembershipUseCase freezes,
            CancelMembershipUseCase cancellations,
            RestoreMembershipUseCase restore,
            RunDailyChecksUseCase checks) {
        this.signUp = signUp;
        this.view = view;
        this.freezes = freezes;
        this.cancellations = cancellations;
        this.restore = restore;
        this.checks = checks;
    }

    /**
     * Lists the plans.
     *
     * <p>Every plan on offer, with its price, its trial and when it can be used.
     *
     * @return The plans on offer
     */
    @GetMapping("/plans")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanResponse> plans() {
        return view.plans().stream().map(PlanResponse::from).toList();
    }

    /**
     * Signs someone up.
     *
     * <p>Creates the member and their membership and charges the first payment to their card:
     * <ol>
     *   <li>The membership starts in <code>TRIAL</code> for 7 days, or <code>ACTIVE</code> for an Annual plan.</li>
     *   <li>The first charge is the plan price for the days left in this month, or the whole year for an Annual
     *       plan, minus the best discount.</li>
     *   <li>If the card is declined the membership is still created, and the trial ends <code>LAPSED</code>
     *       unless the arrears are paid first.</li>
     *   <li>With a referral code, the referrer is credited €20 once the first charge is paid.</li>
     * </ol>
     *
     * @param request who is joining and on which plan
     * @return The new membership and its first invoice
     */
    @PostMapping("/memberships")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "A required field is missing or the email isn't valid",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The email already has a membership, the student ID is missing or not 8 digits, or the referral code is unknown",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest request) {
        Applicant applicant = new Applicant(request.name(), request.email(), request.studentId(), request.employer(), request.familyId());
        SignUp result = signUp.signUp(applicant, request.plan(), request.referralCode());
        return SignUpResponse.from(result.membership(), result.firstInvoice());
    }

    /**
     * Shows a membership.
     *
     * <p>The membership's state, and what that state means for it: when a trial or freeze ends, or how long it
     * has been lapsed.
     *
     * @param membershipId the membership to show
     * @return The membership
     */
    @GetMapping("/memberships/{membershipId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public MembershipResponse membership(@Parameter(example = "ms-ann") @PathVariable String membershipId) {
        return MembershipResponse.from(view.membership(membershipId));
    }

    /**
     * Freezes a membership.
     *
     * <p>Only an <code>ACTIVE</code> membership can be frozen, for 1 to 3 months and at most once in 12 months.
     * It isn't billed while frozen, and the daily checks unfreeze it when the months are up.
     *
     * @param membershipId the membership to freeze
     * @param request how many months
     * @return The frozen membership
     */
    @PostMapping("/memberships/{membershipId}/freeze")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "The number of months is missing",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The membership isn't Active, the months aren't 1 to 3, or it was frozen in the last 12 months",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public MembershipResponse freeze(@Parameter(example = "ms-ann") @PathVariable String membershipId, @Valid @RequestBody FreezeRequest request) {
        return MembershipResponse.from(freezes.freeze(membershipId, request.months()));
    }

    /**
     * Unfreezes a membership early.
     *
     * <p>Makes a <code>FROZEN</code> membership <code>ACTIVE</code> before its months are up.
     *
     * @param membershipId the membership to unfreeze
     * @return The active membership
     */
    @PostMapping("/memberships/{membershipId}/unfreeze")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The membership isn't Frozen",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public MembershipResponse unfreeze(@Parameter(example = "ms-ann") @PathVariable String membershipId) {
        return MembershipResponse.from(freezes.unfreeze(membershipId));
    }

    /**
     * Cancels a membership.
     *
     * <p>Cancels it and charges the final bill: any penalties still waiting, plus €50 for an Annual plan
     * cancelled within its first 6 months.
     *
     * @param membershipId the membership to cancel
     * @return The cancelled membership and its final invoice
     */
    @PostMapping("/memberships/{membershipId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The membership is already cancelled",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public CancellationResponse cancel(@Parameter(example = "ms-ann") @PathVariable String membershipId) {
        Cancellation result = cancellations.cancel(membershipId);
        return CancellationResponse.from(result.membership(), result.finalInvoice());
    }

    /**
     * Pays a membership's arrears.
     *
     * <p>Charges every unpaid invoice to the member's card in one payment. A <code>LAPSED</code> membership
     * becomes <code>ACTIVE</code> again.
     *
     * @param membershipId the membership whose arrears to pay
     * @return The membership after paying
     */
    @PostMapping("/memberships/{membershipId}/pay-arrears")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No membership has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The membership is Frozen or Cancelled, owes nothing, or the card was declined",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public MembershipResponse payArrears(@Parameter(example = "ms-ann") @PathVariable String membershipId) {
        return MembershipResponse.from(restore.payArrears(membershipId));
    }

    /**
     * Runs the daily membership checks.
     *
     * <p>The gym's clock starts this every morning. For every membership:
     * <ol>
     *   <li>A trial that is 7 days old ends: <code>ACTIVE</code> if the first charge was paid,
     *       <code>LAPSED</code> if not.</li>
     *   <li>A freeze whose months are up ends, and the membership is <code>ACTIVE</code> again.</li>
     *   <li>A membership with two unpaid invoices in a row becomes <code>LAPSED</code>.</li>
     *   <li>A membership <code>LAPSED</code> for 60 days is cancelled with a final bill.</li>
     * </ol>
     * Running it again on the same day changes nothing.
     *
     * @return The memberships whose state changed
     */
    @PostMapping("/membership-checks")
    @ResponseStatus(HttpStatus.OK)
    public List<MembershipResponse> runChecks() {
        return checks.runDailyChecks().stream().map(MembershipResponse::from).toList();
    }
}
