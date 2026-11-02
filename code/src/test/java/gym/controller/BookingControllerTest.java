package gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.GymClass;
import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import gym.port.in.BookingCommandsUseCase;
import gym.port.in.ManageScheduleUseCase;
import gym.port.in.RecordAttendanceUseCase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The web side of Booking: routes, status codes and the JSON, with the use cases faked.
 */
class BookingControllerTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 6, 18, 0);
    private static final Booking BOOKING = Booking.book("b1", "m1", "c1", START.minusDays(1));

    private final List<String> undone = new ArrayList<>();

    private final BookingCommandsUseCase commands = new BookingCommandsUseCase() {
        @Override
        public BookingResult book(String memberId, String classId) {
            return switch (classId) {
                case "c1" -> BookingResult.booked(BOOKING);
                case "full" -> BookingResult.waitlisted(2);
                case "started" -> throw new RuleViolationException("Class started has already started");
                default -> throw new NotFoundException("Class", classId);
            };
        }

        @Override
        public Booking cancel(String bookingId) {
            return found(bookingId).cancel();
        }

        @Override
        public void undoLast(String memberId) {
            if (memberId.equals("nothing")) {
                throw new RuleViolationException("Member nothing has nothing to undo");
            }
            undone.add(memberId);
        }
    };

    private final RecordAttendanceUseCase attendance = new RecordAttendanceUseCase() {
        @Override
        public Booking markAttended(String bookingId) {
            return found(bookingId).attend();
        }

        @Override
        public Booking markNoShow(String bookingId) {
            return found(bookingId).noShow();
        }
    };

    private final ManageScheduleUseCase schedule = new ManageScheduleUseCase() {
        @Override
        public GymClass addClass(String name, LocalDateTime startTime, int capacity, boolean premium) {
            if (capacity < 1) {
                throw new RuleViolationException("A class needs at least 1 place, not " + capacity);
            }
            return GymClass.schedule("c1", name, startTime, capacity, premium);
        }

        @Override
        public List<ScheduledClass> schedule() {
            return List.of(new ScheduledClass(GymClass.schedule("c1", "Spin", START, 12, true).joinWaitlist("m9"), 3));
        }
    };

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new BookingController(commands, attendance, schedule))
            .setControllerAdvice(new ErrorHandler())
            .build();

    private static Booking found(String bookingId) {
        if (!bookingId.equals("b1")) {
            throw new NotFoundException("Booking", bookingId);
        }
        return BOOKING;
    }

    @Test
    void theScheduleShowsPlacesLeftAndTheWaitlist() throws Exception {
        mvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].classId").value("c1"))
                .andExpect(jsonPath("$[0].capacity").value(12))
                .andExpect(jsonPath("$[0].premium").value(true))
                .andExpect(jsonPath("$[0].placesLeft").value(3))
                .andExpect(jsonPath("$[0].waitlistSize").value(1));
    }

    @Test
    void addingAClassReturnsItWithEveryPlaceFree() throws Exception {
        addClass("""
                {"name": "Spin", "startTime": "2026-11-06T18:00:00", "capacity": 12}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spin"))
                .andExpect(jsonPath("$.placesLeft").value(12))
                .andExpect(jsonPath("$.premium").value(false))
                .andExpect(jsonPath("$.waitlistSize").value(0));
    }

    @Test
    void aClassWithoutItsFieldsIsABadRequest() throws Exception {
        addClass("{}").andExpect(status().isBadRequest());
    }

    @Test
    void aClassWithABadStartTimeIsABadRequest() throws Exception {
        addClass("""
                {"name": "Spin", "startTime": "Friday", "capacity": 12}""")
                .andExpect(status().isBadRequest());
    }

    @Test
    void aClassTheRulesRefuseIsUnprocessable() throws Exception {
        addClass("""
                {"name": "Spin", "startTime": "2026-11-06T18:00:00", "capacity": 0}""")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A class needs at least 1 place, not 0"));
    }

    @Test
    void bookingReturnsTheNewBooking() throws Exception {
        bookInto("c1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value("m1"))
                .andExpect(jsonPath("$.classId").value("c1"))
                .andExpect(jsonPath("$.bookingId").value("b1"))
                .andExpect(jsonPath("$.waitlistPosition").value(0));
    }

    @Test
    void bookingAFullClassReturnsTheWaitlistPosition() throws Exception {
        bookInto("full")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").doesNotExist())
                .andExpect(jsonPath("$.waitlistPosition").value(2));
    }

    @Test
    void bookingAnUnknownClassIsNotFound() throws Exception {
        bookInto("nobody").andExpect(status().isNotFound());
    }

    @Test
    void aBookingTheRulesRefuseIsUnprocessable() throws Exception {
        bookInto("started").andExpect(status().isUnprocessableContent());
    }

    @Test
    void bookingWithoutAMemberIsABadRequest() throws Exception {
        mvc.perform(post("/classes/c1/bookings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancellingReturnsTheCancelledBooking() throws Exception {
        mvc.perform(post("/bookings/b1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancellingAnUnknownBookingIsNotFound() throws Exception {
        mvc.perform(post("/bookings/nobody/cancel")).andExpect(status().isNotFound());
    }

    @Test
    void undoTakesBackTheMembersLastAction() throws Exception {
        mvc.perform(post("/members/m1/bookings/undo")).andExpect(status().isNoContent());

        assertEquals(List.of("m1"), undone);
    }

    @Test
    void undoWithNothingToUndoIsUnprocessable() throws Exception {
        mvc.perform(post("/members/nothing/bookings/undo"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Member nothing has nothing to undo"));
    }

    @Test
    void attendanceIsRecorded() throws Exception {
        mvc.perform(post("/bookings/b1/attended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));
    }

    @Test
    void aNoShowIsRecorded() throws Exception {
        mvc.perform(post("/bookings/b1/no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    private ResultActions addClass(String body) throws Exception {
        return mvc.perform(post("/classes").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions bookInto(String classId) throws Exception {
        return mvc.perform(post("/classes/" + classId + "/bookings").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"memberId": "m1"}"""));
    }
}
