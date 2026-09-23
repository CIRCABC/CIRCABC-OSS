import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroupService,
  MembersService,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { UrlHelperService } from 'app/core/url-helper.service';
import { SERVER_URL } from 'app/core/variables';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { MembersComponent } from './members.component';

const paramsSubject = new Subject<{ [key: string]: string }>();

const mockRoute = {
  params: paramsSubject.asObservable(),
};

const mockRouter = {
  navigate: vi.fn(),
};

const mockGroupService = {
  getInterestGroupAsync: vi
    .fn()
    .mockResolvedValue({ id: 'g1', name: 'Group1', permissions: {} }),
};

const mockMembersService = {
  getMembersFilterAsync: vi.fn().mockResolvedValue({
    data: [{ user: { userId: 'user1' }, profile: { id: 'p1', name: 'Admin' } }],
    total: 1,
  }),
};

const mockProfileService = {
  getProfilesAsync: vi.fn().mockResolvedValue([
    { id: 'p1', name: 'Admin' },
    { id: 'p2', name: 'EVERYONE' },
    { id: 'p3', name: 'guest' },
  ]),
};

const mockUserService = {
  getUserMembershipAsync: vi.fn().mockResolvedValue([]),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getUser: vi.fn().mockReturnValue({ userId: 'currentUser' }),
  getCurrentUsername: vi.fn().mockReturnValue('currentUser'),
};

const mockUiMessageService = {
  addSuccessMessage: vi.fn(),
  addErrorMessage: vi.fn(),
};

const mockPermEvalService = {
  isDirAdmin: vi.fn().mockReturnValue(true),
  isDirManageMembers: vi.fn().mockReturnValue(true),
};

const mockSaveAsService = {
  saveUrlAs: vi.fn(),
};

const mockI18nPipe = {
  transform: vi.fn().mockReturnValue('translated'),
};

describe('MembersComponent', () => {
  let component: MembersComponent;
  let fixture: ComponentFixture<MembersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MembersComponent],
      providers: [
        provideNativeDateAdapter(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: BASE_PATH, useValue: 'http://localhost' },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
        {
          provide: DownloadUtilService,
          useValue: { getDownloadUrl: vi.fn().mockReturnValue('http://mock') },
        },
        {
          provide: UrlHelperService,
          useValue: { get: vi.fn().mockReturnValue(of('blob:mock')) },
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

    fixture = TestBed.createComponent(MembersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should have initialized the search and export forms', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.exportForm).toBeDefined();
  });

  it('should load members when route params emit', async () => {
    paramsSubject.next({ id: 'g1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: 'g1',
    });
    expect(component.nodeId).toBe('g1');
  });

  it('should filter out EVERYONE and guest profiles', async () => {
    paramsSubject.next({ id: 'g1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.availableProfiles()).toHaveLength(1);
    expect(component.availableProfiles()[0].name).toBe('Admin');
  });

  it('should toggle selected user', () => {
    component.members.set([
      { user: { userId: 'user1' }, profile: { id: 'p1' }, selected: false },
      { user: { userId: 'user2' }, profile: { id: 'p2' }, selected: false },
    ]);
    component.toggleSelectedUser(component.members()[0]);
    expect(component.members()[0].selected).toBe(true);
    expect(component.selectedUsers).toHaveLength(1);

    component.toggleSelectedUser(component.members()[0]);
    expect(component.members()[0].selected).toBe(false);
    expect(component.selectedUsers).toHaveLength(0);
  });

  it('should select all members except current user', () => {
    component.members.set([
      {
        user: { userId: 'currentUser' },
        profile: { id: 'p1' },
        selected: false,
      },
      { user: { userId: 'user2' }, profile: { id: 'p2' }, selected: false },
    ]);
    component.selectAll();
    expect(component.allSelected).toBe(true);
    expect(component.selectedUsers).toHaveLength(1);
    expect(component.selectedUsers[0].user?.userId).toBe('user2');
  });

  it('should deselect all when selectAll is toggled off', () => {
    component.members.set([
      { user: { userId: 'user2' }, profile: { id: 'p2' }, selected: true },
    ]);
    component.allSelected = true;
    component.selectedUsers = [component.members()[0]];

    component.selectAll();
    expect(component.allSelected).toBe(false);
    expect(component.selectedUsers).toHaveLength(0);
    expect(component.members()[0].selected).toBe(false);
  });

  it('should return true for isDirAdmin when permission evaluator says so', async () => {
    paramsSubject.next({ id: 'g1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.currentGroup()).toBeDefined();
    expect(component.isDirAdmin()).toBe(true);
  });

  it('should return false for isDirAdmin when currentGroup is undefined', () => {
    expect(component.currentGroup()).toBeUndefined();
    expect(component.isDirAdmin()).toBe(false);
  });

  it('should set showUninviteDialog and selectedUser on uninviteUser', () => {
    const user = { userId: 'user1' };
    component.uninviteUser(user);
    expect(component.showUninviteDialog).toBe(true);
    expect(component.selectedUser).toBe(user);
  });

  it('should not set showUninviteDialog when user is undefined', () => {
    component.uninviteUser(undefined);
    expect(component.showUninviteDialog).toBe(false);
  });

  it('should navigate to profiles on goToProfiles', () => {
    component.goToProfiles();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['../profiles'], {
      relativeTo: mockRoute,
    });
  });

  it('should call saveUrlAs on export', async () => {
    paramsSubject.next({ id: 'g1' });
    fixture.detectChanges();
    await fixture.whenStable();

    component.exportForm.patchValue({ export: { code: 'csv', name: 'CSV' } });
    component.searchForm.patchValue({
      searchProfile: 'all',
      firstName: '',
      lastName: '',
      email: '',
    });
    component.export();
    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalled();
    expect(component.exporting).toBe(false);
  });

  it('should identify connected user', () => {
    expect(component.isConnectedUser('currentUser')).toBe(true);
    expect(component.isConnectedUser('otherUser')).toBe(false);
    expect(component.isConnectedUser(undefined)).toBe(false);
  });

  it('should return profile name from getProfileNameOrTitle', () => {
    expect(component.getProfileNameOrTitle(undefined)).toBe('');
    expect(component.getProfileNameOrTitle({ name: 'Admin' })).toBe('Admin');
    expect(
      component.getProfileNameOrTitle({
        name: 'Admin',
        title: { en: 'Administrator' },
      })
    ).toBe('translated');
  });

  it('should handle inviteWizardClosed with success', () => {
    component.inviteWizardClosed({
      result: ActionResult.SUCCEED,
      type: ActionType.ADD_MEMBERSHIPS,
    });
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    expect(component.showWizard).toBe(false);
  });

  it('should handle inviteWizardClosed with failure', () => {
    component.inviteWizardClosed({
      result: ActionResult.FAILED,
      type: ActionType.ADD_MEMBERSHIPS,
    });
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    expect(component.showWizard).toBe(false);
  });

  it('should handle changeModalClosed with cancel', () => {
    component.showChangeDialog = true;
    component.changeModalClosed({
      result: ActionResult.CANCELED,
      type: ActionType.CHANGE_PROFILE,
    });
    expect(component.showChangeDialog).toBe(false);
  });

  it('should track member by userId and profile id', () => {
    const result = component.trackMember(0, {
      user: { userId: 'u1' },
      profile: { id: 'p1' },
    });
    expect(result).toBe('u1-p1');
  });

  it('should reset search and call searchUsers', () => {
    component.searchForm.patchValue({
      firstName: 'John',
      lastName: 'Doe',
      email: 'j@d.com',
      searchProfile: 'p1',
    });
    component.resetSearch();
    expect(component.searchForm.value.firstName).toBe('');
    expect(component.searchForm.value.lastName).toBe('');
    expect(component.searchForm.value.email).toBe('');
    expect(component.searchForm.value.searchProfile).toBe('all');
  });

  it('should prepare multiple deletion', () => {
    component.members.set([
      { user: { userId: 'user1' }, profile: { id: 'p1' }, selected: true },
      { user: { userId: 'user2' }, profile: { id: 'p2' }, selected: false },
    ]);
    component.prepareMultipleDeletion();
    expect(component.showUninviteMultipleDialog).toBe(true);
    expect(component.selectedUsers).toHaveLength(1);
  });

  it('should prepare change expiration for a single member', () => {
    const member = {
      user: { userId: 'user1' },
      profile: { id: 'p1' },
      selected: false,
    };
    component.prepareChangeExpiration(member);
    expect(component.showExpirationDialog).toBe(true);
    expect(component.selectedExpiredUsers).toHaveLength(1);
  });
});
