package gym.adapter.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.adapter.payment.FakePaymentGateway.Payment;
import gym.domain.Money;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FakePaymentGatewayTest {

    private final FakePaymentGateway gateway = new FakePaymentGateway(new PaymentProperties(Set.of("m-declined")));

    @Test
    void chargesAMemberOnce() {
        assertTrue(gateway.charge("m-1", Money.euros(45)));

        assertEquals(List.of(new Payment("m-1", Money.euros(45))), gateway.payments());
    }

    @Test
    void declinesListedMembersWithoutTakingMoney() {
        assertFalse(gateway.charge("m-declined", Money.euros(45)));

        assertTrue(gateway.payments().isEmpty());
    }

    @Test
    void refusesAnAmountThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> gateway.charge("m-1", Money.ZERO));
        assertThrows(IllegalArgumentException.class, () -> gateway.charge("m-1", Money.euros(-5)));
    }

    @Test
    void declinesNobodyWhenNothingIsConfigured() {
        FakePaymentGateway unconfigured = new FakePaymentGateway(new PaymentProperties(null));

        assertTrue(unconfigured.charge("m-declined", Money.euros(45)));
    }
}
