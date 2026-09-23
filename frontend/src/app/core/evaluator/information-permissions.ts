/**
 * Enumerates the permission levels that apply to the Information service
 * within an interest group.
 *
 * The levels form an ascending hierarchy of capability, where each higher
 * value grants strictly more access than the ones below it:
 * - `InfNoAccess` (0): the user cannot view the information service at all.
 * - `InfAccess` (1): the user may read/consume the information content.
 * - `InfManage` (2): the user may manage the information content.
 * - `InfAdmin` (3): the user has full administrative control over the
 *   information service.
 *
 * Declared as a `const` object (rather than a TypeScript `enum`) so the
 * numeric values can be used at runtime while the companion
 * {@link InformationPermissions} type provides compile-time safety.
 */
export const InformationPermissions = {
  InfNoAccess: 0,
  InfAccess: 1,
  InfManage: 2,
  InfAdmin: 3,
} as const;

/**
 * Union type of the valid numeric permission values (0–3) derived from the
 * {@link InformationPermissions} const object.
 *
 * Use this type to constrain variables and parameters that hold an
 * information permission level, ensuring only recognised values are assigned.
 */
export type InformationPermissions =
  (typeof InformationPermissions)[keyof typeof InformationPermissions];

/**
 * Array of string keys from the InformationPermissions object
 * Used for permission lookups and UI components
 */
export const informationPermissionKeys: string[] = Object.keys(
  InformationPermissions
);
