package gym.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gym.domain.AnnualPlan;
import gym.domain.Freeze;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.PlanType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class MembershipPersistenceIT {

    @TempDir
    Path directory;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void aMembershipKeepsItsFreezesAndPaymentsAcrossARestart() {
        LocalDate joined = LocalDate.of(2026, 3, 2);
        Membership membership = Membership.signUp("ms1", "m1", new AnnualPlan(), joined)
                .recordPayment(false, joined)
                .freeze(2, joined.plusMonths(1));
        new JsonFileMembershipRepository(new PersistenceProperties(directory), mapper).save(membership);

        Optional<Membership> reloaded = new JsonFileMembershipRepository(new PersistenceProperties(directory), mapper).findById("ms1");

        assertEquals(Optional.of(membership), reloaded);
        assertEquals(List.of(new Freeze(joined.plusMonths(1), 2)), reloaded.orElseThrow().freezes());
    }

    @Test
    void aMembershipSavedBeforeItHadALifecycleStillLoads() throws Exception {
        // memberships.json on the live app was written when a membership had only these five fields
        Files.writeString(directory.resolve("memberships.json"), """
                [{"membershipId": "ms1", "memberId": "m1", "plan": "MONTHLY", "startDate": "2026-03-02", "status": "ACTIVE"}]""");

        Membership loaded = new JsonFileMembershipRepository(new PersistenceProperties(directory), mapper).findById("ms1").orElseThrow();

        assertEquals(new Membership("ms1", "m1", PlanType.MONTHLY, LocalDate.of(2026, 3, 2), MembershipStatus.ACTIVE), loaded);
    }
}
