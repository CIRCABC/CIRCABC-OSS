export enum LibraryPermissions {
  LibNoAccess,
  LibAccess,
  LibManageOwn,
  LibEditOnly,
  LibFullEdit,
  LibAdmin,
}

export const libraryPermissionKeys = Object.keys(LibraryPermissions).filter(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (k) => typeof LibraryPermissions[k as any] === 'number'
);
