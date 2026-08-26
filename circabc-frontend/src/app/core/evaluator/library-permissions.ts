export const LibraryPermissions = {
  LibNoAccess: 0,
  LibAccess: 1,
  LibManageOwn: 2,
  LibEditOnly: 3,
  LibFullEdit: 4,
  LibAdmin: 5,
} as const;

export type LibraryPermissions =
  (typeof LibraryPermissions)[keyof typeof LibraryPermissions];

/**
 * Array of string keys from the LibraryPermissions object
 * Used for permission lookups and UI components
 */
export const libraryPermissionKeys: string[] = Object.keys(LibraryPermissions);
