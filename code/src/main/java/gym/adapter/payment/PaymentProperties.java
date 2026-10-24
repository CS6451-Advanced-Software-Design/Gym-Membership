package gym.adapter.payment;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under {@code gym.payment} in application.properties.
 *
 * @param declinedMembers member ids whose card is always declined, to demo failed payments
 */
@ConfigurationProperties("gym.payment")
public record PaymentProperties(Set<String> declinedMembers) {

    public PaymentProperties {
        declinedMembers = declinedMembers == null ? Set.of() : Set.copyOf(declinedMembers);
    }
}
