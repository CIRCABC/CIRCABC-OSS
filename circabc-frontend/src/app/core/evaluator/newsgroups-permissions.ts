export enum NewsgroupsPermissions {
  NwsNoAccess,
  NwsAccess,
  NwsPost,
  NwsModerate,
  NwsAdmin,
}

export const newsGroupPermissionKeys = Object.keys(
  NewsgroupsPermissions
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
).filter((k) => typeof NewsgroupsPermissions[k as any] === 'number');
