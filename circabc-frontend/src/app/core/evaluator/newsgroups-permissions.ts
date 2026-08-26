export const NewsgroupsPermissions = {
  NwsNoAccess: 0,
  NwsAccess: 1,
  NwsPost: 2,
  NwsModerate: 3,
  NwsAdmin: 4,
} as const;

export type NewsgroupsPermissions =
  (typeof NewsgroupsPermissions)[keyof typeof NewsgroupsPermissions];

/**
 * Array of string keys from the NewsgroupsPermissions object
 * Used for permission lookups and UI components
 */
export const newsGroupPermissionKeys: string[] = Object.keys(
  NewsgroupsPermissions
);
