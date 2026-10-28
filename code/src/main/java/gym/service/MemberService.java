package gym.service;

import gym.domain.CheckIn;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.NotFoundException;
import gym.domain.Notification;
import gym.domain.RuleViolationException;
import gym.port.in.CheckInUseCase;
import gym.port.in.ViewMemberUseCase;
import gym.port.out.CheckInRepository;
import gym.port.out.MemberRepository;
import gym.port.out.MembershipRepository;
import gym.port.out.NotificationSender;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Member feature: shows a member's profile and records their check-ins. The check-in rule itself
 * (state and plan) lives in {@link Membership#canCheckIn}; this service finds the member's current
 * membership, asks it, and notifies the member once a visit is recorded (Observer, via
 * {@link NotificationSender}).
 */
@Service
public class MemberService implements ViewMemberUseCase, CheckInUseCase {

    private final MemberRepository members;
    private final MembershipRepository memberships;
    private final CheckInRepository checkIns;
    private final NotificationSender notifications;
    private final Clock clock;

    public MemberService(
            MemberRepository members,
            MembershipRepository memberships,
            CheckInRepository checkIns,
            NotificationSender notifications,
            Clock clock) {
        this.members = members;
        this.memberships = memberships;
        this.checkIns = checkIns;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Override
    public Member member(String memberId) {
        return members.findById(memberId).orElseThrow(() -> new NotFoundException("Member", memberId));
    }

    @Override
    public CheckIn checkIn(String memberId) {
        Member member = member(memberId);
        Membership membership = currentMembership(member.memberId());
        LocalDateTime now = LocalDateTime.now(clock);
        if (!membership.canCheckIn(now)) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " can't check in right now");
        }
        CheckIn checkIn = checkIns.save(new CheckIn(newId("ci-"), member.memberId(), membership.membershipId(), now));
        notifications.send(new Notification(member.memberId(), "Checked in", "You're checked in. Enjoy your workout."));
        return checkIn;
    }

    @Override
    public List<CheckIn> history(String memberId) {
        member(memberId);
        return checkIns.findAll().stream()
                .filter(checkIn -> checkIn.memberId().equals(memberId))
                .sorted(Comparator.comparing(CheckIn::at))
                .toList();
    }

    /**
     * UC: Check In precondition — the member must hold a membership that isn't cancelled.
     */
    private Membership currentMembership(String memberId) {
        return memberships.findAll().stream()
                .filter(membership -> membership.memberId().equals(memberId) && membership.status() != MembershipStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Current membership for member", memberId));
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
