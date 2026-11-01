package gym.service;

import gym.domain.Invoice;
import gym.domain.InvoiceStatus;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Notification;
import gym.port.in.CalculateChargesUseCase;
import gym.port.in.RunDailyChecksUseCase;
import gym.port.in.ViewInvoicesUseCase;
import gym.port.out.MembershipRepository;
import gym.port.out.NotificationSender;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * UC13: the Clock actor's daily sweep across every membership (ends trials and freezes that are over,
 * lapses unpaid memberships, cancels ones lapsed 60 days). Kept separate from {@link MembershipService}:
 * this is a batch job over every membership, not an operation on the one membership a request names.
 */
@Service
public class MembershipDailyChecksService implements RunDailyChecksUseCase {

    private final MembershipRepository memberships;
    private final ViewInvoicesUseCase invoices;
    private final CalculateChargesUseCase billing;
    private final NotificationSender notifications;
    private final Clock clock;

    public MembershipDailyChecksService(
            MembershipRepository memberships,
            ViewInvoicesUseCase invoices,
            CalculateChargesUseCase billing,
            NotificationSender notifications,
            Clock clock) {
        this.memberships = memberships;
        this.invoices = invoices;
        this.billing = billing;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Override
    public List<Membership> runDailyChecks() {
        LocalDate today = LocalDate.now(clock);
        List<Membership> changed = new ArrayList<>();
        for (Membership before : memberships.findAll()) {
            Membership after = recordUnpaidInvoices(before, today).checkOn(today);
            if (after.equals(before)) {
                continue;
            }
            if (after.status() == MembershipStatus.CANCELLED) {
                billing.billFinal(after.membershipId());
            }
            memberships.save(after);
            if (after.status() != before.status()) {
                String status = after.status().name().toLowerCase(Locale.ROOT);
                notifications.send(new Notification(after.memberId(), "Membership " + status, "Your membership is now " + status + "."));
            }
            changed.add(after);
        }
        return changed;
    }

    /**
     * BR-M4: brings the count of declined payments in a row up to date with the invoices Billing has issued.
     * Counting from the invoices makes the daily checks safe to repeat.
     */
    private Membership recordUnpaidInvoices(Membership membership, LocalDate today) {
        int unpaid = 0;
        for (Invoice invoice : invoices.invoicesFor(membership.membershipId())) {
            if (invoice.status() == InvoiceStatus.PAID) {
                break;
            }
            unpaid++;
        }
        if (unpaid == 0 && membership.failedPayments() > 0) {
            return membership.recordPayment(true, today);
        }
        Membership updated = membership;
        for (int failed = membership.failedPayments(); failed < unpaid; failed++) {
            updated = updated.recordPayment(false, today);
        }
        return updated;
    }
}
