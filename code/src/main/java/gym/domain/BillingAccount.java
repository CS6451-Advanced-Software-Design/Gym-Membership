package gym.domain;

/**
 * What Billing needs to know about a membership and its member, worked out on the day of billing.
 *
 * @param membershipId the membership that is billed
 * @param memberId the member who pays and is notified
 * @param price the plan price: per month, or per year for an Annual plan (BR-M1)
 * @param annual {@code true} for an Annual plan, paid upfront
 * @param corporate {@code true} if the member has an employer (BR-P2)
 * @param familyMember {@code true} if someone in the member's family joined earlier (BR-P2)
 * @param monthsAsMember whole months since the membership started (BR-P2 loyalty, BR-M5 early exit)
 * @param frozen {@code true} while the membership is frozen (BR-M3)
 */
public record BillingAccount(
        String membershipId,
        String memberId,
        Money price,
        boolean annual,
        boolean corporate,
        boolean familyMember,
        long monthsAsMember,
        boolean frozen) {
}
