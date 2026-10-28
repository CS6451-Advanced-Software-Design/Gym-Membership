package gym.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The members waiting for a place in a full class, first come first served (BR-B1). It never changes.
 * Each operation returns the new waitlist.
 *
 * @param memberIds the waiting members, the one who has waited longest first. A waitlist stored before it
 *                  existed loads as empty
 */
public record Waitlist(List<String> memberIds) {

    public Waitlist {
        memberIds = memberIds == null ? List.of() : List.copyOf(memberIds);
    }

    public static Waitlist empty() {
        return new Waitlist(List.of());
    }

    public int size() {
        return memberIds.size();
    }

    public boolean contains(String memberId) {
        return memberIds.contains(memberId);
    }

    /**
     * Pre: the member is not already waiting. Post: they are last in the line.
     *
     * @throws RuleViolationException if the member is already on the waitlist
     */
    public Waitlist add(String memberId) {
        if (contains(memberId)) {
            throw new RuleViolationException("Member " + memberId + " is already on the waitlist");
        }
        List<String> longer = new ArrayList<>(memberIds);
        longer.add(memberId);
        return new Waitlist(longer);
    }

    /**
     * @return their place in the line, counting from 1, or 0 if they are not waiting
     */
    public int positionOf(String memberId) {
        return memberIds.indexOf(memberId) + 1;
    }

    /**
     * @return the member who has waited longest, or empty if nobody is waiting
     */
    public Optional<String> first() {
        return memberIds.stream().findFirst();
    }

    /**
     * Pre: none. Post: the first member is gone and everyone else has moved up one. An empty waitlist stays empty.
     */
    public Waitlist withoutFirst() {
        return memberIds.isEmpty() ? this : new Waitlist(memberIds.subList(1, memberIds.size()));
    }
}