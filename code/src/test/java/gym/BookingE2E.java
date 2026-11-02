package gym;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.support.TestClock;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Booking end to end: staff add classes and members book, wait, cancel and undo over HTTP, with real files and
 * real Billing behind them, as Postman would see it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class BookingE2E {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime FRIDAY_EVENING = LocalDateTime.of(2026, 11, 6, 18, 0);
    private static final AtomicInteger EMAILS = new AtomicInteger();
    private static final Map<String, String> MEMBERSHIPS = new ConcurrentHashMap<>();

    @TempDir
    static Path data;

    @DynamicPropertySource
    static void dataFolder(DynamicPropertyRegistry registry) {
        registry.add("gym.persistence.directory", data::toString);
    }

    private static final TestClock CLOCK = new TestClock(TODAY);

    @TestBean
    Clock clock;

    static Clock clock() {
        return CLOCK;
    }

    @LocalServerPort
    int port;

    RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        CLOCK.set(TODAY);
    }

    @Test
    void aBookingUsesAPlaceOnTheSchedule() {
        String spin = addClass("Spin", FRIDAY_EVENING, 2, false);
        String member = signUp("ANNUAL");

        book(spin, member)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.bookingId").isNotEmpty()
                .jsonPath("$.waitlistPosition").isEqualTo(0);

        client.get().uri("/classes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.classId == '" + spin + "')].placesLeft").isEqualTo(List.of(1))
                .jsonPath("$[?(@.classId == '" + spin + "')].waitlistSize").isEqualTo(List.of(0));

        book(spin, member)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("Member " + member + " already has a place in class " + spin);
    }

    @Test
    void cancellingPromotesTheFirstOnTheWaitlistAndTellsThem(CapturedOutput output) {
        String spin = addClass("Spin", FRIDAY_EVENING, 1, false);
        String first = signUp("ANNUAL");
        String second = signUp("ANNUAL");
        String third = signUp("ANNUAL");
        String booking = bookedPlace(spin, first);
        book(spin, second).expectStatus().isCreated().expectBody().jsonPath("$.waitlistPosition").isEqualTo(1);
        book(spin, third).expectStatus().isCreated().expectBody().jsonPath("$.waitlistPosition").isEqualTo(2);

        client.post().uri("/bookings/{id}/cancel", booking)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("CANCELLED");

        client.get().uri("/classes")
                .exchange()
                .expectBody()
                .jsonPath("$[?(@.classId == '" + spin + "')].placesLeft").isEqualTo(List.of(0))
                .jsonPath("$[?(@.classId == '" + spin + "')].waitlistSize").isEqualTo(List.of(1));
        assertTrue(output.getAll().contains("notify member " + second + ": You have a place in Spin"));
        assertFalse(output.getAll().contains("notify member " + third + ": You have a place"));
        book(spin, second).expectStatus().isEqualTo(422);
    }

    @Test
    void aMemberCanUndoTheirActionsNewestFirst() {
        String spin = addClass("Spin", FRIDAY_EVENING, 1, false);
        String member = signUp("ANNUAL");
        String booking = bookedPlace(spin, member);
        client.post().uri("/bookings/{id}/cancel", booking).exchange().expectStatus().isOk();

        undo(member).expectStatus().isNoContent();
        client.post().uri("/bookings/{id}/cancel", booking).exchange().expectStatus().isOk();
        undo(member).expectStatus().isNoContent();
        undo(member).expectStatus().isNoContent();

        client.post().uri("/bookings/{id}/cancel", booking)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("Booking " + booking + " is CANCELLED, so it can't be cancelled");
        undo(member)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("Member " + member + " has nothing to undo");
    }

    @Test
    void aCancellationCannotBeUndoneOnceThePlaceIsGone() {
        String spin = addClass("Spin", FRIDAY_EVENING, 1, false);
        String first = signUp("ANNUAL");
        String second = signUp("ANNUAL");
        String booking = bookedPlace(spin, first);
        book(spin, second).expectStatus().isCreated();
        client.post().uri("/bookings/{id}/cancel", booking).exchange().expectStatus().isOk();

        undo(first)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("The place in class " + spin + " has been taken");
    }

    @Test
    void threeNoShowsSuspendBookingForAWeek() {
        String member = signUp("ANNUAL");
        String[] bookings = new String[3];
        for (int number = 0; number < 3; number++) {
            String gymClass = addClass("Spin", TODAY.plusDays(number + 1L).atTime(10, 0), 5, false);
            bookings[number] = bookedPlace(gymClass, member);
        }
        CLOCK.set(TODAY.plusDays(3).plusDays(1));
        for (String booking : bookings) {
            client.post().uri("/bookings/{id}/no-show", booking)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().jsonPath("$.status").isEqualTo("NO_SHOW");
        }
        String next = addClass("Spin", TODAY.plusDays(20).atTime(10, 0), 5, false);

        book(next, member)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo(
                        "Member " + member + " can't book: 3 no-shows in 30 days suspend booking for 7 days");

        CLOCK.set(TODAY.plusDays(11));
        book(next, member).expectStatus().isCreated();
    }

    @Test
    void attendanceWaitsForTheClassToStart() {
        String spin = addClass("Spin", FRIDAY_EVENING, 5, false);
        String booking = bookedPlace(spin, signUp("ANNUAL"));

        client.post().uri("/bookings/{id}/attended", booking)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("Class " + spin + " hasn't started yet");

        CLOCK.set(TODAY.plusDays(5));
        client.post().uri("/bookings/{id}/attended", booking)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("ATTENDED");
    }

    @Test
    void bookingFeesReachTheNextInvoice() {
        String pilates = addClass("Reformer Pilates", FRIDAY_EVENING, 5, true);
        String monthly = signUp("MONTHLY");
        bookedPlace(pilates, monthly);

        String startsSoon = addClass("Spin", TODAY.atTime(10, 0), 5, false);
        String annual = signUp("ANNUAL");
        String late = bookedPlace(startsSoon, annual);
        client.post().uri("/bookings/{id}/cancel", late).exchange().expectStatus().isOk();

        CLOCK.set(TODAY.plusDays(30));
        client.post().uri("/billing-runs").contentType(MediaType.APPLICATION_JSON).body("{\"period\": \"2026-12\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$[?(@.membershipId == '" + MEMBERSHIPS.get(monthly) + "')].charges[?(@.type == 'PREMIUM_SURCHARGE')].amount").isEqualTo(List.of(8.0))
                .jsonPath("$[?(@.membershipId == '" + MEMBERSHIPS.get(annual) + "')].charges[?(@.type == 'LATE_CANCELLATION')].amount").isEqualTo(List.of(5.0));
    }

    @Test
    void anUnknownClassOrBookingIsNotFound() {
        book("c-nobody", signUp("ANNUAL")).expectStatus().isNotFound();
        client.post().uri("/bookings/b-nobody/cancel").exchange().expectStatus().isNotFound();
    }

    @Test
    void aMissingFieldIsABadRequestAndABadCapacityIsRefused() {
        client.post().uri("/classes").contentType(MediaType.APPLICATION_JSON).body("{\"name\": \"Spin\"}")
                .exchange()
                .expectStatus().isBadRequest();
        client.post().uri("/classes").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"Spin\", \"startTime\": \"" + FRIDAY_EVENING + "\", \"capacity\": 0}")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    private String addClass(String name, LocalDateTime startTime, int capacity, boolean premium) {
        AtomicReference<String> classId = new AtomicReference<>();
        client.post().uri("/classes").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"" + name + "\", \"startTime\": \"" + startTime + "\", \"capacity\": " + capacity
                        + ", \"premium\": " + premium + "}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.classId").value(String.class, classId::set);
        return classId.get();
    }

    private String signUp(String plan) {
        AtomicReference<String> memberId = new AtomicReference<>();
        AtomicReference<String> membershipId = new AtomicReference<>();
        client.post().uri("/memberships").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"Someone\", \"email\": \"member" + EMAILS.incrementAndGet() + "@example.com\", \"plan\": \"" + plan + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.membership.memberId").value(String.class, memberId::set)
                .jsonPath("$.membership.membershipId").value(String.class, membershipId::set);
        MEMBERSHIPS.put(memberId.get(), membershipId.get());
        return memberId.get();
    }

    private String bookedPlace(String classId, String memberId) {
        AtomicReference<String> bookingId = new AtomicReference<>();
        book(classId, memberId)
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.bookingId").value(String.class, bookingId::set);
        return bookingId.get();
    }

    private RestTestClient.ResponseSpec book(String classId, String memberId) {
        return client.post().uri("/classes/{id}/bookings", classId).contentType(MediaType.APPLICATION_JSON)
                .body("{\"memberId\": \"" + memberId + "\"}")
                .exchange();
    }

    private RestTestClient.ResponseSpec undo(String memberId) {
        return client.post().uri("/members/{id}/bookings/undo", memberId).exchange();
    }
}
