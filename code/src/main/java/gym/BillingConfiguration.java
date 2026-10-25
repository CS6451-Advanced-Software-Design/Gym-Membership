package gym;

import gym.domain.BestDiscount;
import gym.domain.CorporateDiscount;
import gym.domain.DiscountPolicy;
import gym.domain.FamilyDiscount;
import gym.domain.LoyaltyDiscount;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the discount strategies Billing uses (BR-P2). A new discount is one more entry here.
 */
@Configuration
public class BillingConfiguration {

    @Bean
    public DiscountPolicy discountPolicy() {
        return new BestDiscount(List.of(new CorporateDiscount(), new FamilyDiscount(), new LoyaltyDiscount()));
    }
}
