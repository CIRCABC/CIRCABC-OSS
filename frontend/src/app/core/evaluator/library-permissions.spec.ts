import {
  LibraryPermissions,
  libraryPermissionKeys,
} from './library-permissions';

describe('LibraryPermissions', () => {
  it('should define all permission levels with correct numeric values', () => {
    expect(LibraryPermissions.LibNoAccess).toBe(0);
    expect(LibraryPermissions.LibAccess).toBe(1);
    expect(LibraryPermissions.LibManageOwn).toBe(2);
    expect(LibraryPermissions.LibEditOnly).toBe(3);
    expect(LibraryPermissions.LibFullEdit).toBe(4);
    expect(LibraryPermissions.LibAdmin).toBe(5);
  });

  it('should have 6 permission levels', () => {
    expect(Object.keys(LibraryPermissions)).toHaveLength(6);
  });
});

describe('libraryPermissionKeys', () => {
  it('should contain all permission key names', () => {
    expect(libraryPermissionKeys).toEqual([
      'LibNoAccess',
      'LibAccess',
      'LibManageOwn',
      'LibEditOnly',
      'LibFullEdit',
      'LibAdmin',
    ]);
  });

  it('should have the same length as LibraryPermissions entries', () => {
    expect(libraryPermissionKeys).toHaveLength(
      Object.keys(LibraryPermissions).length
    );
  });
});
