# Transparency {#transparency}

<!--
Owner: Bharat.
Every number comes from a tool, named next to the table: cloc for lines of code, git blame or a script for authorship.
Tables: classes per package with totals, every class with its author and LOC, LOC per member, and who did what for
patterns, testing and CI/CD. Then the diary, which comes from report/diary.md.
-->

## Classes per Package

| Package | Types | Lines of code |
| --- | --- | --- |
| `gym` | 4 | 219 |
| `gym.adapter.logging` | 3 | 51 |
| `gym.adapter.notification` | 3 | 31 |
| `gym.adapter.payment` | 3 | 46 |
| `gym.adapter.persistence` | 10 | 157 |
| `gym.controller` | 8 | 382 |
| `gym.domain` | 40 | 918 |
| `gym.dto` | 17 | 192 |
| `gym.interceptor` | 5 | 147 |
| `gym.port.in` | 20 | 132 |
| `gym.port.out` | 11 | 52 |
| `gym.service` | 14 | 1022 |
| **Main total** | **138** | **3349** |
| Tests (`code/src/test`) | 44 | 3831 |
| **All** | **182** | **7180** |

Table: Types (classes, interfaces, records and enums) and lines of code per package. Counted by cloc 2.02 on commit 41bed5f; lines of code leave out blank and comment lines

## Classes, Authors and Lines of Code

```{=typst}
#[
#show table: set text(size: 8pt)
#set table(inset: 3pt)
```

| Package | Classes (main author, lines of code) |
| --- | --- |
| gym | BillingConfiguration (Chirag, 16), GymApplication (Chirag, 19), InterceptorConfiguration (Chirag, 20), OpenApiConfiguration (Chirag, 164) |
| adapter.logging | LoggingInterceptor (Chirag, 37), Outcome (Chirag, 13) |
| adapter.notification | EventNotificationSender (Dhruv, 16), NotificationLog (Dhruv, 14) |
| adapter.payment | FakePaymentGateway (Dhruv, 36), PaymentProperties (Dhruv, 9) |
| adapter.persistence | JsonFileBookingRepository (Bharat, 11), JsonFileChargeRepository (Chirag, 11), JsonFileCheckInRepository (DineshReddy, 11), JsonFileGymClassRepository (Bharat, 11), JsonFileInvoiceRepository (Chirag, 11), JsonFileMemberRepository (Chirag, 11), JsonFileMembershipRepository (Chirag, 11), JsonFileRepository (Chirag, 73), PersistenceProperties (Chirag, 6) |
| controller | BillingController (Chirag, 46), BillingJob (Chirag, 19), BookingController (Bharat 97%, 103), ErrorHandler (Chirag, 19), MemberController (DineshReddy 84%, 54), MembershipChecksJob (Dhruv, 15), MembershipController (Dhruv 97%, 125) |
| domain | ActiveState (Dhruv, 55), AnnualPlan (Dhruv, 19), BestDiscount (Chirag, 16), BillingAccount (Chirag, 11), Booking (Bharat, 40), BookingResult (Bharat, 12), BookingStatus (Bharat, 7), CancelledState (Dhruv, 7), Charge (Chirag, 25), ChargeType (Chirag, 12), CheckIn (DineshReddy, 11), CorporateDiscount (Chirag, 7), DiscountPolicy (Chirag, 4), FamilyDiscount (Chirag, 7), Freeze (Dhruv, 11), FrozenState (Dhruv, 21), GymClass (Bharat, 47), Invoice (Chirag, 39), InvoiceStatus (Chirag, 5), LapsedState (Dhruv, 34), LoyaltyDiscount (Chirag, 7), Member (Chirag 88%, 15), Membership (Dhruv 79%, 106), MembershipState (Dhruv 78%, 49), MembershipStateFactory (DineshReddy, 16), MembershipStatus (Chirag, 8), Money (Dhruv, 68), MonthlyPlan (Dhruv, 10), NoShows (Bharat, 21), NotFoundException (Chirag, 6), Notification (Dhruv, 9), OffPeakPlan (Dhruv, 22), Plan (Dhruv, 30), PlanFactory (Dhruv, 24), PlanType (Chirag, 14), RuleViolationException (Chirag, 6), StudentPlan (Dhruv, 18), TrialState (Dhruv 88%, 55), Waitlist (Bharat, 43) |
| dto | AddClassRequest (Bharat, 11), BillingRunRequest (Chirag, 6), BookClassRequest (Bharat 90%, 5), BookClassResponse (Bharat, 9), BookingResponse (Bharat, 9), CancellationResponse (Dhruv, 8), ChargeResponse (Chirag, 10), CheckInResponse (DineshReddy, 8), ClassResponse (Bharat, 10), FreezeRequest (Dhruv, 5), InvoiceResponse (Chirag, 25), MemberResponse (DineshReddy, 15), MembershipResponse (Dhruv 86%, 36), PlanResponse (Dhruv, 11), SignUpRequest (Dhruv 96%, 15), SignUpResponse (Dhruv, 8) |
| interceptor | UseCaseContext (Chirag, 52), UseCaseDispatcher (Chirag, 34), UseCaseInterception (Chirag, 51), UseCaseInterceptor (Chirag, 9) |
| port.in | BookClassUseCase (Bharat, 5), BookingCommandsUseCase (Bharat, 8), CalculateChargesUseCase (Chirag, 7), CancelBookingUseCase (Bharat, 5), CancelMembershipUseCase (Dhruv, 8), CheckInUseCase (DineshReddy, 7), FreezeMembershipUseCase (Dhruv, 6), ManageScheduleUseCase (Bharat, 10), PayArrearsUseCase (Chirag, 6), RecordAttendanceUseCase (Bharat, 6), RecordChargeUseCase (Chirag, 8), RestoreMembershipUseCase (Dhruv, 5), RunDailyChecksUseCase (Dhruv, 6), RunMonthlyBillingUseCase (Chirag, 7), SignUpUseCase (Dhruv, 11), UndoBookingUseCase (Bharat, 7), ViewInvoicesUseCase (Chirag, 6), ViewMemberUseCase (DineshReddy, 5), ViewMembershipUseCase (Dhruv, 8) |
| port.out | BookingRepository (Bharat, 4), ChargeRepository (Chirag, 4), CheckInRepository (DineshReddy, 4), GymClassRepository (Bharat, 4), InvoiceRepository (Chirag, 4), MemberRepository (DineshReddy 67%, 8), MembershipRepository (Chirag, 4), NotificationSender (Dhruv, 5), PaymentGateway (Dhruv, 5), Repository (Chirag, 9) |
| service | AttendanceService (Bharat, 50), BillingAccountFactory (DineshReddy, 54), BillingService (Chirag 96%, 227), BookClassCommand (Bharat, 30), BookingCommand (Bharat, 5), BookingCommandService (Bharat, 51), BookingQueries (Bharat, 63), BookingService (Bharat, 174), CancelBookingCommand (Bharat, 22), MemberService (DineshReddy, 73), MembershipDailyChecksService (DineshReddy, 75), MembershipService (Dhruv 97%, 147), ScheduleService (Bharat, 50) |
| test: gym | ArchitectureTest (Chirag, 52), BillingE2E (Chirag, 122), BookingE2E (Bharat, 232), GymApplicationIT (Chirag, 9), MemberE2E (DineshReddy, 96), MembershipE2E (Dhruv, 116), OpenApiSpecIT (Chirag 96%, 60) |
| test: adapter.logging | LoggingInterceptorTest (Chirag, 64) |
| test: adapter.notification | NotificationIT (Dhruv 96%, 21) |
| test: adapter.payment | FakePaymentGatewayTest (Dhruv, 33) |
| test: adapter.persistence | BillingPersistenceIT (Chirag, 44), BookingPersistenceIT (Bharat, 42), JsonFileRepositoryIT (Chirag 84%, 62), MemberPersistenceIT (DineshReddy, 21), MembershipPersistenceIT (Dhruv, 38) |
| test: controller | BillingControllerTest (Chirag, 62), BillingJobIT (Chirag, 23), BillingJobTest (Chirag, 24), BookingControllerTest (Bharat, 188), ErrorHandlerTest (Chirag, 39), MembershipChecksJobIT (Dhruv, 23), MembershipChecksJobTest (Dhruv, 17) |
| test: domain | BookingTest (Bharat, 52), DiscountPolicyTest (Chirag, 35), GymClassTest (Bharat, 71), InvoiceTest (Chirag, 43), MembershipStateTest (Dhruv 92%, 152), MoneyTest (Dhruv, 59), NoShowsTest (Bharat, 46), PlanFactoryTest (Dhruv, 62), WaitlistTest (Bharat, 58) |
| test: interceptor | InterceptorWiringIT (Chirag, 25), UseCaseDispatcherTest (Chirag, 49), UseCaseInterceptionTest (Chirag, 91) |
| test: port.in | PingUseCase (Chirag, 14) |
| test: port.out | MemberRepositoryTest (DineshReddy, 24) |
| test: service | BillingServiceTest (Chirag 99%, 239), BookingCommandServiceTest (Bharat, 226), BookingServiceTest (Bharat, 797), MemberServiceTest (DineshReddy, 82), MembershipServiceTest (Dhruv 92%, 187), ScheduleServiceTest (Bharat, 70) |
| test: support | InMemoryRepository (Dhruv, 31), TestClock (Dhruv, 30) |

Table: Every class, by package, with the member who wrote most of its lines (with their share when it is under 100%) and its lines of code. Authorship by `git blame -w -M` on commit 41bed5f (non-blank lines), lines of code by cloc 2.02

```{=typst}
]
```

## Lines of Code per Member

| Member | Main code lines | Test code lines | Total | Share |
| --- | --- | --- | --- | --- |
| Chirag | 1611 | 1111 | 2722 | 30.6% |
| Bharat | 1339 | 1857 | 3196 | 36.0% |
| DineshReddy | 466 | 251 | 717 | 8.1% |
| Dhruv | 1449 | 801 | 2250 | 25.3% |
| **All** | **4865** | **4020** | **8885** | **100%** |

Table: Lines per member in the code and tests on commit 41bed5f, by `git blame -w -M` (non-blank lines, so comments and Javadoc count, which is why the total is higher than cloc's)

## Responsibilities

| Area | Who |
| --- | --- |
| Design patterns | Chirag worked on Strategy (discount) Interceptor, Hexagonal and MVC Skeleton and the ArchitectureTest. Bharat worked on Command Pattern (for undo functionality) and used Observer Pattern for waitlist notification. Dhruv worked on State (membership lifecycle part) and Factory (PlanFactory Part) and Observer (port, publisher and listener). DineshReddy used Observer for check-in notifications |
| Testing | Each member tested their own features. Chirag split the tests into 3 kinds (unit, integration, end-to-end), each as its own CI check. Bharat did the most test code, 1857 lines that includes - BookingServiceTest, BookingCommandServiceTest, BookingPersistenceIT and BookingE2E |
| CI/CD | Chirag created CI Pipeline, the metrics comment on PRs and the Railway deployment and Report PDF build. DineshReddy did SonarQube scan, the Docker build on every PR, the live-app deploy check and CI fixes |

## Diary

::: {include="../diary.md" widths="0.11 0.43 0.23 0.23" size="8pt"}
:::
