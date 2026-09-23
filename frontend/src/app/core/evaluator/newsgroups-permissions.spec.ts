import {
  NewsgroupsPermissions,
  newsGroupPermissionKeys,
} from './newsgroups-permissions';

describe('NewsgroupsPermissions', () => {
  it('should have correct numeric values', () => {
    expect(NewsgroupsPermissions.NwsNoAccess).toBe(0);
    expect(NewsgroupsPermissions.NwsAccess).toBe(1);
    expect(NewsgroupsPermissions.NwsPost).toBe(2);
    expect(NewsgroupsPermissions.NwsModerate).toBe(3);
    expect(NewsgroupsPermissions.NwsAdmin).toBe(4);
  });

  it('should have exactly 5 permission levels', () => {
    expect(Object.keys(NewsgroupsPermissions)).toHaveLength(5);
  });
});

describe('newsGroupPermissionKeys', () => {
  it('should contain all permission key names', () => {
    expect(newsGroupPermissionKeys).toEqual([
      'NwsNoAccess',
      'NwsAccess',
      'NwsPost',
      'NwsModerate',
      'NwsAdmin',
    ]);
  });

  it('should have the same length as NewsgroupsPermissions keys', () => {
    expect(newsGroupPermissionKeys).toHaveLength(
      Object.keys(NewsgroupsPermissions).length
    );
  });
});
