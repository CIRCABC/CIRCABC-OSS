/**
 * Severity levels used to classify UI messages/notifications shown to the user.
 *
 * The level typically drives the visual treatment of a message (icon, color,
 * styling) and can be used to filter or prioritize notifications within the
 * application's messaging system.
 */
export enum UiMessageLevel {
  /** Informational message conveying neutral, non-critical feedback. */
  INFO = 0,
  /** Warning message highlighting a potential issue that needs attention. */
  WARNING = 1,
  /** Error message indicating that an operation failed or something went wrong. */
  ERROR = 2,
  /** Success message confirming that an operation completed successfully. */
  SUCCESS = 3,
}
