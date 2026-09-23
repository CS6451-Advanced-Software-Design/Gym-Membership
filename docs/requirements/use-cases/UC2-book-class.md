# UC2: Book Class

> Bootstrap draft (GenAI prompt #2), in the Week 2 template format (courtesy of DCU). Rewrite it in our own words before it goes into the report.

| Field | Value |
| --- | --- |
| **Goal in Context** | A member reserves a place in a scheduled class, or joins the waitlist if it is full |
| **Scope & Level** | Booking subsystem, user goal |
| **Preconditions** | The member is identified. The class exists and hasn't started |
| **Success End Condition** | The member holds a confirmed booking or a waitlist position, and any surcharge is recorded against the next bill |
| **Failed End Condition** | No booking or waitlist entry is created, and no charges are recorded |
| **Primary, Secondary Actors** | Member |
| **Trigger** | The member selects a class and chooses "Book" |

**Description**

| Step | Action |
| --- | --- |
| 1 | The member selects a class from the timetable |
| 2 | The system checks that the membership allows booking (`<<include>>` Verify Membership Status: not Lapsed or Frozen, no no-show suspension; BR-M4, BR-B4) |
| 3 | The system checks the member's booking limits and plan restrictions (BR-B5; Trial limit BR-M2) |
| 4 | The system checks capacity and finds a free place (BR-B1) |
| 5 | The system works out any premium surcharge (BR-B6) and shows it |
| 6 | The member confirms |
| 7 | The system creates the booking, records the surcharge for the next bill, and sends a confirmation |

**Extensions**

| Step | Branching action |
| --- | --- |
| 2a | The membership is Lapsed, Frozen or suspended: 2a1. The system refuses and says why. The use case ends |
| 3a | A limit is exceeded, or an Off-Peak member picks a peak class: 3a1. The system refuses and says why |
| 4a | The class is full: 4a1. `<<extend>>` Join Waitlist. The system adds the member to the waitlist and shows their position |

**Variations**

| Step | Branching action |
| --- | --- |
| 1 | The booking is made by the member (via the API) or by front desk staff on the member's behalf |

| Related information | |
| --- | --- |
| Priority | Top |
| Performance | Response in under 1 second. Must stay correct when two members book the last place at once |
| Frequency | About 500/day, peaking when the timetable is released |
| Open issues | Can a member book back-to-back classes that overlap? |
| Superordinates | Manage Bookings |
| Subordinates | Verify Membership Status, Join Waitlist |
