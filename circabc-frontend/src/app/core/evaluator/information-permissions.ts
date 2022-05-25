/* eslint-disable @typescript-eslint/no-explicit-any */
enum InformationPermissions {
  InfNoAccess,
  InfAccess,
  InfManage,
  InfAdmin,
}

export const informationPermissionKeys = Object.keys(
  InformationPermissions
).filter((k) => typeof InformationPermissions[k as any] === 'number');
