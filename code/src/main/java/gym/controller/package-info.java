/**
 * The driving adapters: MVC controllers that Postman calls, and {@link gym.controller.BillingJob} for the clock.
 * They translate a request or a tick into a call on a {@code gym.port.in} use case and never hold business rules.
 */
package gym.controller;
