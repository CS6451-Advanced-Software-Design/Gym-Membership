package gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Freezes a membership.
 *
 * @param months how long to freeze it for, 1 to 3
 */
public record FreezeRequest(@NotNull @Schema(example = "2") Integer months) {
}
