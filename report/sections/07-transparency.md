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

| Package | Classes by main author, with lines of code |
| --- | --- |
| gym | **Chirag:** BillingConfiguration 16, GymApplication 19, InterceptorConfiguration 20, OpenApiConfiguration 164 |
| adapter | **Chirag:** LoggingInterceptor 37, Outcome 13, JsonFileChargeRepository 11, JsonFileInvoiceRepository 11, JsonFileMemberRepository 11, JsonFileMembershipRepository 11, JsonFileRepository 73, PersistenceProperties 6; **Dhruv:** EventNotificationSender 16, NotificationLog 14, FakePaymentGateway 36, PaymentProperties 9; **Bharat:** JsonFileBookingRepository 11, JsonFileGymClassRepository 11; **DineshReddy:** JsonFileCheckInRepository 11 |
| controller | **Chirag:** BillingController 46, BillingJob 19, ErrorHandler 19; **Bharat:** BookingController 103; **DineshReddy:** MemberController 54; **Dhruv:** MembershipChecksJob 15, MembershipController 125 |
| domain | **Dhruv:** ActiveState 55, AnnualPlan 19, CancelledState 7, Freeze 11, FrozenState 21, LapsedState 34, Membership 106, MembershipState 49, Money 68, MonthlyPlan 10, Notification 9, OffPeakPlan 22, Plan 30, PlanFactory 24, StudentPlan 18, TrialState 55; **Chirag:** BestDiscount 16, BillingAccount 11, Charge 25, ChargeType 12, CorporateDiscount 7, DiscountPolicy 4, FamilyDiscount 7, Invoice 39, InvoiceStatus 5, LoyaltyDiscount 7, Member 15, MembershipStatus 8, NotFoundException 6, PlanType 14, RuleViolationException 6; **Bharat:** Booking 40, BookingResult 12, BookingStatus 7, GymClass 47, NoShows 21, Waitlist 43; **DineshReddy:** CheckIn 11, MembershipStateFactory 16 |
| dto | **Bharat:** AddClassRequest 11, BookClassRequest 5, BookClassResponse 9, BookingResponse 9, ClassResponse 10; **Chirag:** BillingRunRequest 6, ChargeResponse 10, InvoiceResponse 25; **Dhruv:** CancellationResponse 8, FreezeRequest 5, MembershipResponse 36, PlanResponse 11, SignUpRequest 15, SignUpResponse 8; **DineshReddy:** CheckInResponse 8, MemberResponse 15 |
| interceptor | **Chirag:** UseCaseContext 52, UseCaseDispatcher 34, UseCaseInterception 51, UseCaseInterceptor 9 |
| port | **Bharat:** BookClassUseCase 5, BookingCommandsUseCase 8, CancelBookingUseCase 5, ManageScheduleUseCase 10, RecordAttendanceUseCase 6, UndoBookingUseCase 7, BookingRepository 4, GymClassRepository 4; **Chirag:** CalculateChargesUseCase 7, PayArrearsUseCase 6, RecordChargeUseCase 8, RunMonthlyBillingUseCase 7, ViewInvoicesUseCase 6, ChargeRepository 4, InvoiceRepository 4, MembershipRepository 4, Repository 9; **Dhruv:** CancelMembershipUseCase 8, FreezeMembershipUseCase 6, RestoreMembershipUseCase 5, RunDailyChecksUseCase 6, SignUpUseCase 11, ViewMembershipUseCase 8, NotificationSender 5, PaymentGateway 5; **DineshReddy:** CheckInUseCase 7, ViewMemberUseCase 5, CheckInRepository 4, MemberRepository 8 |
| service | **Bharat:** AttendanceService 50, BookClassCommand 30, BookingCommand 5, BookingCommandService 51, BookingQueries 63, BookingService 174, CancelBookingCommand 22, ScheduleService 50; **DineshReddy:** BillingAccountFactory 54, MemberService 73, MembershipDailyChecksService 75; **Chirag:** BillingService 227; **Dhruv:** MembershipService 147 |

Table: Every class in the main code, grouped by package and by the member who wrote most of its lines, with its lines of code. Authorship by `git blame -w -M` on commit 41bed5f (non-blank lines), lines of code by cloc 2.02

The 44 test classes (3831 lines) follow the same packages and are counted in the two tables around this one.

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

::: {include="../diary.md" merge="true" widths="0.08 0.46 0.23 0.23" size="8pt"}
:::
