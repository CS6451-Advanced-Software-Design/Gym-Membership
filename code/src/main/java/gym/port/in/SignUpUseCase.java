package gym.port.in;

import gym.domain.Invoice;
import gym.domain.Membership;
import gym.domain.PlanType;

/**
 * UC1 Sign Up for Membership.
 */
public interface SignUpUseCase {

    /**
     * Someone joining the gym.
     *
     * @param name their full name
     * @param email where notifications go, and how a returning member is recognised
     * @param studentId their student ID, needed for the Student plan, or {@code null}
     * @param employer their employer, for the Corporate discount, or {@code null}
     * @param familyId shared with family members, for the Family discount, or {@code null}
     */
    record Applicant(String name, String email, String studentId, String employer, String familyId) {
    }

    /**
     * The new membership and the invoice for its first charge.
     */
    record SignUp(Membership membership, Invoice firstInvoice) {
    }

    /**
     * Creates the membership in Trial, or Active for an Annual plan (BR-M2), and takes the first charge:
     * pro-rated by the days left in the month, minus the best discount (BR-P2, BR-P4). A declined first charge
     * still creates the membership, and its trial ends Lapsed. A referral code is the referrer's membership id.
     * Once the first charge is paid, the referrer is credited €20 (BR-P3).
     *
     * <p>Pre: the applicant holds no membership that isn't cancelled. Post: the member, the membership and the
     * first invoice are stored and the member is sent a welcome.
     *
     * @param referralCode the referrer's membership id, or {@code null}
     * @throws gym.domain.RuleViolationException if the applicant already has a membership, isn't eligible for
     *         the plan, or the referral code doesn't belong to a membership that isn't cancelled
     */
    SignUp signUp(Applicant applicant, PlanType plan, String referralCode);
}
