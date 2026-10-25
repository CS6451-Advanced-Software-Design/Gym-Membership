package gym.dto;

import gym.domain.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Someone joining the gym, and the plan they chose.
 *
 * @param name their full name
 * @param email where notifications go. Someone who left and joins again uses the same email
 * @param plan the plan they chose
 * @param studentId 8 digits, needed for the <code>STUDENT</code> plan
 * @param employer their employer, for the 15% Corporate discount
 * @param familyId the same for everyone in a family, for the 10% Family discount
 * @param referralCode the membership id of the member who referred them, who is credited €20
 */
public record SignUpRequest(
        @NotBlank @Schema(example = "Ann Byrne") String name,
        @NotBlank @Email @Schema(example = "ann@example.com") String email,
        @NotNull @Schema(example = "MONTHLY") PlanType plan,
        @Schema(example = "24123456") String studentId,
        @Schema(example = "Stripe") String employer,
        @Schema(example = "byrne") String familyId,
        @Schema(example = "ms-3f9c2a1b") String referralCode) {
}
