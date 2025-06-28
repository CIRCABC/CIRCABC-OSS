export const DirectoryPermissions = {
  DirNoAccess: 0,
  DirAccess: 1,
  DirManageMembers: 2,
  DirAdmin: 3,
} as const;

export type DirectoryPermissions =
  (typeof DirectoryPermissions)[keyof typeof DirectoryPermissions];

/**
 * Array of string keys from the DirectoryPermissions object
 * Used for permission lookups and UI components
 */
export const directoryPermissionKeys: string[] =
  Object.keys(DirectoryPermissions);
