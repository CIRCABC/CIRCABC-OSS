/**
 * Enumerates the hierarchical permission levels a user can hold on the
 * newsgroups (forum/discussion) service of an interest group.
 *
 * The numeric values are ordered by increasing privilege, so they can be
 * compared directly to determine whether a user meets a required access
 * level:
 * - `NwsNoAccess` (0): no access to the newsgroup.
 * - `NwsAccess` (1): read-only access to newsgroup content.
 * - `NwsPost` (2): may post messages in addition to reading.
 * - `NwsModerate` (3): may moderate posts (edit/approve/delete) in addition
 *   to posting.
 * - `NwsAdmin` (4): full administrative control over the newsgroup.
 *
 * Declared `as const` so each entry keeps its literal numeric type, enabling
 * the companion {@link NewsgroupsPermissions} union type.
 */
export const NewsgroupsPermissions = {
  NwsNoAccess: 0,
  NwsAccess: 1,
  NwsPost: 2,
  NwsModerate: 3,
  NwsAdmin: 4,
} as const;

/**
 * Union type of the valid numeric permission values defined by the
 * {@link NewsgroupsPermissions} constant object (i.e. `0 | 1 | 2 | 3 | 4`).
 *
 * Use this type wherever a newsgroup permission level is expected to ensure
 * only recognised values are accepted.
 */
export type NewsgroupsPermissions =
  (typeof NewsgroupsPermissions)[keyof typeof NewsgroupsPermissions];

/**
 * Array of string keys from the NewsgroupsPermissions object
 * Used for permission lookups and UI components
 */
export const newsGroupPermissionKeys: string[] = Object.keys(
  NewsgroupsPermissions
);
