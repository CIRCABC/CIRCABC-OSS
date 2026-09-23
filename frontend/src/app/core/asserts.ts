/**
 * Runtime assertion that a value is neither `null` nor `undefined`.
 *
 * This is a TypeScript assertion function: on a successful (non-throwing)
 * return the compiler narrows the type of `obj` to `NonNullable<T>`, so
 * callers can safely treat the value as defined afterwards.
 *
 * @typeParam T - The type of the value being checked.
 * @param obj - The value to assert as defined and non-null.
 * @returns Nothing; narrows `obj` to `NonNullable<T>` when it does not throw.
 * @throws {Error} If `obj` is `undefined` or `null`.
 */
export function assertDefined<T>(obj: T): asserts obj is NonNullable<T> {
  if (obj === undefined || obj === null) {
    throw new Error('Must not be a nullable or undefined value');
  }
}
