# Requirements {#requirements}

<!--
Owner: Dhruv.
The use case diagram, UC1 and UC2 in the Week 2 template (with extensions, variations and open issues), two quality
attributes as six-part scenarios (one must be extensibility) with named Bass et al. (2019) tactics and the class that
applies each one, and two GUI prototypes.
The use case drafts in report/requirements/use-cases came from GenAI (prompt #2): rewrite them in your own words.
-->

## Use Cases

![Use case diagram](../requirements/use-case-diagram.png)

*Introduction to the actors and use cases, to be written by Dhruv.*

## UC1: Sign Up for Membership

*To be written by Dhruv.*

## UC2: Book Class

| USE CASE 2 | Book Class |
| --- | --- |
| Goal in Context | A member reserves a place in a class on the timetable, or a place on its waitlist when the class is full |
| Scope & Level | Gym membership system (Booking), user goal |
| Preconditions | The member holds a membership. The class is on the timetable and hasn't started |
| Success End Condition | The member has a confirmed booking or a waitlist position. Any premium surcharge is recorded for the next bill, and a Trial booking is counted against the limit of 2 |
| Failed End Condition | No booking or waitlist place is created, nothing is charged, and the member is told why |
| Primary, Secondary Actors | Member; Front Desk Staff when booking for a member |
| Trigger | The member picks a class and chooses Book |

| DESCRIPTION | Step | Action |
| --- | --- | --- |
| | 1 | The member opens the timetable and picks a class |
| | 2 | The system checks that the member isn't suspended for no-shows (3 in 30 days, BR-B4) |
| | 3 | The system checks that the member holds fewer than 5 future bookings (BR-B5) |
| | 4 | The system asks the membership whether it may book this class, «include» Verify Membership Status. The membership asks its state (Trial while fewer than 2 bookings are used, Active yes, Frozen, Lapsed and Cancelled no) and its plan (Off-Peak only books off-peak classes, BR-B5) |
| | 5 | The system checks that the class has a free place (BR-B1) |
| | 6 | The system shows any premium surcharge (€8, BR-B6) and the member confirms |
| | 7 | The system creates the booking, records the surcharge for the next bill, counts the booking if the membership is in Trial, and confirms |

| EXTENSIONS | Step | Branching Action |
| --- | --- | --- |
| | 2a | The member is suspended: 2a1. The system refuses and says when the suspension ends |
| | 3a | The member already holds 5 future bookings: 3a1. The system refuses |
| | 4a | The state or the plan says no, for example a Frozen membership or an Off-Peak member choosing an evening class: 4a1. The system refuses and gives the reason |
| | 5a | The class is full: 5a1. «extend» Join Waitlist: the member goes to the end of the waitlist and is told their position. 5a2. When someone cancels, the first person waiting is booked automatically and notified (BR-B2, UC3) |

| VARIATIONS | Step | Branching Action |
| --- | --- | --- |
| | 1 | The member books online, or Front Desk Staff book for them |
| | 6 | Annual members pay no premium surcharge, and an ordinary class has none |

| RELATED INFORMATION | Book Class |
| --- | --- |
| Priority | Top: booking is what members do most |
| Performance | Answers in under 1 second. When two members try to book the last place at the same time, only one of them gets it |
| Frequency | About 500 a day, with a peak when the next week's timetable opens |
| Channel to actors | REST API (`POST /bookings`); the Book a Class screen in the second GUI prototype |
| OPEN ISSUES | Can a member book two classes that overlap in time? |
| | Should a member on the waitlist of a class that starts within 2 hours still be promoted, when they may not see the notification in time? |
| | Booking is still being implemented, so this description and the sequence diagram in §6 are the design it follows |
| Due Date | Release 1.0 |
| Superordinates | Manage bookings |
| Subordinates | Verify Membership Status, Join Waitlist |

## Quality Attributes

*QA1 (extensibility) and QA2, each as a six-part scenario with its tactics, to be written by Dhruv.*

## GUI Prototypes

*Two prototypes, e.g. Book a Class and Front-desk Check-in.*
