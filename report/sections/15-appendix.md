# Appendix A: Classes, Authors and Lines of Code {#appendix-classes .unnumbered}

The full list behind §7.2: every class (main code and tests) with the member who wrote most of its lines and its lines of code.

```{=typst}
#[
#show table: set text(size: 8pt)
#set table(inset: 3pt)
```

| Package | Class | Main author (share of lines) | Lines of code |
| --- | --- | --- | --- |
| gym | BillingConfiguration | Chirag (100%) | 16 |
| gym | GymApplication | Chirag (100%) | 19 |
| gym | InterceptorConfiguration | Chirag (100%) | 20 |
| gym | OpenApiConfiguration | Chirag (100%) | 164 |
| adapter.logging | LoggingInterceptor | Chirag (100%) | 37 |
| adapter.logging | Outcome | Chirag (100%) | 13 |
| adapter.notification | EventNotificationSender | Dhruv (100%) | 16 |
| adapter.notification | NotificationLog | Dhruv (100%) | 14 |
| adapter.payment | FakePaymentGateway | Dhruv (100%) | 36 |
| adapter.payment | PaymentProperties | Dhruv (100%) | 9 |
| adapter.persistence | JsonFileBookingRepository | Bharat (100%) | 11 |
| adapter.persistence | JsonFileChargeRepository | Chirag (100%) | 11 |
| adapter.persistence | JsonFileCheckInRepository | DineshReddy (100%) | 11 |
| adapter.persistence | JsonFileGymClassRepository | Bharat (100%) | 11 |
| adapter.persistence | JsonFileInvoiceRepository | Chirag (100%) | 11 |
| adapter.persistence | JsonFileMemberRepository | Chirag (100%) | 11 |
| adapter.persistence | JsonFileMembershipRepository | Chirag (100%) | 11 |
| adapter.persistence | JsonFileRepository | Chirag (100%) | 73 |
| adapter.persistence | PersistenceProperties | Chirag (100%) | 6 |
| controller | BillingController | Chirag (100%) | 46 |
| controller | BillingJob | Chirag (100%) | 19 |
| controller | BookingController | Bharat (97%) | 103 |
| controller | ErrorHandler | Chirag (100%) | 19 |
| controller | MemberController | DineshReddy (84%) | 54 |
| controller | MembershipChecksJob | Dhruv (100%) | 15 |
| controller | MembershipController | Dhruv (97%) | 125 |
| domain | ActiveState | Dhruv (100%) | 55 |
| domain | AnnualPlan | Dhruv (100%) | 19 |
| domain | BestDiscount | Chirag (100%) | 16 |
| domain | BillingAccount | Chirag (100%) | 11 |
| domain | Booking | Bharat (100%) | 40 |
| domain | BookingResult | Bharat (100%) | 12 |
| domain | BookingStatus | Bharat (100%) | 7 |
| domain | CancelledState | Dhruv (100%) | 7 |
| domain | Charge | Chirag (100%) | 25 |
| domain | ChargeType | Chirag (100%) | 12 |
| domain | CheckIn | DineshReddy (100%) | 11 |
| domain | CorporateDiscount | Chirag (100%) | 7 |
| domain | DiscountPolicy | Chirag (100%) | 4 |
| domain | FamilyDiscount | Chirag (100%) | 7 |
| domain | Freeze | Dhruv (100%) | 11 |
| domain | FrozenState | Dhruv (100%) | 21 |
| domain | GymClass | Bharat (100%) | 47 |
| domain | Invoice | Chirag (100%) | 39 |
| domain | InvoiceStatus | Chirag (100%) | 5 |
| domain | LapsedState | Dhruv (100%) | 34 |
| domain | LoyaltyDiscount | Chirag (100%) | 7 |
| domain | Member | Chirag (88%) | 15 |
| domain | Membership | Dhruv (79%) | 106 |
| domain | MembershipState | Dhruv (78%) | 49 |
| domain | MembershipStateFactory | DineshReddy (100%) | 16 |
| domain | MembershipStatus | Chirag (100%) | 8 |
| domain | Money | Dhruv (100%) | 68 |
| domain | MonthlyPlan | Dhruv (100%) | 10 |
| domain | NoShows | Bharat (100%) | 21 |
| domain | NotFoundException | Chirag (100%) | 6 |
| domain | Notification | Dhruv (100%) | 9 |
| domain | OffPeakPlan | Dhruv (100%) | 22 |
| domain | Plan | Dhruv (100%) | 30 |
| domain | PlanFactory | Dhruv (100%) | 24 |
| domain | PlanType | Chirag (100%) | 14 |
| domain | RuleViolationException | Chirag (100%) | 6 |
| domain | StudentPlan | Dhruv (100%) | 18 |
| domain | TrialState | Dhruv (88%) | 55 |
| domain | Waitlist | Bharat (100%) | 43 |
| dto | AddClassRequest | Bharat (100%) | 11 |
| dto | BillingRunRequest | Chirag (100%) | 6 |
| dto | BookClassRequest | Bharat (90%) | 5 |
| dto | BookClassResponse | Bharat (100%) | 9 |
| dto | BookingResponse | Bharat (100%) | 9 |
| dto | CancellationResponse | Dhruv (100%) | 8 |
| dto | ChargeResponse | Chirag (100%) | 10 |
| dto | CheckInResponse | DineshReddy (100%) | 8 |
| dto | ClassResponse | Bharat (100%) | 10 |
| dto | FreezeRequest | Dhruv (100%) | 5 |
| dto | InvoiceResponse | Chirag (100%) | 25 |
| dto | MemberResponse | DineshReddy (100%) | 15 |
| dto | MembershipResponse | Dhruv (86%) | 36 |
| dto | PlanResponse | Dhruv (100%) | 11 |
| dto | SignUpRequest | Dhruv (96%) | 15 |
| dto | SignUpResponse | Dhruv (100%) | 8 |
| interceptor | UseCaseContext | Chirag (100%) | 52 |
| interceptor | UseCaseDispatcher | Chirag (100%) | 34 |
| interceptor | UseCaseInterception | Chirag (100%) | 51 |
| interceptor | UseCaseInterceptor | Chirag (100%) | 9 |
| port.in | BookClassUseCase | Bharat (100%) | 5 |
| port.in | BookingCommandsUseCase | Bharat (100%) | 8 |
| port.in | CalculateChargesUseCase | Chirag (100%) | 7 |
| port.in | CancelBookingUseCase | Bharat (100%) | 5 |
| port.in | CancelMembershipUseCase | Dhruv (100%) | 8 |
| port.in | CheckInUseCase | DineshReddy (100%) | 7 |
| port.in | FreezeMembershipUseCase | Dhruv (100%) | 6 |
| port.in | ManageScheduleUseCase | Bharat (100%) | 10 |
| port.in | PayArrearsUseCase | Chirag (100%) | 6 |
| port.in | RecordAttendanceUseCase | Bharat (100%) | 6 |
| port.in | RecordChargeUseCase | Chirag (100%) | 8 |
| port.in | RestoreMembershipUseCase | Dhruv (100%) | 5 |
| port.in | RunDailyChecksUseCase | Dhruv (100%) | 6 |
| port.in | RunMonthlyBillingUseCase | Chirag (100%) | 7 |
| port.in | SignUpUseCase | Dhruv (100%) | 11 |
| port.in | UndoBookingUseCase | Bharat (100%) | 7 |
| port.in | ViewInvoicesUseCase | Chirag (100%) | 6 |
| port.in | ViewMemberUseCase | DineshReddy (100%) | 5 |
| port.in | ViewMembershipUseCase | Dhruv (100%) | 8 |
| port.out | BookingRepository | Bharat (100%) | 4 |
| port.out | ChargeRepository | Chirag (100%) | 4 |
| port.out | CheckInRepository | DineshReddy (100%) | 4 |
| port.out | GymClassRepository | Bharat (100%) | 4 |
| port.out | InvoiceRepository | Chirag (100%) | 4 |
| port.out | MemberRepository | DineshReddy (67%) | 8 |
| port.out | MembershipRepository | Chirag (100%) | 4 |
| port.out | NotificationSender | Dhruv (100%) | 5 |
| port.out | PaymentGateway | Dhruv (100%) | 5 |
| port.out | Repository | Chirag (100%) | 9 |
| service | AttendanceService | Bharat (100%) | 50 |
| service | BillingAccountFactory | DineshReddy (100%) | 54 |
| service | BillingService | Chirag (96%) | 227 |
| service | BookClassCommand | Bharat (100%) | 30 |
| service | BookingCommand | Bharat (100%) | 5 |
| service | BookingCommandService | Bharat (100%) | 51 |
| service | BookingQueries | Bharat (100%) | 63 |
| service | BookingService | Bharat (100%) | 174 |
| service | CancelBookingCommand | Bharat (100%) | 22 |
| service | MemberService | DineshReddy (100%) | 73 |
| service | MembershipDailyChecksService | DineshReddy (100%) | 75 |
| service | MembershipService | Dhruv (97%) | 147 |
| service | ScheduleService | Bharat (100%) | 50 |
| test: gym | ArchitectureTest | Chirag (100%) | 52 |
| test: gym | BillingE2E | Chirag (100%) | 122 |
| test: gym | BookingE2E | Bharat (100%) | 232 |
| test: gym | GymApplicationIT | Chirag (100%) | 9 |
| test: gym | MemberE2E | DineshReddy (100%) | 96 |
| test: gym | MembershipE2E | Dhruv (100%) | 116 |
| test: gym | OpenApiSpecIT | Chirag (96%) | 60 |
| test: adapter.logging | LoggingInterceptorTest | Chirag (100%) | 64 |
| test: adapter.notification | NotificationIT | Dhruv (96%) | 21 |
| test: adapter.payment | FakePaymentGatewayTest | Dhruv (100%) | 33 |
| test: adapter.persistence | BillingPersistenceIT | Chirag (100%) | 44 |
| test: adapter.persistence | BookingPersistenceIT | Bharat (100%) | 42 |
| test: adapter.persistence | JsonFileRepositoryIT | Chirag (84%) | 62 |
| test: adapter.persistence | MemberPersistenceIT | DineshReddy (100%) | 21 |
| test: adapter.persistence | MembershipPersistenceIT | Dhruv (100%) | 38 |
| test: controller | BillingControllerTest | Chirag (100%) | 62 |
| test: controller | BillingJobIT | Chirag (100%) | 23 |
| test: controller | BillingJobTest | Chirag (100%) | 24 |
| test: controller | BookingControllerTest | Bharat (100%) | 188 |
| test: controller | ErrorHandlerTest | Chirag (100%) | 39 |
| test: controller | MembershipChecksJobIT | Dhruv (100%) | 23 |
| test: controller | MembershipChecksJobTest | Dhruv (100%) | 17 |
| test: domain | BookingTest | Bharat (100%) | 52 |
| test: domain | DiscountPolicyTest | Chirag (100%) | 35 |
| test: domain | GymClassTest | Bharat (100%) | 71 |
| test: domain | InvoiceTest | Chirag (100%) | 43 |
| test: domain | MembershipStateTest | Dhruv (92%) | 152 |
| test: domain | MoneyTest | Dhruv (100%) | 59 |
| test: domain | NoShowsTest | Bharat (100%) | 46 |
| test: domain | PlanFactoryTest | Dhruv (100%) | 62 |
| test: domain | WaitlistTest | Bharat (100%) | 58 |
| test: interceptor | InterceptorWiringIT | Chirag (100%) | 25 |
| test: interceptor | UseCaseDispatcherTest | Chirag (100%) | 49 |
| test: interceptor | UseCaseInterceptionTest | Chirag (100%) | 91 |
| test: port.in | PingUseCase | Chirag (100%) | 14 |
| test: port.out | MemberRepositoryTest | DineshReddy (100%) | 24 |
| test: service | BillingServiceTest | Chirag (99%) | 239 |
| test: service | BookingCommandServiceTest | Bharat (100%) | 226 |
| test: service | BookingServiceTest | Bharat (100%) | 797 |
| test: service | MemberServiceTest | DineshReddy (100%) | 82 |
| test: service | MembershipServiceTest | Dhruv (92%) | 187 |
| test: service | ScheduleServiceTest | Bharat (100%) | 70 |
| test: support | InMemoryRepository | Dhruv (100%) | 31 |
| test: support | TestClock | Dhruv (100%) | 30 |

Table: Every class, its main author and lines of code (`git blame` and cloc, commit 41bed5f)

```{=typst}
]
```
