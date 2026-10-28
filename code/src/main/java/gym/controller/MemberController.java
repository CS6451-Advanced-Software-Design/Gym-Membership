package gym.controller;

import gym.dto.CheckInResponse;
import gym.dto.MemberResponse;
import gym.port.in.CheckInUseCase;
import gym.port.in.ViewMemberUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shows a member's profile and checks them in for a visit.
 *
 * <p>A check-in is only recorded if the member's current membership isn't <code>CANCELLED</code>, and
 * their plan and state allow a visit right now (e.g. a <code>FROZEN</code> membership refuses, see
 * {@link gym.domain.Membership#canCheckIn}).
 */
@Tag(name = "Members")
@Order(4)
@RestController
public class MemberController {

    private static final String PROBLEM = "application/problem+json";

    private final ViewMemberUseCase view;
    private final CheckInUseCase checkIns;

    public MemberController(ViewMemberUseCase view, CheckInUseCase checkIns) {
        this.view = view;
        this.checkIns = checkIns;
    }

    /**
     * Shows a member.
     *
     * @param memberId the member to show
     * @return The member
     */
    @GetMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No member has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public MemberResponse member(@Parameter(example = "m-3f9c2a71") @PathVariable String memberId) {
        return MemberResponse.from(view.member(memberId));
    }

    /**
     * Checks a member in.
     *
     * <p>Records a visit against the member's current membership, and sends them a notification.
     *
     * @param memberId the member checking in
     * @return The check-in
     */
    @PostMapping("/members/{memberId}/check-ins")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "404", description = "No member has this id, or they hold no current membership",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The membership's state or plan refuses the check-in",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public CheckInResponse checkIn(@Parameter(example = "m-3f9c2a71") @PathVariable String memberId) {
        return CheckInResponse.from(checkIns.checkIn(memberId));
    }

    /**
     * Lists a member's check-ins.
     *
     * @param memberId the member
     * @return Every check-in, oldest first
     */
    @GetMapping("/members/{memberId}/check-ins")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No member has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public List<CheckInResponse> history(@Parameter(example = "m-3f9c2a71") @PathVariable String memberId) {
        return checkIns.history(memberId).stream().map(CheckInResponse::from).toList();
    }
}
