package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.AnnualPlan;
import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.BookingStatus;
import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.Money;
import gym.domain.MonthlyPlan;
import gym.domain.NotFoundException;
import gym.domain.OffPeakPlan;
import gym.domain.RuleViolationException;
import gym.port.in.RecordChargeUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.service.BillingServiceTest.InMemoryMembershipRepository;
import gym.support.InMemoryRepository;
import gym.support.TestClock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime SPIN_STARTS = LocalDateTime.of(2026, 11, 6, 18, 0);

    static class InMemoryGymClassRepository extends InMemoryRepository<GymClass, String> implements GymClassRepository {
        InMemoryGymClassRepository() {
            super(GymClass::classId);
        }
    }

    static class InMemoryBookingRepository extends InMemoryRepository<Booking, String> implements BookingRepository {
        InMemoryBookingRepository() {
            super(Booking::bookingId);
        }
    }

    /**
     * Stands in for Billing, keeping each charge so a test can look at it.
     */
    static class RecordedCharges extends ArrayList<Charge> implements RecordChargeUseCase {
        @Override
        public Charge record(String membershipId, ChargeType type, Money amount) {
            Charge charge = Charge.pending("ch" + size(), membershipId, type, amount, TODAY);
            add(charge);
            return charge;
        }

        @Override
        public Money creditReferral(String membershipId) {
            return Money.ZERO;
        }
    }

    private final InMemoryGymClassRepository classes = new InMemoryGymClassRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final RecordedCharges charges = new RecordedCharges();
    private final TestClock clock = new TestClock(TODAY);

    private final BookingService service = new BookingService(classes, bookings, memberships, charges, clock);

    @Test
    void bookingAClassStoresAPlaceForTheMember() {
        spinClass(10);
        activeMembership("m1");

        BookingResult result = service.book("m1", "c1");

        assertFalse(result.isWaitlisted());
        Booking booking = result.booking();
        assertEquals("m1", booking.memberId());
        assertEquals("c1", booking.classId());
        assertEquals(BookingStatus.BOOKED, booking.status());
        assertEquals(TODAY.atTime(9, 0), booking.bookedAt());
        assertTrue(bookings.findById(booking.bookingId()).isPresent());
    }

    @Test
    void bookingAnUnknownClassIsRefused() {
        activeMembership("m1");

        assertThrows(NotFoundException.class, () -> service.book("m1", "nobody"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aClassThatHasStartedCannotBeBooked() {
        spinClass(10);
        activeMembership("m1");
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberWithoutACurrentMembershipCannotBook() {
        spinClass(10);

        assertThrows(NotFoundException.class, () -> service.book("m1", "c1"));
    }

    @Test
    void aFrozenMembershipCannotBook() {
        spinClass(10);
        memberships.save(activeMembership("m1").freeze(1, TODAY));

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberCannotBookTheSameClassTwice() {
        spinClass(10);
        activeMembership("m1");
        service.book("m1", "c1");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertEquals(1, bookings.findAll().size());
    }

    @Test
    void aMemberCanRebookAfterCancelling() {
        spinClass(10);
        activeMembership("m1");
        bookings.save(service.book("m1", "c1").booking().cancel());

        Booking again = service.book("m1", "c1").booking();

        assertEquals(BookingStatus.BOOKED, again.status());
    }

    @Test
    void aFullClassPutsTheMemberOnTheWaitlist() {
        // BR-B1: one place, so the second member has to wait
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        service.book("m1", "c1");

        BookingResult result = service.book("m2", "c1");

        assertTrue(result.isWaitlisted());
        assertEquals(1, result.waitlistPosition());
        assertEquals(1, bookings.findAll().size());
        assertEquals(List.of("m2"), classes.findById("c1").orElseThrow().waitlist().memberIds());
    }

    @Test
    void theWaitlistIsFirstComeFirstServed() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        activeMembership("m3");
        service.book("m1", "c1");

        service.book("m3", "c1");
        BookingResult second = service.book("m2", "c1");

        assertEquals(2, second.waitlistPosition());
        assertEquals(List.of("m3", "m2"), classes.findById("c1").orElseThrow().waitlist().memberIds());
    }

    @Test
    void aMemberCannotJoinTheWaitlistTwice() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        service.book("m1", "c1");
        service.book("m2", "c1");

        assertThrows(RuleViolationException.class, () -> service.book("m2", "c1"));
    }

    @Test
    void aCancelledBookingFreesItsPlace() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        bookings.save(service.book("m1", "c1").booking().cancel());

        BookingResult result = service.book("m2", "c1");

        assertFalse(result.isWaitlisted());
    }

    @Test
    void aTrialMemberCanBookOnlyTwoClasses() {
        // BR-M2: the third booking is refused, and nothing is stored for it
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        trialMembership("m1");

        service.book("m1", "c1");
        service.book("m1", "c2");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c3"));
        assertEquals(2, bookings.findAll().size());
        assertEquals(2, memberships.findById("ms-m1").orElseThrow().trialBookingsUsed());
    }

    @Test
    void aCancelledBookingStillCountsAgainstTheTrial() {
        // BR-B3: cancelling doesn't give a Trial member the booking back
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        trialMembership("m1");
        bookings.save(service.book("m1", "c1").booking().cancel());
        service.book("m1", "c2");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c3"));
    }

    @Test
    void joiningTheWaitlistDoesNotUseATrialBooking() {
        // BR-M2 counts bookings, and a member on the waitlist doesn't hold one yet
        aClass("c1", 1);
        activeMembership("m1");
        trialMembership("m2");
        service.book("m1", "c1");

        service.book("m2", "c1");

        assertEquals(0, memberships.findById("ms-m2").orElseThrow().trialBookingsUsed());
    }

    @Test
    void anActiveMemberHasNoTrialLimit() {
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        activeMembership("m1");

        service.book("m1", "c1");
        service.book("m1", "c2");
        service.book("m1", "c3");

        assertEquals(3, bookings.findAll().size());
    }

    private void aClass(String classId, int capacity) {
        classes.save(GymClass.schedule(classId, "Spin", SPIN_STARTS, capacity, false));
    }

    private Membership trialMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY));
    }

    @Test
    void anOffPeakMemberCanBookAClassInOffPeakHours() {
        // BR-B5: Friday at 11:00 is inside Off-Peak's 10:00-16:00
        classes.save(GymClass.schedule("c1", "Yoga", SPIN_STARTS.withHour(11), 10, false));
        offPeakMembership("m1");

        assertFalse(service.book("m1", "c1").isWaitlisted());
    }

    @Test
    void anOffPeakMemberCannotBookAClassOutsideOffPeakHours() {
        // BR-B5: Friday at 18:00 is outside Off-Peak's hours
        aClass("c1", 10);
        offPeakMembership("m1");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberCanHoldAtMostFiveFutureBookings() {
        // BR-B5: the sixth is refused, and nothing is stored for it
        fiveBookings("m1");
        aClass("c6", 10);

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c6"));
        assertEquals(5, bookings.findAll().size());
    }

    @Test
    void aMemberAtTheLimitCannotJoinAWaitlistEither() {
        fiveBookings("m1");
        aClass("full", 1);
        activeMembership("m2");
        service.book("m2", "full");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "full"));
        assertTrue(classes.findById("full").orElseThrow().waitlist().memberIds().isEmpty());
    }

    @Test
    void aCancelledBookingDoesNotCountTowardsTheLimit() {
        fiveBookings("m1");
        Booking first = bookings.findAll().getFirst();
        bookings.save(first.cancel());
        aClass("c6", 10);

        assertFalse(service.book("m1", "c6").isWaitlisted());
    }

    @Test
    void aClassThatHasStartedDoesNotCountTowardsTheLimit() {
        classes.save(GymClass.schedule("today", "Spin", TODAY.atTime(18, 0), 10, false));
        activeMembership("m1");
        service.book("m1", "today");
        clock.advanceDays(1);
        for (int number = 1; number <= 5; number++) {
            aClass("c" + number, 10);
            service.book("m1", "c" + number);
        }

        assertEquals(6, bookings.findAll().size());
    }

    private void fiveBookings(String memberId) {
        activeMembership(memberId);
        for (int number = 1; number <= Booking.MAX_FUTURE_BOOKINGS; number++) {
            aClass("c" + number, 10);
            service.book(memberId, "c" + number);
        }
    }

    private void offPeakMembership(String memberId) {
        memberships.save(Membership.signUp("ms-" + memberId, memberId, new OffPeakPlan(), TODAY).endTrial(TODAY.plusDays(7)));
    }

    @Test
    void aPremiumClassCostsAnEightEuroSurcharge() {
        // BR-B6
        premiumClass(10);
        activeMembership("m1");

        service.book("m1", "premium");

        assertEquals(1, charges.size());
        Charge surcharge = charges.getFirst();
        assertEquals("ms-m1", surcharge.membershipId());
        assertEquals(ChargeType.PREMIUM_SURCHARGE, surcharge.type());
        assertEquals(Money.euros(8), surcharge.amount());
    }

    @Test
    void anAnnualMemberPaysNoSurcharge() {
        // BR-B6: Annual is the one exemption
        premiumClass(10);
        memberships.save(Membership.signUp("ms-m1", "m1", new AnnualPlan(), TODAY));

        service.book("m1", "premium");

        assertEquals(1, bookings.findAll().size());
        assertTrue(charges.isEmpty());
    }

    @Test
    void aNormalClassHasNoSurcharge() {
        spinClass(10);
        activeMembership("m1");

        service.book("m1", "c1");

        assertTrue(charges.isEmpty());
    }

    @Test
    void aTrialMemberPaysTheSurchargeToo() {
        premiumClass(10);
        trialMembership("m1");

        service.book("m1", "premium");

        assertEquals(1, charges.size());
    }

    @Test
    void joiningAPremiumWaitlistCostsNothingYet() {
        // The surcharge goes with a place, so a waiting member pays it when promoted
        premiumClass(1);
        memberships.save(Membership.signUp("ms-m1", "m1", new AnnualPlan(), TODAY));
        activeMembership("m2");
        service.book("m1", "premium");

        service.book("m2", "premium");

        assertTrue(charges.isEmpty());
    }

    @Test
    void aRefusedBookingLeavesNoSurchargeBehind() {
        premiumClass(10);
        memberships.save(activeMembership("m1").freeze(1, TODAY));

        assertThrows(RuleViolationException.class, () -> service.book("m1", "premium"));
        assertTrue(charges.isEmpty());
    }

    private void premiumClass(int capacity) {
        classes.save(GymClass.schedule("premium", "Reformer Pilates", SPIN_STARTS, capacity, true));
    }

    @Test
    void theLastPlaceGoesToOnlyOneMember() throws Exception {
        // Reading is slowed down so the requests overlap: unprotected, they would all see a free place
        InMemoryBookingRepository slowBookings = new InMemoryBookingRepository() {
            @Override
            public List<Booking> findAll() {
                pause();
                return super.findAll();
            }
        };
        BookingService racing = new BookingService(classes, slowBookings, memberships, charges, clock);
        spinClass(1);
        List<String> members = List.of("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8");
        members.forEach(this::activeMembership);

        try (ExecutorService threads = Executors.newFixedThreadPool(members.size())) {
            List<Callable<BookingResult>> requests = new ArrayList<>();
            members.forEach(member -> requests.add(() -> racing.book(member, "c1")));
            for (Future<BookingResult> request : threads.invokeAll(requests)) {
                request.get();
            }
        }

        assertEquals(1, slowBookings.findAll().size());
        assertEquals(7, classes.findById("c1").orElseThrow().waitlist().size());
    }

    private static void pause() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void cancellingABookingStoresItAsCancelled() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();

        Booking cancelled = service.cancel(booking.bookingId());

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertEquals(Optional.of(cancelled), bookings.findById(booking.bookingId()));
    }

    @Test
    void cancellingFreesThePlaceForAnotherMember() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        Booking booking = service.book("m1", "c1").booking();

        service.cancel(booking.bookingId());

        assertFalse(service.book("m2", "c1").isWaitlisted());
    }

    @Test
    void cancellingAnUnknownBookingIsRefused() {
        assertThrows(NotFoundException.class, () -> service.cancel("nobody"));
    }

    @Test
    void aBookingCannotBeCancelledTwice() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        service.cancel(booking.bookingId());

        assertThrows(RuleViolationException.class, () -> service.cancel(booking.bookingId()));
    }

    @Test
    void aBookingCannotBeCancelledOnceItsClassHasStarted() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> service.cancel(booking.bookingId()));
        assertEquals(BookingStatus.BOOKED, bookings.findById(booking.bookingId()).orElseThrow().status());
    }

    @Test
    void cancellingUnderTwoHoursBeforeTheClassCostsFiveEuros() {
        // BR-B3: the clock says 09:00 and the class starts at 10:00
        classAt(TODAY.atTime(10, 0));
        activeMembership("m1");
        Booking booking = service.book("m1", "soon").booking();

        service.cancel(booking.bookingId());

        assertEquals(1, charges.size());
        Charge fee = charges.getFirst();
        assertEquals("ms-m1", fee.membershipId());
        assertEquals(ChargeType.LATE_CANCELLATION, fee.type());
        assertEquals(Money.euros(5), fee.amount());
    }

    @Test
    void cancellingExactlyTwoHoursBeforeTheClassIsFree() {
        // BR-B3 says less than 2 hours, so 09:00 for an 11:00 class isn't late
        classAt(TODAY.atTime(11, 0));
        activeMembership("m1");
        Booking booking = service.book("m1", "soon").booking();

        service.cancel(booking.bookingId());

        assertTrue(charges.isEmpty());
    }

    @Test
    void cancellingInGoodTimeIsFree() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();

        service.cancel(booking.bookingId());

        assertTrue(charges.isEmpty());
    }

    @Test
    void aTrialMemberPaysNoLateCancellationFee() {
        // BR-B3: no fee for a Trial, but the booking still counts against its 2
        classAt(TODAY.atTime(10, 0));
        trialMembership("m1");
        Booking booking = service.book("m1", "soon").booking();

        service.cancel(booking.bookingId());

        assertTrue(charges.isEmpty());
        assertEquals(1, memberships.findById("ms-m1").orElseThrow().trialBookingsUsed());
    }

    @Test
    void aRefusedCancellationCostsNothing() {
        classAt(TODAY.atTime(10, 0));
        activeMembership("m1");
        Booking booking = service.book("m1", "soon").booking();
        service.cancel(booking.bookingId());

        assertThrows(RuleViolationException.class, () -> service.cancel(booking.bookingId()));
        assertEquals(1, charges.size());
    }

    @Test
    void aMemberWhoLeftTheGymCanStillCancelWithoutAFee() {
        classAt(TODAY.atTime(10, 0));
        Membership membership = activeMembership("m1");
        Booking booking = service.book("m1", "soon").booking();
        memberships.save(membership.cancel());

        service.cancel(booking.bookingId());

        assertTrue(charges.isEmpty());
        assertEquals(BookingStatus.CANCELLED, bookings.findById(booking.bookingId()).orElseThrow().status());
    }

    private void classAt(LocalDateTime start) {
        classes.save(GymClass.schedule("soon", "Spin", start, 10, false));
    }

    @Test
    void cancellingPromotesTheFirstMemberOnTheWaitlist() {
        // BR-B2: m2 waited longest, so m2 gets the place and m3 moves up
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        activeMembership("m3");
        Booking booking = service.book("m1", "c1").booking();
        service.book("m2", "c1");
        service.book("m3", "c1");

        service.cancel(booking.bookingId());

        Booking promoted = placeHeldBy("m2");
        assertEquals("c1", promoted.classId());
        assertEquals(TODAY.atTime(9, 0), promoted.bookedAt());
        assertEquals(List.of("m3"), classes.findById("c1").orElseThrow().waitlist().memberIds());
    }

    @Test
    void aPromotedTrialMemberUsesOneOfTheirBookings() {
        // BR-M2: joining the waitlist cost nothing, but getting the place does
        spinClass(1);
        activeMembership("m1");
        trialMembership("m2");
        Booking booking = service.book("m1", "c1").booking();
        service.book("m2", "c1");

        service.cancel(booking.bookingId());

        assertEquals(1, memberships.findById("ms-m2").orElseThrow().trialBookingsUsed());
    }

    @Test
    void aPromotedMemberPaysThePremiumSurcharge() {
        // BR-B6: the surcharge goes with the place
        premiumClass(1);
        memberships.save(Membership.signUp("ms-m1", "m1", new AnnualPlan(), TODAY));
        activeMembership("m2");
        Booking booking = service.book("m1", "premium").booking();
        service.book("m2", "premium");

        service.cancel(booking.bookingId());

        assertEquals(1, charges.size());
        assertEquals("ms-m2", charges.getFirst().membershipId());
        assertEquals(ChargeType.PREMIUM_SURCHARGE, charges.getFirst().type());
    }

    @Test
    void aPromotedAnnualMemberPaysNoSurcharge() {
        premiumClass(1);
        activeMembership("m1");
        memberships.save(Membership.signUp("ms-m2", "m2", new AnnualPlan(), TODAY));
        Booking booking = service.book("m1", "premium").booking();
        charges.clear();
        service.book("m2", "premium");

        service.cancel(booking.bookingId());

        assertTrue(charges.isEmpty());
    }

    @Test
    void aMemberWhoCanNoLongerBookIsSkipped() {
        // m2 froze their membership while waiting, so the place goes to m3
        spinClass(1);
        activeMembership("m1");
        Membership m2 = activeMembership("m2");
        activeMembership("m3");
        Booking booking = service.book("m1", "c1").booking();
        service.book("m2", "c1");
        service.book("m3", "c1");
        memberships.save(m2.freeze(1, TODAY));

        service.cancel(booking.bookingId());

        assertEquals("c1", placeHeldBy("m3").classId());
        assertTrue(classes.findById("c1").orElseThrow().waitlist().memberIds().isEmpty());
        assertEquals(2, bookings.findAll().size());
    }

    @Test
    void aMemberAtTheBookingLimitIsSkipped() {
        // BR-B5: m2 reached 5 future bookings while waiting
        spinClass(1);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        activeMembership("m2");
        service.book("m2", "c1");
        for (int number = 1; number <= Booking.MAX_FUTURE_BOOKINGS; number++) {
            aClass("other" + number, 10);
            service.book("m2", "other" + number);
        }

        service.cancel(booking.bookingId());

        assertTrue(classes.findById("c1").orElseThrow().waitlist().memberIds().isEmpty());
        assertEquals(Booking.MAX_FUTURE_BOOKINGS, bookings.findAll().stream().filter(Booking::isBooked).count());
    }

    @Test
    void anEmptyWaitlistLeavesThePlaceFree() {
        spinClass(1);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();

        service.cancel(booking.bookingId());

        assertTrue(bookings.findAll().stream().noneMatch(Booking::isBooked));
    }

    @Test
    void aLateCancellationStillChargesTheFeeWhenSomeoneIsPromoted() {
        classes.save(GymClass.schedule("soon", "Spin", TODAY.atTime(10, 0), 1, false));
        activeMembership("m1");
        activeMembership("m2");
        Booking booking = service.book("m1", "soon").booking();
        service.book("m2", "soon");

        service.cancel(booking.bookingId());

        assertEquals(ChargeType.LATE_CANCELLATION, charges.getFirst().type());
        assertEquals("ms-m1", charges.getFirst().membershipId());
        assertEquals("soon", placeHeldBy("m2").classId());
    }

    private Booking placeHeldBy(String memberId) {
        return bookings.findAll().stream()
                .filter(booking -> booking.isBooked() && booking.memberId().equals(memberId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void attendingAClassStoresItAsAttended() {
        Booking booking = bookingForAClassThatHasStarted();

        Booking attended = service.markAttended(booking.bookingId());

        assertEquals(BookingStatus.ATTENDED, attended.status());
        assertEquals(Optional.of(attended), bookings.findById(booking.bookingId()));
        assertTrue(charges.isEmpty());
    }

    @Test
    void aNoShowCostsFiveEuros() {
        // BR-B4
        Booking booking = bookingForAClassThatHasStarted();

        Booking noShow = service.markNoShow(booking.bookingId());

        assertEquals(BookingStatus.NO_SHOW, noShow.status());
        assertEquals(Optional.of(noShow), bookings.findById(booking.bookingId()));
        assertEquals(1, charges.size());
        Charge fee = charges.getFirst();
        assertEquals("ms-m1", fee.membershipId());
        assertEquals(ChargeType.NO_SHOW, fee.type());
        assertEquals(Money.euros(5), fee.amount());
    }

    @Test
    void aTrialMemberPaysTheNoShowFee() {
        // BR-B3 exempts a Trial from the late fee only, and BR-B4 has no exemption
        spinClass(10);
        trialMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        clock.advanceDays(5);

        service.markNoShow(booking.bookingId());

        assertEquals(1, charges.size());
    }

    @Test
    void attendanceCannotBeRecordedBeforeTheClassStarts() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();

        assertThrows(RuleViolationException.class, () -> service.markAttended(booking.bookingId()));
        assertThrows(RuleViolationException.class, () -> service.markNoShow(booking.bookingId()));
        assertEquals(BookingStatus.BOOKED, bookings.findById(booking.bookingId()).orElseThrow().status());
        assertTrue(charges.isEmpty());
    }

    @Test
    void attendanceForAnUnknownBookingIsRefused() {
        assertThrows(NotFoundException.class, () -> service.markAttended("nobody"));
        assertThrows(NotFoundException.class, () -> service.markNoShow("nobody"));
    }

    @Test
    void aNoShowCannotBeChargedTwice() {
        Booking booking = bookingForAClassThatHasStarted();
        service.markNoShow(booking.bookingId());

        assertThrows(RuleViolationException.class, () -> service.markNoShow(booking.bookingId()));
        assertEquals(1, charges.size());
    }

    @Test
    void aCancelledBookingCannotBeMarked() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        service.cancel(booking.bookingId());
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> service.markNoShow(booking.bookingId()));
        assertTrue(charges.isEmpty());
    }

    @Test
    void aMemberWhoLeftTheGymIsMarkedWithoutAFee() {
        spinClass(10);
        Membership membership = activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        memberships.save(membership.cancel());
        clock.advanceDays(5);

        Booking noShow = service.markNoShow(booking.bookingId());

        assertEquals(BookingStatus.NO_SHOW, noShow.status());
        assertTrue(charges.isEmpty());
    }

    private Booking bookingForAClassThatHasStarted() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = service.book("m1", "c1").booking();
        clock.advanceDays(5);
        return booking;
    }

    private void spinClass(int capacity) {
        classes.save(GymClass.schedule("c1", "Spin", SPIN_STARTS, capacity, false));
    }

    private Membership activeMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY).endTrial(TODAY.plusDays(7)));
    }
}
