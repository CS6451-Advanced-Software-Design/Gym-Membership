# Roles

**Status: proposal, to be agreed by the team.**

The spec (Table 2) lists 10 roles. Everyone must contribute **equally to code and to the report**, so each member takes one report-facing role and one technical role, **owns one feature across every layer** (its controller, port, service, domain and adapter classes, plus their tests), and writes their own reflection.

## Role split

| Member | Report-facing role | Technical role | Owns feature |
| --- | --- | --- | --- |
| A | Project Manager (§3 plan, diary, transparency tables §7) | Tester (test strategy, JUnit 5 setup, §8 tests) | Booking |
| B | Business Analyst / Requirements Engineer (§4) | Systems Analyst (§6 analysis sketches) | Membership |
| C | Architect (§5 architecture, tech pipeline) | Designer (§10 recovered blueprints in the UML workbench) | Billing |
| D | Documentation Manager (assembles the report, presentation checks) | Technical Lead + DevOps (repo, CI/CD, metrics → refactoring §9) | Member |

Everyone: Programmer (spec Table 2, row 8), reviews other members' PRs, and writes their own reflection (§12).

Chirag's platform and DevOps background suits role D. The other allocations are open, so match them to experience and interest.

## Package structure: monolithic, package by layer

Part 1 must be **monolithic**. The lecture slides show two ways to lay out an MVC project:

- **Package by layer** (`controller`, `service`, `domain`, `repository`) is the monolithic layout. **We use this.**
- **Package by feature** (`membership`, `booking`, ... each with its own controller, service and repository) is the microservice-style layout, which belongs to Part 2. **Don't use it here.**

The self-researched architectural pattern is **Hexagonal (Ports and Adapters)**, chosen on 7 Oct 2026. It adds `port` and `adapter` packages, but they are still layers rather than features, so the system stays monolithic. Full layout and rationale: [architecture/README.md](architecture/README.md).

```
gym/
+- GymApplication
+- controller/   MembershipController, BookingController, BillingController, MemberController
+- dto/          request and response records
+- port/in/      SignUpUseCase, BookClassUseCase, ...
+- port/out/     Repository<T, ID>, MembershipRepository, GymClassRepository, ..., PaymentGateway, NotificationSender
+- service/      MembershipService, BookingService, BillingService, MemberService
+- domain/       Member, Membership, Plan, GymClass, Booking, Waitlist, Charge, Invoice, ...
+- adapter/      persistence (JSON files), payment, notification
```

Dependencies point inwards towards `domain`: controller → port.in ← service → port.out ← adapter. The business rules live in `service` and `domain`, not in the controllers or adapters.

## Features (owned across all layers)

| Feature | Scope | Likely design pattern(s) |
| --- | --- | --- |
| Membership | Plans, sign-up, freeze and cancel, membership lifecycle | State (membership lifecycle), Factory (plan creation) |
| Billing | Monthly billing run, discounts, penalties, referral credits, invoices | Strategy (pricing and discount policies) |
| Booking | Class schedule, bookings, capacity, waitlist, no-shows | Command (book and cancel with undo), Observer (waitlist promotion) |
| Member | Member profiles, check-in, referrals, notifications | Observer (notifications) |

The owner of a feature writes its class in each layer, e.g. the Booking owner writes `BookingController`, `BookClassUseCase`, `BookingService`, `Booking`/`Waitlist`, `GymClassRepository` and `JsonFileGymClassRepository`. That keeps contributions equal and traceable in the §7 transparency tables (classes per package with author and LOC).

Shared (built together in Iteration 1): the MVC/REST controller base, the `Repository<T, ID>` port with a generic JSON file adapter, and the domain exceptions.
