export enum DirectoryPermissions {
  DirNoAccess,
  DirAccess,
  DirManageMembers,
  DirAdmin,
}

export const directoryPermissionKeys = Object.keys(DirectoryPermissions).filter(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (k) => typeof DirectoryPermissions[k as any] === 'number'
);
