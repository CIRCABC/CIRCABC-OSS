import {
  InformationPermissions,
  informationPermissionKeys,
} from './information-permissions';

describe('InformationPermissions', () => {
  it('should define correct permission levels', () => {
    expect(InformationPermissions.InfNoAccess).toBe(0);
    expect(InformationPermissions.InfAccess).toBe(1);
    expect(InformationPermissions.InfManage).toBe(2);
    expect(InformationPermissions.InfAdmin).toBe(3);
  });

  it('should export informationPermissionKeys matching object keys', () => {
    expect(informationPermissionKeys).toEqual([
      'InfNoAccess',
      'InfAccess',
      'InfManage',
      'InfAdmin',
    ]);
  });
});
