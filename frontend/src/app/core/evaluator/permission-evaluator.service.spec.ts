import { TestBed } from '@angular/core/testing';
import {
  InterestGroup,
  Node as ModelNode,
  PermissionDefinition,
} from '../generated/circabc';
import { PermissionEvaluatorService } from './permission-evaluator.service';

describe('PermissionEvaluatorService', () => {
  let service: PermissionEvaluatorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PermissionEvaluatorService);
  });

  describe('isLibAdmin', () => {
    it('should return true when LibAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
      expect(service.isLibAdmin(node)).toBe(true);
    });

    it('should return false when LibAdmin is not ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'DENIED' } };
      expect(service.isLibAdmin(node)).toBe(false);
    });

    it('should return false when permissions is undefined', () => {
      const node: ModelNode = {};
      expect(service.isLibAdmin(node)).toBe(false);
    });

    it('should return false when node is undefined', () => {
      expect(service.isLibAdmin(undefined as unknown as ModelNode)).toBe(false);
    });
  });

  describe('isLibFullEdit', () => {
    it('should return true when LibFullEdit is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibFullEdit: 'ALLOWED' } };
      expect(service.isLibFullEdit(node)).toBe(true);
    });

    it('should return false when LibFullEdit is not present', () => {
      const node: ModelNode = { permissions: {} };
      expect(service.isLibFullEdit(node)).toBe(false);
    });
  });

  describe('isLibAccess', () => {
    it('should return true when LibAccess is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAccess: 'ALLOWED' } };
      expect(service.isLibAccess(node)).toBe(true);
    });
  });

  describe('isLibNoAccess', () => {
    it('should return true when LibNoAccess is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibNoAccess: 'ALLOWED' } };
      expect(service.isLibNoAccess(node)).toBe(true);
    });
  });

  describe('isLibManageOwnOrHigher', () => {
    it('should return true when LibManageOwn is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibManageOwn: 'ALLOWED' } };
      expect(service.isLibManageOwnOrHigher(node)).toBe(true);
    });

    it('should return true when LibEditOnly is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibEditOnly: 'ALLOWED' } };
      expect(service.isLibManageOwnOrHigher(node)).toBe(true);
    });

    it('should return true when LibFullEdit is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibFullEdit: 'ALLOWED' } };
      expect(service.isLibManageOwnOrHigher(node)).toBe(true);
    });

    it('should return true when LibAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
      expect(service.isLibManageOwnOrHigher(node)).toBe(true);
    });

    it('should return false when only LibAccess is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAccess: 'ALLOWED' } };
      expect(service.isLibManageOwnOrHigher(node)).toBe(false);
    });
  });

  describe('isLibAdminOrFullEdit', () => {
    it('should return true when LibFullEdit is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibFullEdit: 'ALLOWED' } };
      expect(service.isLibAdminOrFullEdit(node)).toBe(true);
    });

    it('should return true when LibAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibAdmin: 'ALLOWED' } };
      expect(service.isLibAdminOrFullEdit(node)).toBe(true);
    });

    it('should return false when only LibManageOwn is ALLOWED', () => {
      const node: ModelNode = { permissions: { LibManageOwn: 'ALLOWED' } };
      expect(service.isLibAdminOrFullEdit(node)).toBe(false);
    });
  });

  describe('isOwner', () => {
    it('should return true when userName matches owner property', () => {
      const node: ModelNode = { properties: { owner: 'john' } };
      expect(service.isOwner(node, 'john')).toBe(true);
    });

    it('should return false when userName does not match', () => {
      const node: ModelNode = { properties: { owner: 'john' } };
      expect(service.isOwner(node, 'jane')).toBe(false);
    });

    it('should return false when properties is undefined', () => {
      const node: ModelNode = {};
      expect(service.isOwner(node, 'john')).toBe(false);
    });
  });

  describe('isDirAdmin', () => {
    it('should return true when directory permission is DirAdmin', () => {
      const ig: InterestGroup = {
        name: 'test',
        permissions: { directory: 'DirAdmin' },
      };
      expect(service.isDirAdmin(ig)).toBe(true);
    });

    it('should return false when directory permission is not DirAdmin', () => {
      const ig: InterestGroup = {
        name: 'test',
        permissions: { directory: 'DirAccess' },
      };
      expect(service.isDirAdmin(ig)).toBe(false);
    });

    it('should return false when permissions is undefined', () => {
      const ig = { name: 'test' } as InterestGroup;
      expect(service.isDirAdmin(ig)).toBe(false);
    });
  });

  describe('isDirManageMembers', () => {
    it('should return true when directory permission is DirManageMembers', () => {
      const ig: InterestGroup = {
        name: 'test',
        permissions: { directory: 'DirManageMembers' },
      };
      expect(service.isDirManageMembers(ig)).toBe(true);
    });
  });

  describe('isGroupAdmin', () => {
    it('should return true when LibAdmin is ALLOWED', () => {
      const ig = {
        name: 'test',
        permissions: { LibAdmin: 'ALLOWED' },
      } as unknown as InterestGroup;
      expect(service.isGroupAdmin(ig)).toBe(true);
    });

    it('should return true when directory is DirAdmin', () => {
      const ig: InterestGroup = {
        name: 'test',
        permissions: { directory: 'DirAdmin' },
      };
      expect(service.isGroupAdmin(ig)).toBe(true);
    });

    it('should return true when EveAdmin is ALLOWED', () => {
      const ig = {
        name: 'test',
        permissions: { EveAdmin: 'ALLOWED' },
      } as unknown as InterestGroup;
      expect(service.isGroupAdmin(ig)).toBe(true);
    });

    it('should return false when no admin permissions', () => {
      const ig: InterestGroup = {
        name: 'test',
        permissions: { directory: 'DirAccess' },
      };
      expect(service.isGroupAdmin(ig)).toBe(false);
    });
  });

  describe('isNewsgroupPost', () => {
    it('should return true when NwsPost is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsPost: 'ALLOWED' } };
      expect(service.isNewsgroupPost(node)).toBe(true);
    });

    it('should return true when NwsModerate is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsModerate: 'ALLOWED' } };
      expect(service.isNewsgroupPost(node)).toBe(true);
    });

    it('should return true when NwsAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsAdmin: 'ALLOWED' } };
      expect(service.isNewsgroupPost(node)).toBe(true);
    });

    it('should return false when only NwsAccess is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsAccess: 'ALLOWED' } };
      expect(service.isNewsgroupPost(node)).toBe(false);
    });
  });

  describe('isNewsgroupModerate', () => {
    it('should return true when NwsModerate is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsModerate: 'ALLOWED' } };
      expect(service.isNewsgroupModerate(node)).toBe(true);
    });

    it('should return true when NwsAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsAdmin: 'ALLOWED' } };
      expect(service.isNewsgroupModerate(node)).toBe(true);
    });

    it('should return false when only NwsPost is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsPost: 'ALLOWED' } };
      expect(service.isNewsgroupModerate(node)).toBe(false);
    });
  });

  describe('isNewsgroupAdmin', () => {
    it('should return true when NwsAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsAdmin: 'ALLOWED' } };
      expect(service.isNewsgroupAdmin(node)).toBe(true);
    });

    it('should return false when NwsModerate is ALLOWED but not NwsAdmin', () => {
      const node: ModelNode = { permissions: { NwsModerate: 'ALLOWED' } };
      expect(service.isNewsgroupAdmin(node)).toBe(false);
    });
  });

  describe('canModerateNewsgroup', () => {
    it('should return true when NwsModerate is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsModerate: 'ALLOWED' } };
      expect(service.canModerateNewsgroup(node)).toBe(true);
    });

    it('should return true when NwsAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsAdmin: 'ALLOWED' } };
      expect(service.canModerateNewsgroup(node)).toBe(true);
    });

    it('should return false when only NwsPost is ALLOWED', () => {
      const node: ModelNode = { permissions: { NwsPost: 'ALLOWED' } };
      expect(service.canModerateNewsgroup(node)).toBe(false);
    });
  });

  describe('canDeleteForum', () => {
    it('should return true when DeleteForum is ALLOWED', () => {
      const node: ModelNode = { permissions: { DeleteForum: 'ALLOWED' } };
      expect(service.canDeleteForum(node)).toBe(true);
    });

    it('should return false when DeleteForum is not ALLOWED', () => {
      const node: ModelNode = { permissions: { DeleteForum: 'DENIED' } };
      expect(service.canDeleteForum(node)).toBe(false);
    });

    it('should return false when node is undefined', () => {
      expect(service.canDeleteForum(undefined as unknown as ModelNode)).toBe(
        false
      );
    });
  });

  describe('isEveAdmin', () => {
    it('should return true when EveAdmin is ALLOWED', () => {
      const node: ModelNode = { permissions: { EveAdmin: 'ALLOWED' } };
      expect(service.isEveAdmin(node)).toBe(true);
    });

    it('should return false when EveAdmin is not present', () => {
      const node: ModelNode = { permissions: {} };
      expect(service.isEveAdmin(node)).toBe(false);
    });
  });

  describe('hasGuestProfileAccess', () => {
    it('should return true when guest profile has access', () => {
      const perms: PermissionDefinition = {
        inherited: false,
        permissions: {
          profiles: [{ profile: { name: 'guest' }, permission: 'LibAccess' }],
        },
      };
      expect(service.hasGuestProfileAccess(perms, 'LibNoAccess')).toBe(true);
    });

    it('should return false when guest profile has noAccess permission', () => {
      const perms: PermissionDefinition = {
        inherited: false,
        permissions: {
          profiles: [{ profile: { name: 'guest' }, permission: 'LibNoAccess' }],
        },
      };
      expect(service.hasGuestProfileAccess(perms, 'LibNoAccess')).toBe(false);
    });

    it('should return false when no guest profile exists', () => {
      const perms: PermissionDefinition = {
        inherited: false,
        permissions: {
          profiles: [{ profile: { name: 'admin' }, permission: 'LibAdmin' }],
        },
      };
      expect(service.hasGuestProfileAccess(perms, 'LibNoAccess')).toBe(false);
    });

    it('should return false when perms is null', () => {
      expect(
        service.hasGuestProfileAccess(
          null as unknown as PermissionDefinition,
          'LibNoAccess'
        )
      ).toBe(false);
    });

    it('should return false when perms is undefined', () => {
      expect(
        service.hasGuestProfileAccess(
          undefined as unknown as PermissionDefinition,
          'LibNoAccess'
        )
      ).toBe(false);
    });
  });

  describe('getLibraryPermissions', () => {
    it('should return the expected permission list', () => {
      expect(service.getLibraryPermissions()).toEqual([
        'LibNoAccess',
        'LibAccess',
        'LibManageOwn',
        'LibFullEdit',
        'LibAdmin',
      ]);
    });
  });

  describe('getLibraryContentPermissions', () => {
    it('should return the expected permission list', () => {
      expect(service.getLibraryContentPermissions()).toEqual([
        'LibNoAccess',
        'LibAccess',
        'LibEditOnly',
        'LibFullEdit',
        'LibAdmin',
      ]);
    });
  });

  describe('getLibraryFolderPermissions', () => {
    it('should return the expected permission list', () => {
      expect(service.getLibraryFolderPermissions()).toEqual([
        'LibNoAccess',
        'LibAccess',
        'LibEditOnly',
        'LibManageOwn',
        'LibFullEdit',
        'LibAdmin',
      ]);
    });
  });

  describe('geNewsgroupsPermissions', () => {
    it('should return the expected permission list', () => {
      expect(service.geNewsgroupsPermissions()).toEqual([
        'NwsNoAccess',
        'NwsAccess',
        'NwsPost',
        'NwsModerate',
        'NwsAdmin',
      ]);
    });
  });
});
