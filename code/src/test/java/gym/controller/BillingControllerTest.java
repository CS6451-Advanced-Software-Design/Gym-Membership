package gym.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.Money;
import gym.domain.NotFoundException;
import gym.port.in.RunMonthlyBillingUseCase;
import gym.port.in.ViewInvoicesUseCase;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BillingControllerTest {

    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private final Invoice invoice = Invoice.issue("i1", "m1", NOVEMBER,
            List.of(Charge.pending("c1", "m1", ChargeType.PLAN_FEE, Money.euros(45), LocalDate.of(2026, 11, 1))),
            Money.euros("6.75")).settled(true);

    private final RunMonthlyBillingUseCase billing = period -> period.equals(NOVEMBER) ? List.of(invoice) : List.of();

    private final ViewInvoicesUseCase invoices = membershipId -> {
        if (!membershipId.equals("m1")) {
            throw new NotFoundException("Membership", membershipId);
        }
        return List.of(invoice);
    };

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new BillingController(billing, invoices))
            .setControllerAdvice(new ErrorHandler())
            .build();

    @Test
    void aBillingRunReturnsTheInvoicesItIssued() throws Exception {
        mvc.perform(post("/billing-runs").contentType(MediaType.APPLICATION_JSON).content("{\"period\": \"2026-11\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].membershipId").value("m1"))
                .andExpect(jsonPath("$[0].period").value("2026-11"))
                .andExpect(jsonPath("$[0].discount").value(6.75))
                .andExpect(jsonPath("$[0].total").value(38.25))
                .andExpect(jsonPath("$[0].status").value(InvoiceStatus.PAID.name()))
                .andExpect(jsonPath("$[0].charges[0].type").value(ChargeType.PLAN_FEE.name()));
    }

    @Test
    void aBillingRunNeedsAPeriod() throws Exception {
        mvc.perform(post("/billing-runs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsAMembersInvoices() throws Exception {
        mvc.perform(get("/memberships/m1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("i1"));
    }

    @Test
    void anUnknownMemberIs404() throws Exception {
        mvc.perform(get("/memberships/nobody/invoices")).andExpect(status().isNotFound());
    }
}
