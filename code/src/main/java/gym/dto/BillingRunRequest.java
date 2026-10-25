package gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;

/**
 * Starts a billing run.
 *
 * @param period the month to bill, as <code>YYYY-MM</code>, e.g. <code>2026-11</code>
 */
public record BillingRunRequest(@NotNull @Schema(example = "2026-11") YearMonth period) {
}
