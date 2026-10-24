package gym.adapter.payment;

import gym.domain.Money;
import gym.port.out.PaymentGateway;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Driven adapter that stands in for a real card provider, as the spec allows a simulated payment system.
 * Every payment succeeds unless the member is listed under {@code gym.payment.declined-members},
 * so a demo or test can show the failed-payment rules (BR-M4) on purpose.
 */
@Component
public class FakePaymentGateway implements PaymentGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakePaymentGateway.class);

    /**
     * A payment that went through.
     */
    public record Payment(String memberId, Money amount) {
    }

    private final Set<String> declinedMembers;
    private final List<Payment> payments = new ArrayList<>();

    public FakePaymentGateway(PaymentProperties properties) {
        this.declinedMembers = properties.declinedMembers();
    }

    @Override
    public synchronized boolean charge(String memberId, Money amount) {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("A payment must be positive, not " + amount);
        }
        if (declinedMembers.contains(memberId)) {
            LOGGER.info("declined {} for member {}", amount, memberId);
            return false;
        }
        payments.add(new Payment(memberId, amount));
        LOGGER.info("charged  {} to member {}", amount, memberId);
        return true;
    }

    /**
     * The payments that went through, oldest first.
     */
    public synchronized List<Payment> payments() {
        return List.copyOf(payments);
    }
}
