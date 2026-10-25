package gym.dto;

import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;

/**
 * @param period the month to bill, e.g. {@code "2026-11"}
 */
public record BillingRunRequest(@NotNull YearMonth period) {
}
