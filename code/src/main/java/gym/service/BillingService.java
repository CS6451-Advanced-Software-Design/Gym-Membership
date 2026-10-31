package gym.service;

import gym.domain.BillingAccount;
import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.DiscountPolicy;
import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Money;
import gym.domain.Notification;
import gym.domain.RuleViolationException;
import gym.port.in.CalculateChargesUseCase;
import gym.port.in.PayArrearsUseCase;
import gym.port.in.RecordChargeUseCase;
import gym.port.in.RunMonthlyBillingUseCase;
import gym.port.in.ViewInvoicesUseCase;
import gym.port.out.ChargeRepository;
import gym.port.out.InvoiceRepository;
import gym.port.out.MembershipRepository;
import gym.port.out.NotificationSender;
import gym.port.out.PaymentGateway;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Billing feature: turns a membership's charges into an invoice, takes the payment and tells the member.
 * What Billing needs from a membership and its member is gathered by {@link BillingAccountFactory}.
 */
@Service
public class BillingService implements CalculateChargesUseCase, PayArrearsUseCase, RecordChargeUseCase, RunMonthlyBillingUseCase, ViewInvoicesUseCase {

    private static final Money REFERRAL_CREDIT = Money.euros(20);
    private static final Money REFERRAL_CAP = Money.euros(60);
    private static final Money EARLY_EXIT_FEE = Money.euros(50);

    private final MembershipRepository memberships;
    private final BillingAccountFactory accounts;
    private final ChargeRepository charges;
    private final InvoiceRepository invoices;
    private final DiscountPolicy discounts;
    private final PaymentGateway payments;
    private final NotificationSender notifications;
    private final Clock clock;

    public BillingService(
            MembershipRepository memberships,
            BillingAccountFactory accounts,
            ChargeRepository charges,
            InvoiceRepository invoices,
            DiscountPolicy discounts,
            PaymentGateway payments,
            NotificationSender notifications,
            Clock clock) {
        this.memberships = memberships;
        this.accounts = accounts;
        this.charges = charges;
        this.invoices = invoices;
        this.discounts = discounts;
        this.payments = payments;
        this.notifications = notifications;
        this.clock = clock;
    }

    /**
     * BR-P4: a monthly plan pays for the days left in the month. BR-M1: an Annual plan pays the year upfront.
     */
    @Override
    public Invoice billFirstMonth(String membershipId, LocalDate joined) {
        BillingAccount account = accounts.account(membershipId);
        YearMonth month = YearMonth.from(joined);
        Money fee = account.price();
        if (!account.annual()) {
            int daysLeft = month.lengthOfMonth() - joined.getDayOfMonth() + 1;
            fee = fee.proRata(daysLeft, month.lengthOfMonth());
        }
        Charge planFee = Charge.pending(newId(), membershipId, ChargeType.PLAN_FEE, fee, joined);
        return bill(account, month, List.of(planFee));
    }

    /**
     * BR-M5: an Annual plan cancelled within its first 6 months also pays a €50 early-exit fee.
     */
    @Override
    public Invoice billFinal(String membershipId) {
        BillingAccount account = accounts.account(membershipId);
        LocalDate today = LocalDate.now(clock);
        List<Charge> extra = new ArrayList<>();
        if (account.annual() && account.monthsAsMember() < 6) {
            extra.add(Charge.pending(newId(), membershipId, ChargeType.EARLY_EXIT, EARLY_EXIT_FEE, today));
        }
        return bill(account, YearMonth.from(today), extra);
    }

    @Override
    public Charge record(String membershipId, ChargeType type, Money amount) {
        accounts.account(membershipId);
        if (type.isCredit()) {
            throw new RuleViolationException("Credits are only given for referrals");
        }
        if (!amount.isPositive()) {
            throw new RuleViolationException("A " + type + " charge must be positive, not " + amount);
        }
        return charges.save(Charge.pending(newId(), membershipId, type, amount, LocalDate.now(clock)));
    }

    /**
     * BR-P3: €20 per referral, but no more than €60 in a calendar year.
     */
    @Override
    public Money creditReferral(String membershipId) {
        accounts.account(membershipId);
        LocalDate today = LocalDate.now(clock);

        Money givenThisYear = Money.ZERO;
        for (Charge charge : chargesFor(membershipId)) {
            if (charge.type() == ChargeType.REFERRAL_CREDIT && charge.date().getYear() == today.getYear()) {
                givenThisYear = givenThisYear.plus(charge.amount().negate());
            }
        }

        Money credit = REFERRAL_CREDIT.min(REFERRAL_CAP.minus(givenThisYear));
        if (!credit.isPositive()) {
            return Money.ZERO;
        }
        charges.save(Charge.pending(newId(), membershipId, ChargeType.REFERRAL_CREDIT, credit.negate(), today));
        return credit;
    }

    /**
     * BR-M3: frozen memberships are skipped. A membership already billed for the month isn't billed again.
     */
    @Override
    public List<Invoice> run(YearMonth period) {
        List<Invoice> issued = new ArrayList<>();
        for (Membership membership : memberships.findAll()) {
            if (membership.status() == MembershipStatus.CANCELLED) {
                continue;
            }
            BillingAccount account = accounts.account(membership.membershipId());
            if (account.frozen() || isBilled(account.membershipId(), period)) {
                continue;
            }
            List<Charge> extra = new ArrayList<>();
            if (!account.annual()) {
                extra.add(Charge.pending(newId(), account.membershipId(), ChargeType.PLAN_FEE, account.price(), LocalDate.now(clock)));
            }
            if (extra.isEmpty() && pendingCharges(account.membershipId()).isEmpty()) {
                continue;
            }
            issued.add(bill(account, period, extra));
        }
        return issued;
    }

    /**
     * BR-M4: pays every unpaid invoice in one payment, or none of them.
     */
    @Override
    public List<Invoice> payArrears(String membershipId) {
        BillingAccount account = accounts.account(membershipId);
        List<Invoice> unpaid = new ArrayList<>();
        Money arrears = Money.ZERO;
        for (Invoice invoice : invoicesFor(membershipId)) {
            if (invoice.status() == InvoiceStatus.UNPAID) {
                unpaid.add(invoice);
                arrears = arrears.plus(invoice.total());
            }
        }
        if (unpaid.isEmpty()) {
            throw new RuleViolationException("Membership " + membershipId + " has no arrears");
        }

        if (!payments.charge(account.memberId(), arrears)) {
            notifications.send(new Notification(account.memberId(), "Payment declined",
                    "We couldn't charge your arrears of " + arrears + ". Please update your card."));
            return List.of();
        }
        List<Invoice> paid = new ArrayList<>();
        for (Invoice invoice : unpaid) {
            paid.add(invoices.save(invoice.settled(true)));
        }
        notifications.send(new Notification(account.memberId(), "Arrears paid", "We charged " + arrears + " to your card."));
        return paid;
    }

    @Override
    public List<Invoice> invoicesFor(String membershipId) {
        accounts.account(membershipId);
        return invoices.findAll().stream()
                .filter(invoice -> invoice.membershipId().equals(membershipId))
                .sorted(Comparator.comparing(Invoice::period).reversed())
                .toList();
    }

    /**
     * BR-P1: the plan fee, minus the best discount, plus penalties, minus referral credits.
     * A credit is used only if it fits within what is owed. Otherwise it waits for the next invoice.
     */
    private Invoice bill(BillingAccount account, YearMonth period, List<Charge> extra) {
        List<Charge> waiting = new ArrayList<>(extra);
        waiting.addAll(pendingCharges(account.membershipId()));

        Money planFees = Money.ZERO;
        Money owed = Money.ZERO;
        List<Charge> onInvoice = new ArrayList<>();
        for (Charge charge : waiting) {
            if (charge.type() == ChargeType.PLAN_FEE) {
                planFees = planFees.plus(charge.amount());
            }
            if (!charge.type().isCredit()) {
                owed = owed.plus(charge.amount());
                onInvoice.add(charge);
            }
        }

        Money discount = discounts.discountFor(account, planFees);
        owed = owed.minus(discount);

        for (Charge charge : waiting) {
            Money credit = charge.amount().negate();
            if (charge.type().isCredit() && !credit.isGreaterThan(owed)) {
                owed = owed.minus(credit);
                onInvoice.add(charge);
            }
        }

        Invoice invoice = Invoice.issue(newId(), account.membershipId(), period, onInvoice, discount);
        for (Charge charge : invoice.charges()) {
            charges.save(charge);
        }
        if (invoice.total().isPositive()) {
            boolean paid = payments.charge(account.memberId(), invoice.total());
            invoice = invoice.settled(paid);
            notifications.send(notice(account, invoice));
        }
        return invoices.save(invoice);
    }

    private Notification notice(BillingAccount account, Invoice invoice) {
        if (invoice.status() == InvoiceStatus.PAID) {
            return new Notification(account.memberId(), "Invoice for " + invoice.period(),
                    "We charged " + invoice.total() + " to your card.");
        }
        return new Notification(account.memberId(), "Payment declined",
                "We couldn't charge " + invoice.total() + " for " + invoice.period() + ". Please update your card.");
    }

    private List<Charge> chargesFor(String membershipId) {
        return charges.findAll().stream()
                .filter(charge -> charge.membershipId().equals(membershipId))
                .toList();
    }

    private List<Charge> pendingCharges(String membershipId) {
        return chargesFor(membershipId).stream()
                .filter(charge -> !charge.isBilled())
                .toList();
    }

    private boolean isBilled(String membershipId, YearMonth period) {
        return invoicesFor(membershipId).stream().anyMatch(invoice -> invoice.period().equals(period));
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
