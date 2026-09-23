/**
 * Enumeration of the access levels a user can hold on the document library
 * service of an interest group.
 *
 * Modelled as a `const` object (rather than a TypeScript `enum`) so that the
 * numeric values can be used directly for permission comparisons while the
 * literal-union {@link LibraryPermissions} type keeps usage type-safe. The
 * levels are ordered from the least privileged (`LibNoAccess`) to the most
 * privileged (`LibAdmin`), which allows callers to compare values numerically
 * when checking whether a user meets a required threshold.
 *
 * @property LibNoAccess  No access to the library (value `0`).
 * @property LibAccess    Read-only access to the library (value `1`).
 * @property LibManageOwn Access plus the ability to manage the user's own
 *                        content (value `2`).
 * @property LibEditOnly  Edit access without full administrative rights
 *                        (value `3`).
 * @property LibFullEdit  Full edit access to all content (value `4`).
 * @property LibAdmin     Full administrative control over the library
 *                        (value `5`).
 */
export const LibraryPermissions = {
  LibNoAccess: 0,
  LibAccess: 1,
  LibManageOwn: 2,
  LibEditOnly: 3,
  LibFullEdit: 4,
  LibAdmin: 5,
} as const;

/**
 * Union of the numeric values defined by the {@link LibraryPermissions} const
 * object (`0 | 1 | 2 | 3 | 4 | 5`).
 *
 * Use this type to annotate variables, parameters and return values that
 * represent a library permission level, ensuring only valid values are
 * accepted.
 */
export type LibraryPermissions =
  (typeof LibraryPermissions)[keyof typeof LibraryPermissions];

/**
 * Array of string keys from the LibraryPermissions object
 * Used for permission lookups and UI components
 */
export const libraryPermissionKeys: string[] = Object.keys(LibraryPermissions);
