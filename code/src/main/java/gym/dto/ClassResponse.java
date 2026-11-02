package gym.dto;

import gym.domain.GymClass;
import java.time.LocalDateTime;

/**
 * A class on the timetable.
 *
 * @param classId the class id
 * @param name what the class is
 * @param startTime when it starts
 * @param capacity how many places it has
 * @param premium whether it carries the €8 surcharge
 * @param placesLeft how many places can still be booked
 * @param waitlistSize how many members are waiting for a place
 */
public record ClassResponse(
        String classId, String name, LocalDateTime startTime, int capacity, boolean premium, int placesLeft, int waitlistSize) {

    public static ClassResponse from(GymClass gymClass, int placesLeft) {
        return new ClassResponse(gymClass.classId(), gymClass.name(), gymClass.startTime(), gymClass.capacity(),
                gymClass.premium(), placesLeft, gymClass.waitlist().size());
    }
}
