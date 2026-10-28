package gym;

import gym.support.TestClock;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
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
 * Member end to end: a member signs up, checks in over HTTP and sees the visit in their history, as
 * Postman would see it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberE2E {

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
    void aMemberChecksInAndSeesItInTheirHistory() {
        String memberId = signUp("ann@example.com").get();

        client.get().uri("/members/{id}", memberId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.email").isEqualTo("ann@example.com");

        AtomicReference<String> checkInId = new AtomicReference<>();
        client.post().uri("/members/{id}/check-ins", memberId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.memberId").isEqualTo(memberId)
                .jsonPath("$.checkInId").value(String.class, checkInId::set);

        client.get().uri("/members/{id}/check-ins", memberId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].checkInId").isEqualTo(checkInId.get());
    }

    @Test
    void aFrozenMembershipRefusesCheckIn() {
        AtomicReference<String> memberId = new AtomicReference<>();
        AtomicReference<String> membershipId = new AtomicReference<>();
        client.post().uri("/memberships").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"Bob\", \"email\": \"bob@example.com\", \"plan\": \"MONTHLY\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.membership.memberId").value(String.class, memberId::set)
                .jsonPath("$.membership.membershipId").value(String.class, membershipId::set);

        CLOCK.set(JOINED.plusDays(7));
        client.post().uri("/membership-checks").exchange().expectStatus().isOk();

        client.post().uri("/memberships/{id}/freeze", membershipId.get())
                .contentType(MediaType.APPLICATION_JSON).body("{\"months\": 1}")
                .exchange()
                .expectStatus().isOk();

        client.post().uri("/members/{id}/check-ins", memberId.get())
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void anUnknownMemberIsNotFound() {
        client.get().uri("/members/m-42")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.detail").isEqualTo("Member m-42 not found");
    }

    private AtomicReference<String> signUp(String email) {
        AtomicReference<String> memberId = new AtomicReference<>();
        client.post().uri("/memberships").contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\": \"Someone\", \"email\": \"" + email + "\", \"plan\": \"MONTHLY\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.membership.memberId").value(String.class, memberId::set);
        return memberId;
    }
}
