/**
 * Enumerates the hierarchical access levels a user can hold on a directory
 * (interest group / node) within CIRCABC.
 *
 * The values are ordered by increasing privilege, so they can be compared
 * numerically to determine whether a user meets a required permission level:
 * - `DirNoAccess` (0): no access to the directory.
 * - `DirAccess` (1): basic read/access rights.
 * - `DirManageMembers` (2): can manage the directory's members.
 * - `DirAdmin` (3): full administrative control over the directory.
 *
 * Declared `as const` so each entry is a literal type used across the
 * permission evaluators and related UI components.
 */
export const DirectoryPermissions = {
  DirNoAccess: 0,
  DirAccess: 1,
  DirManageMembers: 2,
  DirAdmin: 3,
} as const;

/**
 * Union type of the numeric access-level values defined by the
 * {@link DirectoryPermissions} object (i.e. `0 | 1 | 2 | 3`).
 *
 * Use this type wherever a directory permission level is expected to keep
 * values constrained to the known set of levels.
 */
export type DirectoryPermissions =
  (typeof DirectoryPermissions)[keyof typeof DirectoryPermissions];

/**
 * Array of string keys from the DirectoryPermissions object
 * Used for permission lookups and UI components
 */
export const directoryPermissionKeys: string[] =
  Object.keys(DirectoryPermissions);
