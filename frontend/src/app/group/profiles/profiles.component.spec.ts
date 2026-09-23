import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroup,
  InterestGroupProfile,
  InterestGroupService,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ProfilesComponent } from './profiles.component';

function flushPromises() {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

const mockGroup: InterestGroup = {
  id: 'group1',
  name: 'Test Group',
  permissions: { directory: 'DirAdmin' },
};

const mockProfiles: Profile[] = [
  {
    id: 'p1',
    name: 'leader',
    permissions: {
      library: 'LibAdmin',
      information: 'InfAdmin',
      events: 'EveAdmin',
      forums: 'NwsAdmin',
      members: 'DirAdmin',
    },
  },
  {
    id: 'p2',
    name: 'author',
    permissions: { library: 'LibManageOwn' },
    exported: false,
    imported: false,
  },
  { id: 'p3', name: 'guest' },
  { id: 'p4', name: 'EVERYONE' },
];

const paramsSubject = new Subject<{ [key: string]: string }>();

const mockProfileService = {
  getProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
  putProfile: vi.fn().mockReturnValue(of({})),
  putProfileAsync: vi.fn().mockResolvedValue({}),
};

const mockGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockGroup),
};

const mockUserService = {
  getUserMembershipAsync: vi
    .fn()
    .mockResolvedValue([] as InterestGroupProfile[]),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getUser: vi.fn().mockReturnValue({ userId: 'testuser' }),
};

const mockPermEvalService = {
  isDirAdmin: vi.fn().mockReturnValue(true),
  isDirManageMembers: vi.fn().mockReturnValue(false),
};

describe('ProfilesComponent', () => {
  let component: ProfilesComponent;
  let fixture: ComponentFixture<ProfilesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfilesComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit / loadProfiles', () => {
    it('should load profiles when route params emit', async () => {
      paramsSubject.next({ id: 'group1' });
      await flushPromises();

      expect(component.nodeId).toBe('group1');
      expect(component.profiles).toEqual(mockProfiles);
      expect(component.currentGroup).toEqual(mockGroup);
      expect(component.loading).toBe(false);
    });

    it('should set alreadyMember when user is member of the group', async () => {
      const memberships: InterestGroupProfile[] = [
        {
          interestGroup: { id: 'group1', name: 'Test Group', permissions: {} },
        },
      ];
      mockUserService.getUserMembershipAsync.mockResolvedValue(memberships);

      paramsSubject.next({ id: 'group1' });
      await flushPromises();

      expect(component.alreadyMember).toBe(true);
    });
  });

  describe('isDeletable', () => {
    it('should return false for guest profile', () => {
      expect(component.isDeletable({ name: 'guest' })).toBe(false);
    });

    it('should return false for EVERYONE profile', () => {
      expect(component.isDeletable({ name: 'EVERYONE' })).toBe(false);
    });

    it('should return false for exported profile', () => {
      expect(
        component.isDeletable({
          name: 'author',
          exported: true,
          permissions: {},
        })
      ).toBe(false);
    });

    it('should return true for a regular editable non-exported profile', () => {
      expect(
        component.isDeletable({
          name: 'author',
          exported: false,
          permissions: {},
        })
      ).toBe(true);
    });
  });

  describe('isEditable', () => {
    it('should return false for leader profile with all admin permissions', () => {
      const profile: Profile = {
        name: 'leader',
        permissions: {
          library: 'LibAdmin',
          information: 'InfAdmin',
          events: 'EveAdmin',
          forums: 'NwsAdmin',
          members: 'DirAdmin',
        },
      };
      expect(component.isEditable(profile)).toBe(false);
    });

    it('should return false for IGLeader', () => {
      expect(component.isEditable({ name: 'IGLeader', permissions: {} })).toBe(
        false
      );
    });

    it('should return true for a regular profile', () => {
      expect(
        component.isEditable({
          name: 'author',
          permissions: { library: 'LibManageOwn' },
        })
      ).toBe(true);
    });

    it('should return true when name or permissions are undefined', () => {
      expect(component.isEditable({})).toBe(true);
    });
  });

  describe('isExportable', () => {
    it('should return false when exportFeatureEnabled is false', () => {
      component.exportFeatureEnabled = false;
      expect(component.isExportable({ name: 'author', permissions: {} })).toBe(
        false
      );
    });

    it('should return true for deletable non-exported non-imported profile when feature enabled', () => {
      component.exportFeatureEnabled = true;
      expect(
        component.isExportable({
          name: 'author',
          exported: false,
          imported: false,
          permissions: {},
        })
      ).toBe(true);
    });
  });

  describe('isUnexportable', () => {
    it('should return true for exported profile with no exportedRefs', () => {
      expect(
        component.isUnexportable({ exported: true, exportedRefs: [] })
      ).toBe(true);
    });

    it('should return false for non-exported profile', () => {
      expect(component.isUnexportable({ exported: false })).toBe(false);
    });

    it('should return false for exported profile with refs', () => {
      expect(
        component.isUnexportable({ exported: true, exportedRefs: ['ref1'] })
      ).toBe(false);
    });
  });

  describe('prepareDelete', () => {
    it('should set selectedProfile and show delete modal', () => {
      const profile: Profile = { id: 'p2', name: 'author' };
      component.prepareDelete(profile);
      expect(component.selectedProfile).toBe(profile);
      expect(component.showDeleteModal).toBe(true);
    });
  });

  describe('prepareEdit', () => {
    it('should set selectedProfile and show modal', () => {
      const profile: Profile = { id: 'p2', name: 'author' };
      component.prepareEdit(profile);
      expect(component.selectedProfile).toBe(profile);
      expect(component.showModal).toBe(true);
    });
  });

  describe('onModalCancel', () => {
    it('should clear selectedProfile and hide modals', () => {
      component.selectedProfile = { id: 'p2' };
      component.showModal = true;
      component.showDeleteModal = true;

      component.onModalCancel();

      expect(component.selectedProfile).toBeUndefined();
      expect(component.showModal).toBe(false);
      expect(component.showDeleteModal).toBe(false);
    });
  });

  describe('onProfileCreated', () => {
    it('should reload profiles and hide modal on success', async () => {
      component.nodeId = 'group1';
      component.showModal = true;

      const result: ActionEmitterResult = {
        type: ActionType.CREATE_PROFILE,
        result: ActionResult.SUCCEED,
      };
      await component.onProfileCreated(result);

      expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(component.showModal).toBe(false);
    });
  });

  describe('onProfileDeleted', () => {
    it('should hide modal and clear selection on cancel', async () => {
      component.selectedProfile = { id: 'p2' };
      component.showDeleteModal = true;

      const result: ActionEmitterResult = {
        type: ActionType.DELETE_PROFILE,
        result: ActionResult.CANCELED,
      };
      await component.onProfileDeleted(result);

      expect(component.selectedProfile).toBeUndefined();
      expect(component.showDeleteModal).toBe(false);
    });

    it('should reload profiles on success', async () => {
      component.nodeId = 'group1';
      component.selectedProfile = { id: 'p2' };
      component.showDeleteModal = true;

      const result: ActionEmitterResult = {
        type: ActionType.DELETE_PROFILE,
        result: ActionResult.SUCCEED,
      };
      await component.onProfileDeleted(result);

      expect(component.selectedProfile).toBeUndefined();
      expect(component.showDeleteModal).toBe(false);
      expect(mockProfileService.getProfilesAsync).toHaveBeenCalled();
    });
  });

  describe('export / unexport', () => {
    it('should set exported to true and call putProfile', async () => {
      const profile: Profile = { id: 'p2', name: 'author', exported: false };
      await component.export(profile);

      expect(profile.exported).toBe(true);
      expect(mockProfileService.putProfileAsync).toHaveBeenCalledWith({
        id: 'p2',
        profile,
      });
    });

    it('should set exported to false and call putProfile', async () => {
      const profile: Profile = { id: 'p2', name: 'author', exported: true };
      await component.unexport(profile);

      expect(profile.exported).toBe(false);
      expect(mockProfileService.putProfileAsync).toHaveBeenCalledWith({
        id: 'p2',
        profile,
      });
    });
  });

  describe('showAddProfile / showImportProfile', () => {
    it('should clear selectedProfile, show modal, and hide dropdown', () => {
      component.selectedProfile = { id: 'p2' };
      component.showAddDropdown = true;

      component.showAddProfile();

      expect(component.selectedProfile).toBeUndefined();
      expect(component.showModal).toBe(true);
      expect(component.showAddDropdown).toBe(false);
    });

    it('should show import modal and hide dropdown', () => {
      component.showAddDropdown = true;

      component.showImportProfile();

      expect(component.showImportModal).toBe(true);
      expect(component.showAddDropdown).toBe(false);
    });
  });

  describe('isDirAdmin / isDirManageMembers', () => {
    it('should delegate to permEvalService', () => {
      paramsSubject.next({ id: 'group1' });

      expect(component.isDirAdmin()).toBe(true);
      expect(component.isDirManageMembers()).toBe(false);
    });
  });

  describe('guest user', () => {
    it('should not call getUserMembership when user is guest', async () => {
      mockLoginService.isGuest.mockReturnValue(true);
      mockUserService.getUserMembershipAsync.mockClear();

      paramsSubject.next({ id: 'group1' });
      await fixture.whenStable();

      expect(mockUserService.getUserMembershipAsync).not.toHaveBeenCalled();
    });
  });
});
