package gym.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.Invoice;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Money;
import gym.domain.PlanType;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class BillingPersistenceTest {

    @TempDir
    Path directory;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void invoicesAndTheirChargesSurviveARestart() {
        PersistenceProperties properties = new PersistenceProperties(directory);
        Charge fee = Charge.pending("c1", "ms1", ChargeType.PLAN_FEE, Money.euros(45), LocalDate.of(2026, 11, 1));
        Charge credit = Charge.pending("c2", "ms1", ChargeType.REFERRAL_CREDIT, Money.euros(-20), LocalDate.of(2026, 10, 3));
        Invoice invoice = Invoice.issue("i1", "ms1", YearMonth.of(2026, 11), List.of(fee, credit), Money.euros("6.75"));
        new JsonFileInvoiceRepository(properties, mapper).save(invoice);
        new JsonFileChargeRepository(properties, mapper).save(credit);

        assertEquals(Optional.of(invoice), new JsonFileInvoiceRepository(properties, mapper).findById("i1"));
        assertEquals(Optional.of(credit), new JsonFileChargeRepository(properties, mapper).findById("c2"));
    }

    @Test
    void membersAndMembershipsSurviveARestart() {
        PersistenceProperties properties = new PersistenceProperties(directory);
        Member member = new Member("m1", "Ann Byrne", "ann@example.com", null, "Stripe", "byrne");
        Membership membership = new Membership("ms1", "m1", PlanType.MONTHLY, LocalDate.of(2026, 3, 2), MembershipStatus.ACTIVE);
        new JsonFileMemberRepository(properties, mapper).save(member);
        new JsonFileMembershipRepository(properties, mapper).save(membership);

        assertEquals(Optional.of(member), new JsonFileMemberRepository(properties, mapper).findById("m1"));
        assertEquals(Optional.of(membership), new JsonFileMembershipRepository(properties, mapper).findById("ms1"));
    }
}
