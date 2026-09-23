import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import {
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  PermissionDefinition,
  PermissionService,
  ProfileService,
  SpaceService,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { ALF_BASE_PATH, CBC_BASE_PATH } from 'app/core/variables';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { PermissionsComponent } from './permissions.component';

const mockNode: ModelNode = {
  id: 'node-1',
  type: 'cm:folder',
  parentId: 'parent-1',
  properties: { owner: 'user1', isUrl: 'false' },
};

const mockPerms: PermissionDefinition = {
  inherited: false,
  permissions: {
    profiles: [
      {
        profile: { groupName: 'GROUP_A', title: { en: 'Group A' } },
        permission: 'LibAccess',
        inherited: false,
      },
      {
        profile: { groupName: 'GROUP_EVERYONE' },
        permission: 'LibAccess',
        inherited: true,
      },
      {
        profile: { groupName: 'GROUP_B' },
        permission: 'Visibility',
        inherited: false,
      },
    ],
    users: [
      {
        user: { userId: 'u1', firstname: 'John', lastname: 'Doe' },
        permission: 'LibFullEdit',
        inherited: false,
      },
      {
        user: { userId: 'u2', firstname: 'Jane', lastname: 'Smith' },
        permission: 'CategoryAdmin',
        inherited: true,
      },
    ],
  },
};

const mockIg: InterestGroup = {
  name: 'test-ig',
  permissions: {
    directory: 'DirAccess',
    library: 'LibAccess',
    newsgroup: 'NwsAccess',
  },
};

let paramsSubject: Subject<Record<string, string>>;
let queryParamsSubject: Subject<Record<string, string>>;

const mockPermissionService = {
  getPermissionsAsync: vi.fn().mockResolvedValue(mockPerms),
  putPermissionAsync: vi.fn().mockResolvedValue(mockPerms),
  deletePermissionAsync: vi.fn().mockResolvedValue(undefined),
  clearPermissionsAsync: vi.fn().mockResolvedValue(undefined),
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue(mockNode),
};

const mockSpaceService = {
  getShareSpacesAsync: vi.fn().mockResolvedValue({ data: [], total: 0 }),
  deleteShareSpaceAsync: vi.fn().mockResolvedValue(undefined),
};

const mockGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
};

const mockProfileService = {
  getProfilesAsync: vi
    .fn()
    .mockResolvedValue([
      { name: 'admin', permissions: { library: 'LibAdmin' } },
    ]),
};

const mockUserService = {
  getUserAsync: vi.fn().mockResolvedValue({
    userId: 'user1',
    firstname: 'Owner',
    lastname: 'Name',
  }),
};

const mockUiMessageService = {
  addSuccessMessage: vi.fn(),
  addErrorMessage: vi.fn(),
};

const mockRouter = {
  navigate: vi.fn(),
};

describe('PermissionsComponent', () => {
  let component: PermissionsComponent;
  let fixture: ComponentFixture<PermissionsComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();
    mockPermissionService.getPermissionsAsync.mockResolvedValue(mockPerms);
    mockPermissionService.putPermissionAsync.mockResolvedValue(mockPerms);
    mockNodesService.getNodeAsync.mockResolvedValue(mockNode);
    mockSpaceService.getShareSpacesAsync.mockResolvedValue({
      data: [],
      total: 0,
    });
    mockGroupService.getInterestGroupAsync.mockResolvedValue(mockIg);
    mockProfileService.getProfilesAsync.mockResolvedValue([
      { name: 'admin', permissions: { library: 'LibAdmin' } },
    ]);
    mockUserService.getUserAsync.mockResolvedValue({
      userId: 'user1',
      firstname: 'Owner',
      lastname: 'Name',
    });

    paramsSubject = new Subject<Record<string, string>>();
    queryParamsSubject = new Subject<Record<string, string>>();

    await TestBed.configureTestingModule({
      imports: [PermissionsComponent],
      providers: [
        provideRouter([]),
        { provide: PermissionService, useValue: mockPermissionService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: UserService, useValue: mockUserService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Router, useValue: mockRouter },
        { provide: ALF_BASE_PATH, useValue: 'http://localhost/alfresco' },
        { provide: CBC_BASE_PATH, useValue: 'http://localhost/circabc' },
        {
          provide: I18nPipe,
          useValue: { transform: vi.fn().mockReturnValue('translated') },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable(),
            queryParams: queryParamsSubject.asObservable(),
          },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PermissionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  /** Emits the given route params/query params and lets the resources settle. */
  async function loadWithParams(
    params: Record<string, string>,
    queryParams: Record<string, string> = {}
  ) {
    paramsSubject.next(params);
    queryParamsSubject.next(queryParams);
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('route-driven loading', () => {
    it('should load node, permissions and profiles when params emit', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(mockPermissionService.getPermissionsAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
        id: 'ig-1',
      });
      expect(component.currentNode()).toEqual(mockNode);
      expect(component.perms()).toEqual(mockPerms);
    });

    it('should set from when queryParams emit', async () => {
      await loadWithParams(
        { id: 'ig-1', nodeId: 'node-1' },
        { from: 'library' }
      );
      expect(component.from()).toBe('library');
    });
  });

  describe('isFile / isFolder', () => {
    it('should return false for isFile when type includes folder', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:folder' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isFile()).toBe(false);
    });

    it('should return true for isFile when type does not include folder', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:content' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isFile()).toBe(true);
    });

    it('should return true for isFolder when type includes folder', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:folder' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isFolder()).toBe(true);
    });

    it('should return false for isFolder when type does not include folder', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:content' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isFolder()).toBe(false);
    });
  });

  describe('isLink', () => {
    it('should return true when isUrl property is true', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({
        type: 'cm:content',
        properties: { isUrl: 'true' },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isLink()).toBe(true);
    });

    it('should return false when isUrl property is false', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({
        type: 'cm:content',
        properties: { isUrl: 'false' },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isLink()).toBe(false);
    });

    it('should return false when no properties', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:content' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isLink()).toBe(false);
    });
  });

  describe('isSharedSpaceLink', () => {
    it('should return true when type includes folderlink', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({
        type: 'app:folderlink',
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isSharedSpaceLink()).toBe(true);
    });

    it('should return false when type does not include folderlink', async () => {
      mockNodesService.getNodeAsync.mockResolvedValue({ type: 'cm:folder' });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.isSharedSpaceLink()).toBe(false);
    });
  });

  describe('profilePermissionMap', () => {
    it('should filter out Visibility, Category, and Dir permissions', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.profilePermissionMap()['GROUP_A']).toBe('LibAccess');
      expect(component.profilePermissionMap()['GROUP_B']).toBeUndefined();
    });

    it('should concatenate multiple permissions with dash', async () => {
      mockPermissionService.getPermissionsAsync.mockResolvedValue({
        inherited: false,
        permissions: {
          profiles: [
            { profile: { groupName: 'G1' }, permission: 'LibAccess' },
            { profile: { groupName: 'G1' }, permission: 'NwsPost' },
          ],
        },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.profilePermissionMap()['G1']).toBe('LibAccess-NwsPost');
    });
  });

  describe('userPermissionMap', () => {
    it('should filter out Visibility and Category permissions for users', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.userPermissionMap()['u1']).toBe('LibFullEdit');
      expect(component.userPermissionMap()['u2']).toBeUndefined();
    });
  });

  describe('splitPermissions', () => {
    it('should split by dash when multiple permissions', () => {
      expect(component.splitPermissions('LibAccess-NwsPost')).toEqual([
        'LibAccess',
        'NwsPost',
      ]);
    });

    it('should return single-element array when no dash', () => {
      expect(component.splitPermissions('LibAccess')).toEqual(['LibAccess']);
    });
  });

  describe('isProfilePermissionDeletable', () => {
    it('should return true for non-EVERYONE profile when not inherited', async () => {
      mockPermissionService.getPermissionsAsync.mockResolvedValue({
        inherited: false,
        permissions: {},
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      const result = component.isProfilePermissionDeletable({
        profile: { groupName: 'GROUP_A' },
        permission: 'LibAccess',
        inherited: false,
      });
      expect(result).toBe(true);
    });

    it('should return false for GROUP_EVERYONE', async () => {
      mockPermissionService.getPermissionsAsync.mockResolvedValue({
        inherited: false,
        permissions: {},
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      const result = component.isProfilePermissionDeletable({
        profile: { groupName: 'GROUP_EVERYONE' },
        permission: 'LibAccess',
      });
      expect(result).toBe(false);
    });

    it('should return false when perms.inherited is true', async () => {
      mockPermissionService.getPermissionsAsync.mockResolvedValue({
        inherited: true,
        permissions: {},
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      const result = component.isProfilePermissionDeletable({
        profile: { groupName: 'GROUP_A' },
        permission: 'LibAccess',
      });
      expect(result).toBe(false);
    });
  });

  describe('hasUserPermissionDeletable', () => {
    it('should return true when user has non-inherited permission', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasUserPermissionDeletable('u1')).toBe(true);
    });

    it('should return false when user only has inherited permissions', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasUserPermissionDeletable('u2')).toBe(false);
    });

    it('should return false for unknown user', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasUserPermissionDeletable('unknown')).toBe(false);
    });
  });

  describe('hasProfilePermissionDeletable', () => {
    it('should return true when profile has non-inherited permission', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasProfilePermissionDeletable('GROUP_A')).toBe(true);
    });

    it('should return false when profile only has inherited permissions', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasProfilePermissionDeletable('GROUP_EVERYONE')).toBe(
        false
      );
    });
  });

  describe('shouldDisablePermissionSetting', () => {
    it('should return false when currentIg is undefined', () => {
      expect(component.shouldDisablePermissionSetting()).toBe(false);
    });

    it('should return true when directory is DirNoAccess and library is LibManageOwn', async () => {
      mockGroupService.getInterestGroupAsync.mockResolvedValue({
        name: 'ig',
        permissions: {
          directory: 'DirNoAccess',
          library: 'LibManageOwn',
          newsgroup: 'NwsAccess',
        },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.shouldDisablePermissionSetting()).toBe(true);
    });

    it('should return true when directory is DirNoAccess and newsgroup is NwsPost', async () => {
      mockGroupService.getInterestGroupAsync.mockResolvedValue({
        name: 'ig',
        permissions: {
          directory: 'DirNoAccess',
          library: 'LibAccess',
          newsgroup: 'NwsPost',
        },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.shouldDisablePermissionSetting()).toBe(true);
    });

    it('should return false when directory is not DirNoAccess', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.shouldDisablePermissionSetting()).toBe(false);
    });
  });

  describe('toggleInheritance', () => {
    it('should toggle inherited flag and call putPermission', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      await component.toggleInheritance();
      expect(mockPermissionService.putPermissionAsync).toHaveBeenCalledWith({
        id: 'node-1',
        permissionDefinition: {
          inherited: true,
          permissions: mockPerms.permissions,
        },
      });
    });
  });

  describe('addPermissionFinished', () => {
    it('should hide modal on cancel', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      component.showAddModal = true;
      await component.addPermissionFinished({ result: ActionResult.CANCELED });
      expect(component.showAddModal).toBe(false);
    });

    it('should reload permissions on success', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      component.showAddModal = true;
      mockPermissionService.getPermissionsAsync.mockClear();
      await component.addPermissionFinished({ result: ActionResult.SUCCEED });
      expect(component.showAddModal).toBe(false);
      expect(mockPermissionService.getPermissionsAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
    });
  });

  describe('getDisplayUser', () => {
    it('should return full name for known user', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.getDisplayUser('u1')).toBe('John Doe');
    });

    it('should return undefined for unknown user', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.getDisplayUser('unknown')).toBeUndefined();
    });
  });

  describe('getProfileTitle', () => {
    it('should return title for known profile', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.getProfileTitle('GROUP_A')).toEqual({ en: 'Group A' });
    });

    it('should return undefined for unknown profile', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.getProfileTitle('UNKNOWN')).toBeUndefined();
    });
  });

  describe('hasShares', () => {
    it('should return false when shares is empty', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasShares()).toBe(false);
    });

    it('should return true when shares has items', async () => {
      mockSpaceService.getShareSpacesAsync.mockResolvedValue({
        data: [{ igId: 'ig1', permission: 'LibAccess' }],
        total: 1,
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.hasShares()).toBe(true);
    });
  });

  describe('prepareDeletePermissionEntry / cancelPreparedPermissionEntry', () => {
    it('should set current item and show modal', () => {
      component.prepareDeletePermissionEntry('user1', 'user');
      expect(component.currentItem).toBe('user1');
      expect(component.currentItemKind).toBe('user');
      expect(component.showModalDelete).toBe(true);
    });

    it('should clear state on cancel', () => {
      component.prepareDeletePermissionEntry('user1', 'user');
      component.cancelPreparedPermissionEntry();
      expect(component.currentItem).toBe('');
      expect(component.currentItemKind).toBe('');
      expect(component.showModalDelete).toBe(false);
    });
  });

  describe('getNodeId', () => {
    it('should return node id when currentNode exists', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      expect(component.getNodeId()).toBe('node-1');
    });

    it('should return empty string when currentNode is undefined', () => {
      expect(component.getNodeId()).toBe('');
    });
  });

  describe('isForumNewsgroupsName', () => {
    it('should return true for Newsgroups with forums type', () => {
      expect(component.isForumNewsgroupsName('Newsgroups', 'ci:forums')).toBe(
        true
      );
    });

    it('should return false for other names', () => {
      expect(component.isForumNewsgroupsName('Library', 'ci:forums')).toBe(
        false
      );
    });
  });

  describe('showAddPermissionModal', () => {
    it('should set showAddModal to true when permission setting is not disabled', async () => {
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      component.showAddPermissionModal();
      expect(component.showAddModal).toBe(true);
    });

    it('should not set showAddModal when permission setting is disabled', async () => {
      mockGroupService.getInterestGroupAsync.mockResolvedValue({
        name: 'ig',
        permissions: {
          directory: 'DirNoAccess',
          library: 'LibManageOwn',
          newsgroup: 'NwsAccess',
        },
      });
      await loadWithParams({ id: 'ig-1', nodeId: 'node-1' });
      component.showAddModal = false;
      component.showAddPermissionModal();
      expect(component.showAddModal).toBe(false);
    });
  });
});
