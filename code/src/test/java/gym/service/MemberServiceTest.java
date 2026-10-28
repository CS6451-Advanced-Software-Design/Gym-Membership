package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.CheckIn;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MonthlyPlan;
import gym.domain.NotFoundException;
import gym.domain.Notification;
import gym.domain.OffPeakPlan;
import gym.domain.RuleViolationException;
import gym.port.out.CheckInRepository;
import gym.service.BillingServiceTest.InMemoryMemberRepository;
import gym.service.BillingServiceTest.InMemoryMembershipRepository;
import gym.support.InMemoryRepository;
import gym.support.TestClock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The Member feature: viewing a profile and checking in. The rule itself ({@link Membership#canCheckIn})
 * belongs to Membership's State and Plan; these tests show MemberService asks it rather than deciding
 * itself, and that a successful check-in notifies the member (Observer, via {@code notifications::add}).
 */
class MemberServiceTest {

    private static final LocalDate JOINED = LocalDate.of(2026, 11, 2);

    static class InMemoryCheckInRepository extends InMemoryRepository<CheckIn, String> implements CheckInRepository {
        InMemoryCheckInRepository() {
            super(CheckIn::checkInId);
        }
    }

    private final InMemoryMemberRepository members = new InMemoryMemberRepository();
    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final InMemoryCheckInRepository checkIns = new InMemoryCheckInRepository();
    private final List<Notification> notifications = new ArrayList<>();
    private final TestClock clock = new TestClock(JOINED);

    private final MemberService service = new MemberService(members, memberships, checkIns, notifications::add, clock);

    @Test
    void checkingInRecordsAVisitAndNotifiesTheMember() {
        Membership active = activeMembership("m1", new MonthlyPlan());

        CheckIn checkIn = service.checkIn("m1");

        assertEquals("m1", checkIn.memberId());
        assertEquals(active.membershipId(), checkIn.membershipId());
        assertTrue(checkIns.findById(checkIn.checkInId()).isPresent());
        assertEquals("Checked in", notifications.getLast().subject());
    }

    @Test
    void aFrozenMembershipRefusesCheckIn() {
        Membership frozen = activeMembership("m1", new MonthlyPlan()).freeze(1, JOINED);
        memberships.save(frozen);

        assertThrows(RuleViolationException.class, () -> service.checkIn("m1"));
        assertTrue(checkIns.findAll().isEmpty());
    }

    @Test
    void anOffPeakPlanRefusesCheckInOutsideItsHours() {
        // BR-M1: Off-Peak only allows 10:00-16:00; the clock starts the day at 09:00
        activeMembership("m1", new OffPeakPlan());

        assertThrows(RuleViolationException.class, () -> service.checkIn("m1"));
    }

    @Test
    void checkingInForAnUnknownMemberIsRefused() {
        assertThrows(NotFoundException.class, () -> service.checkIn("nobody"));
    }

    @Test
    void checkingInWithoutACurrentMembershipIsRefused() {
        members.save(new Member("m1", "Ann", "ann@example.com", null, null, null));

        assertThrows(NotFoundException.class, () -> service.checkIn("m1"));
    }

    @Test
    void historyListsCheckInsOldestFirst() {
        activeMembership("m1", new MonthlyPlan());

        CheckIn first = service.checkIn("m1");
        clock.advanceDays(1);
        CheckIn second = service.checkIn("m1");

        assertEquals(List.of(first, second), service.history("m1"));
    }

    @Test
    void viewingAnUnknownMemberIsRefused() {
        assertThrows(NotFoundException.class, () -> service.member("nobody"));
    }

    private Membership activeMembership(String memberId, gym.domain.Plan plan) {
        members.save(new Member(memberId, "Ann", memberId + "@example.com", null, null, null));
        Membership active = Membership.signUp("ms-" + memberId, memberId, plan, JOINED).endTrial(JOINED.plusDays(7));
        return memberships.save(active);
    }
}
