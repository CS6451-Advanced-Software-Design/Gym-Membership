package gym.domain;

import java.util.regex.Pattern;

/**
 * BR-M1: needs a valid student ID, which is 8 digits on a UL student card.
 */
public class StudentPlan extends Plan {

    private static final Pattern STUDENT_ID = Pattern.compile("\\d{8}");

    public StudentPlan() {
        super(PlanType.STUDENT);
    }

    @Override
    public void checkEligible(Member member) {
        if (member.studentId() == null || !STUDENT_ID.matcher(member.studentId()).matches()) {
            throw new RuleViolationException("The Student plan needs an 8-digit student ID");
        }
    }

    @Override
    public String description() {
        return "Any time, billed monthly. Needs an 8-digit student ID";
    }
}
