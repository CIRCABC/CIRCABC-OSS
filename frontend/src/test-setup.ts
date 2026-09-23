/**
 * Global test setup — suppress noisy Transloco "Missing translation" warnings.
 * These are expected in tests because we don't load real translation files.
 */
/**
 * Reference to the native `console.warn` implementation, captured before it is
 * overridden below. Used to forward all warnings that are not the expected
 * Transloco "Missing translation" noise, so genuine warnings still surface in
 * the test output.
 */
// eslint-disable-next-line no-console
const originalWarn = console.warn;
/**
 * Overrides the global `console.warn` for the test environment.
 *
 * Transloco emits a "Missing translation for ..." warning whenever a key cannot
 * be resolved. Since tests do not load the real translation files, these
 * warnings are expected and only add noise. This wrapper silently drops those
 * specific messages while delegating every other warning to the original
 * {@link originalWarn} implementation.
 *
 * @param args - The arguments originally passed to `console.warn`. The first
 * argument is inspected (when it is a string) to detect the "Missing
 * translation for" prefix.
 * @returns Nothing. Suppressed missing-translation warnings return early;
 * all other warnings are forwarded to the native `console.warn`.
 */
// eslint-disable-next-line no-console
console.warn = (...args: unknown[]) => {
  const msg = typeof args[0] === 'string' ? args[0] : '';
  if (msg.includes('Missing translation for')) {
    return;
  }
  originalWarn.apply(console, args);
};
