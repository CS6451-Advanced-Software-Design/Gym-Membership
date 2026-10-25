package gym;

import gym.support.TestClock;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Membership end to end: members sign up over HTTP and their memberships go through the state chart as the
 * clock moves on, as Postman would see it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MembershipE2E {

    private static final LocalDate JOINED = LocalDate.of(2026, 11, 2);

    @TempDir
    static Path data;

    @DynamicPropertySource
    static void dataFolder(DynamicPropertyRegistry registry) {
        registry.add("gym.persistence.directory", data::toString);
    }

    private static final TestClock CLOCK = new TestClock(JOINED);

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
        CLOCK.set(JOINED);
    }

    @Test
    void thePlansAreListedWithTheirPrices() {
        client.get().uri("/plans")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].plan").isEqualTo(List.of("MONTHLY", "ANNUAL", "STUDENT", "OFF_PEAK"))
                .jsonPath("$[?(@.plan == 'ANNUAL')].trialDays").isEqualTo(List.of(0));
    }

    @Test
    void aNewMemberGoesFromTrialToActiveAndCanThenFreezeAndCancel() {
        AtomicReference<String> created = new AtomicReference<>();
        signUp("ann@example.com", "MONTHLY", "Stripe")
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.membership.status").isEqualTo("TRIAL")
                .jsonPath("$.membership.trialBookingsLeft").isEqualTo(2)
                // 29 of 30 days at €45 is €43.50, minus the 15% corporate discount of €6.53
                .jsonPath("$.firstInvoice.total").isEqualTo(36.97)
                .jsonPath("$.membership.membershipId").value(String.class, created::set);
        String id = created.get();

        client.post().uri("/memberships/{id}/freeze", id).contentType(MediaType.APPLICATION_JSON).body("{\"months\": 1}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("Membership " + id + " is Trial, so it can't be frozen");

        CLOCK.set(JOINED.plusDays(7));
        client.post().uri("/membership-checks")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[?(@.membershipId == '" + id + "')].status").isEqualTo(List.of("ACTIVE"));

        client.post().uri("/memberships/{id}/freeze", id).contentType(MediaType.APPLICATION_JSON).body("{\"months\": 2}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FROZEN")
                .jsonPath("$.frozenUntil").isEqualTo(JOINED.plusDays(7).plusMonths(2).toString());

        client.post().uri("/memberships/{id}/cancel", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.membership.status").isEqualTo("CANCELLED")
                .jsonPath("$.finalInvoice.membershipId").isEqualTo(id);
    }

    @Test
    void theSameEmailCannotHoldTwoMemberships() {
        signUp("bob@example.com", "ANNUAL", null).expectStatus().isCreated();

        signUp("bob@example.com", "MONTHLY", null)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").value(String.class, detail -> detail.startsWith("bob@example.com already has membership"));
    }

    @Test
    void theStudentPlanNeedsAStudentId() {
        signUp("cara@example.com", "STUDENT", null)
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.detail").isEqualTo("The Student plan needs an 8-digit student ID");
    }

    @Test
    void aMissingEmailIsABadRequest() {
        client.post().uri("/memberships").contentType(MediaType.APPLICATION_JSON).body("{\"name\": \"Dan\", \"plan\": \"MONTHLY\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void anUnknownMembershipIsNotFound() {
        client.get().uri("/memberships/ms-42")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.detail").isEqualTo("Membership ms-42 not found");
    }

    private RestTestClient.ResponseSpec signUp(String email, String plan, String employer) {
        String employerField = employer == null ? "" : ", \"employer\": \"" + employer + "\"";
        return client.post().uri("/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"Someone\", \"email\": \"" + email + "\", \"plan\": \"" + plan + "\"" + employerField + "}")
                .exchange();
    }
}
