import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ComponentRef } from '@angular/core';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroupService,
  MembersService,
  Node as ModelNode,
  PermissionDefinition,
  PermissionService,
  ProfileService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddPermissionsComponent } from './add-permissions.component';

const mockNode: ModelNode = {
  id: 'node-1',
  type: '{http://www.alfresco.org/model/content/1.0}folder',
  service: 'library',
  permissions: { LibAdmin: 'ALLOWED' },
};

const mockPermDef: PermissionDefinition = {
  inherited: true,
  permissions: { profiles: [], users: [] },
};

const mockPermissionService = {
  getPermissionsAsync: vi.fn().mockResolvedValue(mockPermDef),
  putPermission: vi.fn().mockReturnValue(of(mockPermDef)),
  putPermissionAsync: vi.fn().mockResolvedValue(mockPermDef),
};

const mockPermEvalService = {
  getLibraryContentPermissions: vi
    .fn()
    .mockReturnValue([
      'LibNoAccess',
      'LibAccess',
      'LibEditOnly',
      'LibFullEdit',
      'LibAdmin',
    ]),
  getLibraryFolderPermissions: vi
    .fn()
    .mockReturnValue([
      'LibNoAccess',
      'LibAccess',
      'LibEditOnly',
      'LibManageOwn',
      'LibFullEdit',
      'LibAdmin',
    ]),
  geNewsgroupsPermissions: vi
    .fn()
    .mockReturnValue([
      'NwsNoAccess',
      'NwsAccess',
      'NwsPost',
      'NwsModerate',
      'NwsAdmin',
    ]),
  isDirAdmin: vi.fn().mockReturnValue(false),
  isDirManageMembers: vi.fn().mockReturnValue(false),
};

const mockI18nPipe = {
  transform: vi.fn().mockReturnValue('Translated'),
};

// The template renders <cbc-users-picker>, whose own ngOnInit resolves the
// current interest group; these mocks satisfy that child component so the
// parent's `resource()` can be exercised via `fixture.detectChanges()`.
const mockInterestGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue({ id: 'ig-1' }),
};
const mockProfileService = {};
const mockMembersService = {};
const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

describe('AddPermissionsComponent', () => {
  let component: AddPermissionsComponent;
  let componentRef: ComponentRef<AddPermissionsComponent>;
  let fixture: ComponentFixture<AddPermissionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddPermissionsComponent],
      providers: [
        provideRouter([]),
        { provide: PermissionService, useValue: mockPermissionService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddPermissionsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('node', mockNode);
    componentRef.setInput('ig', 'ig-1');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('loading permissions', () => {
    it('should initialize form and fetch permissions', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.newPermissionsForm).toBeDefined();
      expect(component.perms()).toEqual(mockPermDef);
      expect(mockPermissionService.getPermissionsAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
    });

    it('should determine folder permissions for library folder node', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.options()).toEqual(
        mockPermEvalService.getLibraryFolderPermissions()
      );
    });
  });

  describe('isContent', () => {
    it('should return true when node type ends with }content', () => {
      componentRef.setInput('node', {
        ...mockNode,
        type: '{http://www.alfresco.org/model/content/1.0}content',
      });
      expect(component.isContent()).toBe(true);
    });

    it('should return false for folder type', () => {
      expect(component.isContent()).toBe(false);
    });
  });

  describe('determineNodePermissionSet', () => {
    it('should return newsgroup permissions for non-library service', () => {
      componentRef.setInput('node', {
        ...mockNode,
        service: 'newsgroups' as ModelNode.ServiceEnum,
      });
      component.permissionCache = [];

      const result = component.determineNodePermissionSet();

      expect(mockPermEvalService.geNewsgroupsPermissions).toHaveBeenCalled();
      expect(result).toEqual([
        'NwsNoAccess',
        'NwsAccess',
        'NwsPost',
        'NwsModerate',
        'NwsAdmin',
      ]);
    });

    it('should return content permissions for library content node', () => {
      componentRef.setInput('node', {
        ...mockNode,
        type: '{http://www.alfresco.org/model/content/1.0}content',
      });
      component.permissionCache = [];

      const result = component.determineNodePermissionSet();

      expect(
        mockPermEvalService.getLibraryContentPermissions
      ).toHaveBeenCalled();
      expect(result).toEqual([
        'LibNoAccess',
        'LibAccess',
        'LibEditOnly',
        'LibFullEdit',
        'LibAdmin',
      ]);
    });
  });

  describe('cancelWizard', () => {
    it('should emit canceled result', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      const emitSpy = vi.spyOn(component.finished, 'emit');

      component.cancelWizard();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.CANCELED })
      );
    });
  });

  describe('assign', () => {
    it('should populate newPermsModel from form value with user', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      const user = { userId: 'user1', firstname: 'John', lastname: 'Doe' };
      component.newPermissionsForm.controls['invitedUsersOrProfiles'].setValue([
        user,
      ]);

      component.assign();

      expect(component.newPermsModel['user1']).toBeDefined();
      expect(component.newPermsModel['user1'].authority).toEqual(user);
    });

    it('should populate newPermsModel from form value with profile', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      const profile = { groupName: 'group1', name: 'TestProfile', title: {} };
      component.newPermissionsForm.controls['invitedUsersOrProfiles'].setValue([
        profile,
      ]);

      component.assign();

      expect(component.newPermsModel['group1']).toBeDefined();
      expect(component.newPermsModel['group1'].authority).toEqual(profile);
    });
  });

  describe('getAuthorityDisplay', () => {
    it('should return full name for user authority', () => {
      const user = { userId: 'u1', firstname: 'Jane', lastname: 'Smith' };
      const result = component.getAuthorityDisplay(user as never);
      expect(result).toBe('Jane  Smith');
    });

    it('should return translated title for profile authority', () => {
      const profile = { name: 'admin', title: { en: 'Administrator' } };
      const result = component.getAuthorityDisplay(profile as never);
      expect(result).toBe('Translated');
    });

    it('should return profile name when title is empty', () => {
      mockI18nPipe.transform.mockReturnValueOnce('');
      const profile = { name: 'admin', title: {} };
      const result = component.getAuthorityDisplay(profile as never);
      expect(result).toBe('admin');
    });
  });

  describe('removePerm', () => {
    it('should remove the permission entry by key', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.newPermsModel = {
        user1: {
          authority: { userId: 'user1' } as never,
          permission: 'LibAccess',
        },
      };

      component.removePerm('user1');

      expect(component.newPermsModel['user1']).toBeUndefined();
      expect(component.configModel).toEqual([]);
    });
  });

  describe('addPermissions', () => {
    it('should call putPermission and emit success', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.newPermsModel = {
        user1: {
          authority: { userId: 'user1', firstname: 'A', lastname: 'B' },
          permission: 'LibAccess',
        },
      };
      const emitSpy = vi.spyOn(component.finished, 'emit');

      await component.addPermissions();

      expect(mockPermissionService.putPermissionAsync).toHaveBeenCalledWith({
        id: 'node-1',
        permissionDefinition: expect.objectContaining({ inherited: true }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.SUCCEED })
      );
      expect(component.adding()).toBe(false);
      expect(component.perms()).toEqual(mockPermDef);
    });

    it('should emit failed result on error', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      mockPermissionService.putPermissionAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      component.newPermsModel = {
        user1: {
          authority: { userId: 'user1', firstname: 'A', lastname: 'B' },
          permission: 'LibAccess',
        },
      };
      const emitSpy = vi.spyOn(component.finished, 'emit');

      await component.addPermissions();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.FAILED })
      );
      expect(component.adding()).toBe(false);
    });
  });

  describe('isShowAddButton', () => {
    it('should return false when newPermsModel is empty', () => {
      expect(component.isShowAddButton()).toBe(false);
    });

    it('should return true when newPermsModel has entries', () => {
      component.newPermsModel = {
        user1: { authority: {} as never, permission: 'LibAccess' },
      };
      expect(component.isShowAddButton()).toBe(true);
    });
  });

  describe('updateModel', () => {
    it('should update permission for given authority key', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.newPermsModel = {
        user1: { authority: {} as never, permission: 'LibAccess' },
      };

      component.updateModel('user1', 'LibAdmin');

      expect(component.newPermsModel['user1'].permission).toBe('LibAdmin');
    });
  });

  describe('toggleHelp', () => {
    it('should toggle showHelpLayout', () => {
      expect(component.showHelpLayout).toBe(false);
      component.toggleHelp();
      expect(component.showHelpLayout).toBe(true);
      component.toggleHelp();
      expect(component.showHelpLayout).toBe(false);
    });
  });
});
