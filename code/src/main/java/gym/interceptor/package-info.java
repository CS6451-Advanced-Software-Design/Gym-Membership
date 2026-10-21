/**
 * The Interceptor architectural pattern (POSA2) around the {@code gym.port.in} use cases:
 * the interceptor interface, its context object, the dispatcher and the proxy that calls them back.
 * Out-of-band services such as logging plug in here without touching a controller or service.
 */
package gym.interceptor;
