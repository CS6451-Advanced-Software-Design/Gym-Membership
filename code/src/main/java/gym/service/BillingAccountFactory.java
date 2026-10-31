package gym.service;

import gym.domain.BillingAccount;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.NotFoundException;
import gym.domain.PlanType;
import gym.port.out.MemberRepository;
import gym.port.out.MembershipRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/**
 * Gathers what Billing needs from a membership and its member, as of today: a simple factory for
 * {@link BillingAccount}, the same way {@code PlanFactory} is a simple factory for a {@code Plan}.
 */
@Component
public class BillingAccountFactory {

    private final MembershipRepository memberships;
    private final MemberRepository members;
    private final Clock clock;

    public BillingAccountFactory(MembershipRepository memberships, MemberRepository members, Clock clock) {
        this.memberships = memberships;
        this.members = members;
        this.clock = clock;
    }

    public BillingAccount account(String membershipId) {
        Membership membership = memberships.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Membership", membershipId));
        Member member = member(membership.memberId());
        return new BillingAccount(
                membershipId,
                member.memberId(),
                membership.plan().price(),
                membership.plan() == PlanType.ANNUAL,
                member.isCorporate(),
                hasFamilyWhoJoinedEarlier(member, membership),
                ChronoUnit.MONTHS.between(membership.startDate(), LocalDate.now(clock)),
                membership.status() == MembershipStatus.FROZEN);
    }

    /**
     * BR-P2: the Family discount is for the second and later members of a family.
     */
    private boolean hasFamilyWhoJoinedEarlier(Member member, Membership membership) {
        if (!member.hasFamily()) {
            return false;
        }
        for (Membership other : memberships.findAll()) {
            boolean sameFamily = member.familyId().equals(member(other.memberId()).familyId());
            boolean someoneElse = !other.memberId().equals(member.memberId());
            if (sameFamily && someoneElse && other.startDate().isBefore(membership.startDate())) {
                return true;
            }
        }
        return false;
    }

    private Member member(String memberId) {
        return members.findById(memberId).orElseThrow(() -> new NotFoundException("Member", memberId));
    }
}
