package gym.port.in;

import gym.domain.Booking;

/**
 * UC2: Book Class. Gives a member a place in a class.
 */
public interface BookClassUseCase {

    /**
     * Books the member into the class.
     *
     * <p>Pre: the class exists. Post: a BOOKED booking is stored for the member.
     *
     * @throws gym.domain.NotFoundException if there is no such class
     */
    Booking book(String memberId, String classId);
}