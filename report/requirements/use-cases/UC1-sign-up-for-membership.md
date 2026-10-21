# UC1: Sign Up for Membership

> Bootstrap draft (GenAI prompt #2), in the Week 2 template format (courtesy of DCU). Rewrite it in our own words before it goes into the report.

| Field | Value |
| --- | --- |
| **Goal in Context** | A prospective member joins the club on a chosen plan and pays the first charge |
| **Scope & Level** | Membership system, user goal |
| **Preconditions** | The prospective member is not already an active member. Plans and prices are configured |
| **Success End Condition** | A membership exists in Trial (or Active, for an upfront Annual plan), the first payment is recorded, and a welcome notification is sent |
| **Failed End Condition** | No membership is created and no money is taken |
| **Primary, Secondary Actors** | Prospective Member; Payment Gateway |
| **Trigger** | The prospective member chooses "Join" |

**Description**

| Step | Action |
| --- | --- |
| 1 | The prospective member asks to join |
| 2 | The system lists the plans with prices and access rules (BR-M1) |
| 3 | The prospective member chooses a plan and enters personal and payment details |
| 4 | The system calculates the first charge: pro-rated price, minus the best eligible discount (`<<include>>` Calculate Charges; BR-P2, BR-P4) |
| 5 | The prospective member confirms |
| 6 | The system charges the amount through the Payment Gateway |
| 7 | The system creates the membership in Trial state (BR-M2), issues an invoice and sends a welcome notification |

**Extensions**

| Step | Branching action |
| --- | --- |
| 3a | A Student plan is chosen but the student ID is invalid: 3a1. The system rejects the plan and returns to step 2 |
| 3b | A referral code is entered: 3b1. `<<extend>>` Apply Referral Credit to the referrer (BR-P3) |
| 6a | The payment is declined: 6a1. The system reports the failure and allows one retry, then the use case ends in failure |

**Variations**

| Step | Branching action |
| --- | --- |
| 1 | The prospective member signs up online (via the API) or at the front desk with staff |
| 3 | Payment by card or direct debit |

| Related information | |
| --- | --- |
| Priority | Top |
| Performance | Confirmation in under 2 seconds |
| Frequency | About 20/day |
| Open issues | Family memberships: one payer for several members? |
| Superordinates | Manage Memberships |
| Subordinates | Calculate Charges, Apply Referral Credit |
