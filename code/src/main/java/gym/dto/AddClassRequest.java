package gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * A class to put on the timetable.
 *
 * @param name what the class is
 * @param startTime when it starts, which must be in the future
 * @param capacity how many places it has, at least 1
 * @param premium whether it carries the €8 surcharge, which Annual members don't pay. False if left out
 */
public record AddClassRequest(
        @NotBlank @Schema(example = "Spin") String name,
        @NotNull @Schema(example = "2026-11-06T18:00:00") LocalDateTime startTime,
        @NotNull @Schema(example = "12") Integer capacity,
        @Schema(example = "false") Boolean premium) {
}
