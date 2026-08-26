/* eslint-disable @typescript-eslint/no-explicit-any */
export const InformationPermissions = {
  InfNoAccess: 0,
  InfAccess: 1,
  InfManage: 2,
  InfAdmin: 3,
} as const;

export type InformationPermissions =
  (typeof InformationPermissions)[keyof typeof InformationPermissions];

/**
 * Array of string keys from the InformationPermissions object
 * Used for permission lookups and UI components
 */
export const informationPermissionKeys: string[] = Object.keys(
  InformationPermissions
);
