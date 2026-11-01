package gym.service;

import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.Member;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.NotFoundException;
import gym.domain.Notification;
import gym.domain.Plan;
import gym.domain.PlanFactory;
import gym.domain.PlanType;
import gym.domain.RuleViolationException;
import gym.port.in.CalculateChargesUseCase;
import gym.port.in.CancelMembershipUseCase;
import gym.port.in.FreezeMembershipUseCase;
import gym.port.in.PayArrearsUseCase;
import gym.port.in.RecordChargeUseCase;
import gym.port.in.RestoreMembershipUseCase;
import gym.port.in.SignUpUseCase;
import gym.port.in.ViewMembershipUseCase;
import gym.port.out.MemberRepository;
import gym.port.out.MembershipRepository;
import gym.port.out.NotificationSender;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Membership feature: signs members up and moves their memberships through the state chart. The rules
 * of each state are in the domain (State pattern), and the plans come from {@link PlanFactory}. Money is
 * left to Billing. The daily sweep across every membership (UC13) is {@link MembershipDailyChecksService},
 * a separate batch job rather than an operation on the one membership a request names.
 */
@Service
public class MembershipService implements SignUpUseCase, ViewMembershipUseCase, FreezeMembershipUseCase,
        CancelMembershipUseCase, RestoreMembershipUseCase {

    private final MembershipRepository memberships;
    private final MemberRepository members;
    private final CalculateChargesUseCase billing;
    private final RecordChargeUseCase charges;
    private final PayArrearsUseCase arrears;
    private final NotificationSender notifications;
    private final Clock clock;

    public MembershipService(
            MembershipRepository memberships,
            MemberRepository members,
            CalculateChargesUseCase billing,
            RecordChargeUseCase charges,
            PayArrearsUseCase arrears,
            NotificationSender notifications,
            Clock clock) {
        this.memberships = memberships;
        this.members = members;
        this.billing = billing;
        this.charges = charges;
        this.arrears = arrears;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Override
    public SignUp signUp(Applicant applicant, PlanType type, String referralCode) {
        LocalDate today = LocalDate.now(clock);
        Plan plan = PlanFactory.create(type);

        Optional<Member> returning = members.findByEmail(applicant.email());
        returning.ifPresent(this::checkHasNoMembership);
        String memberId = returning.map(Member::memberId).orElseGet(() -> newId("m-"));
        Member member = new Member(memberId, applicant.name(), applicant.email(),
                applicant.studentId(), applicant.employer(), applicant.familyId());
        plan.checkEligible(member);
        Optional<Membership> referrer = referrer(referralCode);

        members.save(member);
        Membership membership = memberships.save(Membership.signUp(newId("ms-"), memberId, plan, today));
        Invoice first = billing.billFirstMonth(membership.membershipId(), today);
        boolean paid = first.status() == InvoiceStatus.PAID;
        membership = memberships.save(membership.recordPayment(paid, today));

        if (paid) {
            referrer.ifPresent(other -> charges.creditReferral(other.membershipId()));
        }
        notifications.send(new Notification(memberId, "Welcome", welcome(membership, paid)));
        return new SignUp(membership, first);
    }

    @Override
    public List<Plan> plans() {
        return PlanFactory.all();
    }

    @Override
    public Membership membership(String membershipId) {
        return memberships.findById(membershipId).orElseThrow(() -> new NotFoundException("Membership", membershipId));
    }

    @Override
    public Membership freeze(String membershipId, int months) {
        Membership frozen = memberships.save(membership(membershipId).freeze(months, LocalDate.now(clock)));
        notifications.send(new Notification(frozen.memberId(), "Membership frozen",
                "Your membership is frozen until " + frozen.currentFreeze().endDate() + ". You won't be billed until then."));
        return frozen;
    }

    @Override
    public Membership unfreeze(String membershipId) {
        Membership active = memberships.save(membership(membershipId).unfreeze());
        notifications.send(new Notification(active.memberId(), "Membership unfrozen", "Welcome back. Your membership is Active again."));
        return active;
    }

    @Override
    public Cancellation cancel(String membershipId) {
        Membership cancelled = membership(membershipId).cancel();
        Invoice last = billing.billFinal(membershipId);
        memberships.save(cancelled);
        notifications.send(new Notification(cancelled.memberId(), "Membership cancelled", "Your membership is cancelled. Sorry to see you go."));
        return new Cancellation(cancelled, last);
    }

    @Override
    public Membership payArrears(String membershipId) {
        Membership restored = membership(membershipId).payArrears();
        if (arrears.payArrears(membershipId).isEmpty()) {
            throw new RuleViolationException("The card was declined, so membership " + membershipId + " still owes its arrears");
        }
        return memberships.save(restored);
    }

    /**
     * UC1 precondition: someone who left can join again, but no one holds two memberships at once.
     */
    private void checkHasNoMembership(Member member) {
        for (Membership membership : memberships.findAll()) {
            if (membership.memberId().equals(member.memberId()) && membership.status() != MembershipStatus.CANCELLED) {
                throw new RuleViolationException(member.email() + " already has membership " + membership.membershipId());
            }
        }
    }

    /**
     * UC1 extension 3b: the referral code is the referrer's membership id.
     */
    private Optional<Membership> referrer(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(memberships.findById(referralCode)
                .filter(membership -> membership.status() != MembershipStatus.CANCELLED)
                .orElseThrow(() -> new RuleViolationException("Referral code " + referralCode + " doesn't belong to a current membership")));
    }

    private static String welcome(Membership membership, boolean paid) {
        if (!paid && membership.status() == MembershipStatus.TRIAL) {
            return "Your first payment was declined. Please update your card before your trial ends on "
                    + membership.trialEndsOn() + ", or your membership will lapse.";
        }
        if (!paid) {
            return "Your first payment was declined. Please update your card and pay what you owe.";
        }
        if (membership.status() == MembershipStatus.TRIAL) {
            return "Your trial runs until " + membership.trialEndsOn() + " and includes "
                    + Membership.TRIAL_BOOKINGS + " class bookings.";
        }
        return "Your membership is active until " + membership.endDate() + ".";
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
