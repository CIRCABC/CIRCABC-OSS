/**
 * Enumerates the possible outcomes of a user-triggered action (for example,
 * the result returned when a modal dialog or workflow completes).
 *
 * Declared as a frozen (`as const`) object literal so its members can be used
 * both as runtime values and, together with the companion {@link ActionResult}
 * type, as a lightweight enum.
 *
 * @property SUCCEED  The action completed successfully (numeric value `1`).
 * @property CANCELED The action was cancelled by the user (numeric value `0`).
 * @property FAILED   The action failed to complete (numeric value `-1`).
 */
export const ActionResult = {
  SUCCEED: 1,
  CANCELED: 0,
  FAILED: -1,
} as const;

/**
 * Union of the numeric values held by the {@link ActionResult} constant
 * (`1 | 0 | -1`).
 *
 * Use this type to annotate variables, parameters and return values that carry
 * an action outcome, ensuring only the defined {@link ActionResult} members are
 * accepted.
 */
export type ActionResult = (typeof ActionResult)[keyof typeof ActionResult];
