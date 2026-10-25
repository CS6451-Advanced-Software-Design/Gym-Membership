# Rough work and sketches (report §6)

Photos of the analysis as we actually did it: paper sketches, whiteboard sessions and early versions of a diagram, mistakes and crossings-out included. The spec allows analysis diagrams to be drawn on paper and photographed for the report, and the photos show the analysis is our own work and how it changed. The clean diagrams stay in [../analysis](../analysis/README.md).

## Adding a photo

1. Take the photo straight on, in good light, so the text is readable at page width. Crop it, but don't redraw or tidy it.
2. Name it `YYYY-MM-DD-<diagram>-<member>.jpg`, e.g. `2026-10-22-class-diagram-dhruv.jpg`. If it's a group session, use `team` for the member.
3. Add a row below in the same PR, with the date, who drew it, what it shows, and what changed afterwards.

Keep each photo under about 2 MB, so the report PDF stays small.

## Photos

| Date | Photo | Drawn by | What it shows | What changed afterwards |
| --- | --- | --- | --- | --- |
| 2026-10-25 | [Hexagon, first draft](2026-10-25-hexagon-chirag.jpg) | Chirag | Notes on the three parts of Hexagonal (domain, ports, adapters) and a hand drawing of the hexagon: domain inside the service layer, `port.in` and `port.out` on opposite edges, the controller reaching `port.in` through the interceptor, logging out of band, and the payment and notification adapters implementing `port.out` | Nothing redrawn yet. It explains the layers behind the clean package diagram in [../architecture](../architecture/README.md) for §5. The layout was copied from a GenAI reference page (prompt #29) |
| 2026-10-25 | [Discount Strategy](2026-10-25-discount-strategy-chirag.jpg) | Chirag | Left page: the first idea for Billing discounts, an `if / else if` chain in `Invoice.total` (15% corporate, 10% family, 5% after 24 months), crossed out. Right page: the Strategy design, with `«interface» DiscountPolicy` realised by `CorporateDiscount`, `FamilyDiscount` and `LoyaltyDiscount`, notes on why it is better (easier to add a discount, no RTTI smell, one place for the comparison logic), and the Strategy roles (interface, concrete strategies, context = `BillingService`) | The sketch has no `BestDiscount`, so the `policies 1..*` aggregation ended up on `LoyaltyDiscount`. In the code (PR #14) it belongs to `BestDiscount`, a fourth `DiscountPolicy` that asks every rule and keeps the biggest discount (BR-P2). The layout was copied from a GenAI reference page (prompt #31) |
