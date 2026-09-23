import {
  DirectoryPermissions,
  directoryPermissionKeys,
} from './directory-permissions';

describe('DirectoryPermissions', () => {
  it('should have correct numeric values', () => {
    expect(DirectoryPermissions.DirNoAccess).toBe(0);
    expect(DirectoryPermissions.DirAccess).toBe(1);
    expect(DirectoryPermissions.DirManageMembers).toBe(2);
    expect(DirectoryPermissions.DirAdmin).toBe(3);
  });

  it('should export directoryPermissionKeys matching object keys', () => {
    expect(directoryPermissionKeys).toEqual([
      'DirNoAccess',
      'DirAccess',
      'DirManageMembers',
      'DirAdmin',
    ]);
  });
});
