package gym;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.PlanType;
import gym.port.out.ChargeRepository;
import gym.port.out.InvoiceRepository;
import gym.port.out.MemberRepository;
import gym.port.out.MembershipRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Billing end to end: the whole app runs on a real port with its own data folder, and every check goes
 * through HTTP and the JSON files, as Postman would see it.
 *
 * <p>Members and memberships are stored straight into their repositories, because there is no sign-up
 * endpoint yet. Once Membership adds one, this test should sign them up over HTTP instead.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "gym.payment.declined-members=m-eve")
class BillingE2E {

    @TempDir
    static Path data;

    @DynamicPropertySource
    static void dataFolder(DynamicPropertyRegistry registry) {
        registry.add("gym.persistence.directory", data::toString);
    }

    /**
     * Billing runs on 1 November 2026, so the dates, pro-rating and loyalty don't depend on today.
     */
    @TestBean
    Clock clock;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-01T09:00:00Z"), ZoneOffset.UTC);
    }

    @LocalServerPort
    int port;

    @Autowired
    MemberRepository members;

    @Autowired
    MembershipRepository memberships;

    @Autowired
    ChargeRepository charges;

    @Autowired
    InvoiceRepository invoices;

    RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        invoices.findAll().forEach(invoice -> invoices.deleteById(invoice.id()));
        charges.findAll().forEach(charge -> charges.deleteById(charge.id()));

        join("ann", "Stripe", null, PlanType.MONTHLY, LocalDate.of(2026, 3, 2), MembershipStatus.ACTIVE);
        join("mary", null, "walsh", PlanType.MONTHLY, LocalDate.of(2025, 1, 10), MembershipStatus.ACTIVE);
        join("cara", null, "walsh", PlanType.STUDENT, LocalDate.of(2026, 2, 10), MembershipStatus.ACTIVE);
        join("dan", null, null, PlanType.MONTHLY, LocalDate.of(2025, 9, 1), MembershipStatus.FROZEN);
        join("eve", null, null, PlanType.MONTHLY, LocalDate.of(2026, 5, 20), MembershipStatus.ACTIVE);
    }

    @Test
    void aBillingRunBillsEveryActiveMembershipAndChargesTheirCard() {
        runBilling("2026-11")
                .expectStatus().isCreated()
                .expectBody()
                // Dan is frozen, so four invoices, not five
                .jsonPath("$.length()").isEqualTo(4)
                // Ann has an employer: €45 minus the 15% corporate discount
                .jsonPath("$[?(@.membershipId == 'ms-ann')].total").isEqualTo(List.of(38.25))
                // Mary joined the Walsh family first, so she pays in full
                .jsonPath("$[?(@.membershipId == 'ms-mary')].total").isEqualTo(List.of(45.0))
                // Cara joined after Mary: €30 minus the 10% family discount
                .jsonPath("$[?(@.membershipId == 'ms-cara')].total").isEqualTo(List.of(27.0))
                // Eve's card is declined
                .jsonPath("$[?(@.membershipId == 'ms-eve')].status").isEqualTo(List.of("UNPAID"))
                .jsonPath("$[?(@.membershipId == 'ms-ann')].status").isEqualTo(List.of("PAID"));
    }

    @Test
    void runningTheSameMonthAgainBillsNobody() {
        runBilling("2026-11").expectStatus().isCreated();

        runBilling("2026-11")
                .expectStatus().isCreated()
                .expectBody().json("[]");
    }

    @Test
    void invoicesAreSavedToTheirFileAndListedNewestMonthFirst() throws Exception {
        runBilling("2026-11").expectStatus().isCreated();
        runBilling("2026-12").expectStatus().isCreated();

        assertTrue(Files.readString(data.resolve("invoices.json")).contains("ms-ann"));
        client.get().uri("/memberships/ms-ann/invoices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[*].period").isEqualTo(List.of("2026-12", "2026-11"));
    }

    @Test
    void anUnknownMembershipIsANotFoundProblem() {
        client.get().uri("/memberships/ms-42/invoices")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Membership ms-42 not found");
    }

    @Test
    void aMissingPeriodIsABadRequestProblem() {
        client.post().uri("/billing-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Bad Request");
    }

    private RestTestClient.ResponseSpec runBilling(String period) {
        return client.post().uri("/billing-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"period\": \"" + period + "\"}")
                .exchange();
    }

    private void join(String name, String employer, String familyId, PlanType plan, LocalDate since, MembershipStatus status) {
        members.save(new Member("m-" + name, name, name + "@example.com", null, employer, familyId));
        memberships.save(new Membership("ms-" + name, "m-" + name, plan, since, status));
    }
}
