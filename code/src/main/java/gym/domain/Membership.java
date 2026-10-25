package gym.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A member's membership of one plan, and the context of the State pattern: every operation is handed to
 * the {@link MembershipState} for its {@code status}, which decides what is allowed and what the next state
 * is (the state chart in report/analysis). A membership never changes. Each operation returns the new one.
 *
 * @param membershipId the membership id
 * @param memberId the member who holds it
 * @param plan the plan it is on
 * @param startDate when it started
 * @param status where it is in the state chart
 * @param endDate when it ends, or {@code null} if it rolls on month by month. A freeze moves it (BR-M3)
 * @param failedPayments payments declined in a row (BR-M4). Boxed so a membership stored before it existed loads as 0
 * @param trialBookingsUsed classes booked during the Trial (BR-M2), also boxed
 * @param lapsedSince when it became Lapsed, or {@code null} (BR-M6)
 * @param freezes every freeze so far, oldest first
 */
public record Membership(
        String membershipId,
        String memberId,
        PlanType plan,
        LocalDate startDate,
        MembershipStatus status,
        LocalDate endDate,
        Integer failedPayments,
        Integer trialBookingsUsed,
        LocalDate lapsedSince,
        List<Freeze> freezes) {

    public static final int TRIAL_DAYS = 7;
    public static final int TRIAL_BOOKINGS = 2;

    public Membership {
        Objects.requireNonNull(membershipId, "membershipId");
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(status, "status");
        failedPayments = failedPayments == null ? 0 : failedPayments;
        trialBookingsUsed = trialBookingsUsed == null ? 0 : trialBookingsUsed;
        freezes = freezes == null ? List.of() : List.copyOf(freezes);
    }

    /**
     * A membership in {@code status} with nothing else recorded yet.
     */
    public Membership(String membershipId, String memberId, PlanType plan, LocalDate startDate, MembershipStatus status) {
        this(membershipId, memberId, plan, startDate, status, PlanFactory.create(plan).endDate(startDate), 0, 0, null, List.of());
    }

    /**
     * BR-M2: starts in Trial, or Active for a plan paid upfront.
     */
    public static Membership signUp(String membershipId, String memberId, Plan plan, LocalDate today) {
        MembershipStatus first = plan.hasTrial() ? MembershipStatus.TRIAL : MembershipStatus.ACTIVE;
        return new Membership(membershipId, memberId, plan.type(), today, first);
    }

    public MembershipState state() {
        return MembershipState.of(status);
    }

    public Plan rules() {
        return PlanFactory.create(plan);
    }

    public LocalDate trialEndsOn() {
        return startDate.plusDays(TRIAL_DAYS);
    }

    /**
     * The freeze in force, or {@code null} unless Frozen.
     */
    public Freeze currentFreeze() {
        return status == MembershipStatus.FROZEN && !freezes.isEmpty() ? freezes.getLast() : null;
    }

    public boolean canBook() {
        return state().canBook(this);
    }

    /**
     * Pre: none. Post: {@code true} only if the state lets the member in and so does the plan at {@code at}.
     */
    public boolean canCheckIn(LocalDateTime at) {
        return state().canCheckIn(this) && rules().allowsCheckIn(at);
    }

    /**
     * Counts a booked class against the 2 a Trial allows (BR-M2).
     */
    public Membership recordBooking() {
        return state().recordBooking(this);
    }

    public Membership freeze(int months, LocalDate today) {
        return state().freeze(this, months, today);
    }

    public Membership unfreeze() {
        return state().unfreeze(this);
    }

    public Membership endTrial(LocalDate today) {
        return state().endTrial(this, today);
    }

    public Membership recordPayment(boolean paid, LocalDate today) {
        return state().recordPayment(this, paid, today);
    }

    public Membership payArrears() {
        return state().payArrears(this);
    }

    public Membership cancel() {
        return state().cancel(this);
    }

    public Membership expire(LocalDate today) {
        return state().expire(this, today);
    }

    /**
     * UC13: whatever the Clock should do to this membership today, e.g. end a trial or a freeze that is over.
     */
    public Membership checkOn(LocalDate today) {
        return state().checkOn(this, today);
    }

    Membership moveTo(MembershipStatus next) {
        return new Membership(membershipId, memberId, plan, startDate, next, endDate, failedPayments, trialBookingsUsed, lapsedSince, freezes);
    }

    Membership withFailedPayments(int count) {
        return new Membership(membershipId, memberId, plan, startDate, status, endDate, count, trialBookingsUsed, lapsedSince, freezes);
    }

    Membership withTrialBookingsUsed(int count) {
        return new Membership(membershipId, memberId, plan, startDate, status, endDate, failedPayments, count, lapsedSince, freezes);
    }

    Membership withLapsedSince(LocalDate date) {
        return new Membership(membershipId, memberId, plan, startDate, status, endDate, failedPayments, trialBookingsUsed, date, freezes);
    }

    /**
     * BR-M3: the end date moves forward by the length of the freeze.
     */
    Membership withFreeze(Freeze freeze) {
        List<Freeze> all = new ArrayList<>(freezes);
        all.add(freeze);
        LocalDate movedEnd = endDate == null ? null : endDate.plusMonths(freeze.months());
        return new Membership(membershipId, memberId, plan, startDate, status, movedEnd, failedPayments, trialBookingsUsed, lapsedSince, all);
    }
}
