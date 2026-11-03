package gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * A member asking for a place in a class.
 *
 * @param memberId the member booking
 */
public record BookClassRequest(@NotBlank @Schema(example = "m-ca315689") String memberId) {
}
