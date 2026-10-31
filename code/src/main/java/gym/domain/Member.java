package gym.domain;

import java.util.Objects;

/**
 * A person who holds a membership.
 *
 * @param memberId the member id
 * @param name their full name
 * @param email where notifications go
 * @param studentId their student ID for the Student plan, or {@code null}
 * @param employer their employer for the Corporate discount (BR-P2), or {@code null}
 * @param familyId shared by members of one family for the Family discount (BR-P2), or {@code null}
 */
public record Member(String memberId, String name, String email, String studentId, String employer, String familyId) {

    public Member {
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(email, "email");
    }

    public boolean isCorporate() {
        return employer != null && !employer.isBlank();
    }

    public boolean hasFamily() {
        return familyId != null && !familyId.isBlank();
    }
}
