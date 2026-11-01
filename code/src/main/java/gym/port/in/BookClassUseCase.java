package gym.port.in;

import gym.domain.BookingResult;

/**
 * UC2: Book Class. Gives a member a place in a class, or a place on its waitlist when it is full.
 */
public interface BookClassUseCase {

    /**
     * Books the member into the class, or adds them to the waitlist if the class is full (BR-B1).
     *
     * <p>Pre: the class exists and hasn't started, the member's current membership can book, and the member
     * doesn't already hold a place in the class. Post: a BOOKED booking is stored for the member and counted against a Trial, or the member
     * is last on the class's waitlist.
     *
     * @return the booking, or the member's position on the waitlist
     * @throws gym.domain.NotFoundException if there is no such class, or the member holds no current membership
     * @throws gym.domain.RuleViolationException if the class has started, the membership's state refuses the
     *         booking, or the member already holds a place or is already waiting
     */
    BookingResult book(String memberId, String classId);
}
