package gym.controller;

import gym.domain.GymClass;
import gym.dto.AddClassRequest;
import gym.dto.BookClassRequest;
import gym.dto.BookClassResponse;
import gym.dto.BookingResponse;
import gym.dto.ClassResponse;
import gym.port.in.BookingCommandsUseCase;
import gym.port.in.ManageScheduleUseCase;
import gym.port.in.RecordAttendanceUseCase;
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
 * Runs the class timetable: the schedule, bookings, the waitlist and attendance.
 *
 * <ul>
 *   <li><b>Booking:</b> a member holds at most 5 future bookings. Off-Peak members can book off-peak classes
 *       only, and a premium class costs €8 unless the member is on an Annual plan.</li>
 *   <li><b>Waitlist:</b> a full class has a first-come, first-served waitlist. When a place comes free, the
 *       first member who can still book gets it automatically and is notified.</li>
 *   <li><b>Cancelling:</b> less than 2 hours before the class costs €5. Trial members pay nothing, but a late
 *       cancellation still counts against their 2 bookings.</li>
 *   <li><b>No-shows:</b> each costs €5, and 3 in 30 days suspend booking for 7 days.</li>
 *   <li><b>Undo:</b> a member can take back their bookings and cancellations, newest first. Fees are not
 *       refunded.</li>
 * </ul>
 *
 * <p>A request the rules don't allow is refused with a 422 problem.
 */
@Tag(name = "Bookings")
@Order(3)
@RestController
public class BookingController {

    private static final String PROBLEM = "application/problem+json";

    private final BookingCommandsUseCase commands;
    private final RecordAttendanceUseCase attendance;
    private final ManageScheduleUseCase schedule;

    public BookingController(BookingCommandsUseCase commands, RecordAttendanceUseCase attendance, ManageScheduleUseCase schedule) {
        this.commands = commands;
        this.attendance = attendance;
        this.schedule = schedule;
    }

    /**
     * Shows the schedule.
     *
     * <p>Every class that hasn't started yet, soonest first, with its places left and the size of its waitlist.
     *
     * @return The upcoming classes
     */
    @GetMapping("/classes")
    @ResponseStatus(HttpStatus.OK)
    public List<ClassResponse> schedule() {
        return schedule.schedule().stream()
                .map(scheduled -> ClassResponse.from(scheduled.gymClass(), scheduled.placesLeft()))
                .toList();
    }

    /**
     * Adds a class.
     *
     * <p>Puts a new class on the timetable with every place free.
     *
     * @param request the class to add
     * @return The new class
     */
    @PostMapping("/classes")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "A required field is missing, or the start time isn't a date and time",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The start time has passed, or the capacity is below 1",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public ClassResponse addClass(@Valid @RequestBody AddClassRequest request) {
        boolean premium = Boolean.TRUE.equals(request.premium());
        GymClass added = schedule.addClass(request.name(), request.startTime(), request.capacity(), premium);
        return ClassResponse.from(added, added.capacity());
    }

    /**
     * Books a class.
     *
     * <p>Gives the member a place, or if the class is full, puts them on the waitlist. <code>bookingId</code> and
     * <code>waitlistPosition</code> say which. The €8 premium surcharge is charged when they get a place.
     *
     * @param classId the class to book
     * @param request who is booking
     * @return The place in the class or on its waitlist
     */
    @PostMapping("/classes/{classId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "The member id is missing",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No class has this id, or the member holds no current membership",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The class has started, the membership or plan doesn't allow it, the member is suspended, already has a place, or holds 5 future bookings",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public BookClassResponse book(@Parameter(example = "c-54b4cdb7") @PathVariable String classId, @Valid @RequestBody BookClassRequest request) {
        return BookClassResponse.from(request.memberId(), classId, commands.book(request.memberId(), classId));
    }

    /**
     * Cancels a booking.
     *
     * <p>Frees the place, which goes to the first member on the waitlist who can still book. Less than 2 hours
     * before the class this costs €5, unless the member is on a Trial.
     *
     * @param bookingId the booking to cancel
     * @return The cancelled booking
     */
    @PostMapping("/bookings/{bookingId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No booking has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The booking is no longer booked, or its class has started",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public BookingResponse cancelBooking(@Parameter(example = "b-981af13d") @PathVariable String bookingId) {
        return BookingResponse.from(commands.cancel(bookingId));
    }

    /**
     * Undoes a member's last booking action.
     *
     * <p>Takes back their most recent booking or cancellation. Calling it again takes back the one before:
     * <ul>
     *   <li>A booking is cancelled, and the usual late fee applies. A place on a waitlist is given up.</li>
     *   <li>A cancellation gives the same booking back, if the place hasn't been taken.</li>
     * </ul>
     * Fees are not refunded. The history is cleared if the app restarts.
     *
     * @param memberId the member whose action to undo
     */
    @PostMapping("/members/{memberId}/bookings/undo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "422", description = "There is nothing to undo, or it is too late, e.g. the class has started or the place has been taken",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public void undo(@Parameter(example = "m-1d685de8") @PathVariable String memberId) {
        commands.undoLast(memberId);
    }

    /**
     * Marks a booking as attended.
     *
     * <p>Once the class has started, records that the member came.
     *
     * @param bookingId the booking to mark
     * @return The attended booking
     */
    @PostMapping("/bookings/{bookingId}/attended")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No booking has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The booking is no longer booked, or its class hasn't started",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public BookingResponse markAttended(@Parameter(example = "b-b94b7221") @PathVariable String bookingId) {
        return BookingResponse.from(attendance.markAttended(bookingId));
    }

    /**
     * Marks a booking as a no-show.
     *
     * <p>Once the class has started, records that the member didn't come. This costs €5, and 3 no-shows in
     * 30 days suspend booking for 7 days.
     *
     * @param bookingId the booking to mark
     * @return The no-show booking
     */
    @PostMapping("/bookings/{bookingId}/no-show")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "No booking has this id",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "The booking is no longer booked, or its class hasn't started",
            content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    public BookingResponse markNoShow(@Parameter(example = "b-8e998e2b") @PathVariable String bookingId) {
        return BookingResponse.from(attendance.markNoShow(bookingId));
    }
}
