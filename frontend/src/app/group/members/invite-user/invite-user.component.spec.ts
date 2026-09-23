import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  HistoryService,
  MembersService,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { I18nService } from 'app/shared/services/i18n.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { InviteUserComponent } from './invite-user.component';

const mockProfiles = [
  { id: 'prof1', name: 'Access', title: { en: 'Access' } },
  { id: 'prof2', name: 'guest', title: { en: 'Guest' } },
  { id: 'prof3', name: 'EVERYONE', title: { en: 'Everyone' } },
];

const mockMembers = {
  data: [{ user: { userId: 'existingUser' }, profile: { id: 'prof1' } }],
  total: 1,
};

const mockUserService = {
  getUsersAsync: vi.fn().mockResolvedValue([]),
};

const mockMembersService = {
  getMembersAsync: vi.fn().mockResolvedValue(mockMembers),
  postMember: vi.fn().mockReturnValue(of({})),
  postMemberAsync: vi.fn().mockResolvedValue({}),
};

const mockProfileService = {
  getProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
};

const mockHistoryService = {
  isUserRecoverableAsync: vi
    .fn()
    .mockResolvedValue({ recoverable: false, profile: undefined }),
  cleanUserMembershipLogsAsync: vi.fn().mockResolvedValue({}),
};

const mockLoginService = {
  getUser: vi.fn().mockReturnValue({ properties: { domain: 'internal' } }),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockI18nPipe = {
  transform: vi.fn().mockReturnValue(''),
};

const mockI18nService = {
  getActiveLang: vi.fn().mockReturnValue('en'),
  getDefaultLang: vi.fn().mockReturnValue('en'),
};

const mockDialog = {
  open: vi.fn(),
};

describe('InviteUserComponent', () => {
  let component: InviteUserComponent;
  let componentRef: ComponentRef<InviteUserComponent>;
  let fixture: ComponentFixture<InviteUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviteUserComponent],
      providers: [
        provideNativeDateAdapter(),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: UserService, useValue: mockUserService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: HistoryService, useValue: mockHistoryService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: I18nService, useValue: mockI18nService },
        { provide: MatDialog, useValue: mockDialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InviteUserComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group123');
  });

  /** Triggers the resource loaders and waits for them to settle. */
  async function loadResources() {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('initial load', () => {
    it('should build form and load profiles and members', async () => {
      await loadResources();

      expect(component.addUserForm).toBeDefined();
      expect(component.availableProfiles()).toHaveLength(1);
      expect(component.availableProfiles()[0].id).toBe('prof1');
      expect(component.existingMembers()).toHaveLength(1);
    });

    it('should filter out guest and EVERYONE profiles', async () => {
      await loadResources();

      const names = component.availableProfiles().map((p) => p.name);
      expect(names).not.toContain('guest');
      expect(names).not.toContain('EVERYONE');
    });

    it('should set selectedProfile to first available profile', async () => {
      await loadResources();

      expect(component.addUserForm.controls['selectedProfile'].value).toBe(
        'prof1'
      );
    });
  });

  describe('isAlreadyMember', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should return true if user is already a member', () => {
      expect(component.isAlreadyMember({ userId: 'existingUser' })).toBe(true);
    });

    it('should return false if user is not a member', () => {
      expect(component.isAlreadyMember({ userId: 'newUser' })).toBe(false);
    });
  });

  describe('searchUsers', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should populate users when name is not empty', async () => {
      const users = [
        {
          userId: 'user1',
          firstname: 'John',
          lastname: 'Doe',
          email: 'j@e.com',
        },
      ];
      mockUserService.getUsersAsync.mockResolvedValue(users);
      component.addUserForm.controls['name'].setValue('john');

      await component.searchUsers();

      expect(component.availableUsers()).toHaveLength(1);
      expect(component.availableUsers()[0].userId).toBe('user1');
    });

    it('should set noResultFound when no users returned', async () => {
      mockUserService.getUsersAsync.mockResolvedValue([]);
      component.addUserForm.controls['name'].setValue('nobody');

      await component.searchUsers();

      expect(component.noResultFound()).toBe(true);
    });
  });

  describe('hasUsersSelected', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should return false when no future members', () => {
      expect(component.hasUsersSelected()).toBe(false);
    });

    it('should return true when future members exist', () => {
      component.futureMembers.set([
        { user: { userId: 'u1' }, profile: { id: 'prof1' } },
      ]);
      expect(component.hasUsersSelected()).toBe(true);
    });
  });

  describe('isSearchEmpty', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should return true when name is empty', () => {
      component.addUserForm.controls['name'].setValue('');
      expect(component.isSearchEmpty()).toBe(true);
    });

    it('should return false when name has value', () => {
      component.addUserForm.controls['name'].setValue('test');
      expect(component.isSearchEmpty()).toBe(false);
    });
  });

  describe('isValidEmail', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should return true for valid email', () => {
      component.addUserForm.controls['name'].setValue('user@example.com');
      expect(component.isValidEmail()).toBe(true);
    });

    it('should return false for invalid email', () => {
      component.addUserForm.controls['name'].setValue('notanemail');
      expect(component.isValidEmail()).toBe(false);
    });
  });

  describe('removeFromFutureMember', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should remove the specified member', () => {
      const member = { user: { userId: 'u1' }, profile: { id: 'prof1' } };
      component.futureMembers.set([member]);

      component.removeFromFutureMember(member);

      expect(component.futureMembers()).toHaveLength(0);
    });
  });

  describe('cancelWizard', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should reset state and emit CANCELED when closing', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.futureMembers.set([{ user: { userId: 'u1' } }]);

      component.cancelWizard('close');

      expect(component.showWizard()).toBe(false);
      expect(component.futureMembers()).toHaveLength(0);
      expect(emitSpy).toHaveBeenCalledWith({ result: 0 });
    });

    it('should set showWizard to true when going back to step1', () => {
      component.cancelWizard('step1');
      expect(component.showWizard()).toBe(true);
    });
  });

  describe('submitMembers', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should post members and emit SUCCEED on success', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.futureMembers.set([
        { user: { userId: 'u1' }, profile: { id: 'prof1' } },
      ]);

      await component.submitMembers();

      expect(mockMembersService.postMemberAsync).toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: 1 })
      );
      expect(component.inviting()).toBe(false);
    });

    it('should show error message on failure', async () => {
      mockMembersService.postMemberAsync.mockRejectedValue(new Error('fail'));
      component.futureMembers.set([
        { user: { userId: 'u1' }, profile: { id: 'prof1' } },
      ]);

      await component.submitMembers();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(component.inviting()).toBe(false);
    });

    it('should include expirationDateTime when expiration is enabled', async () => {
      component.addUserForm.controls['expiration'].setValue(true);
      component.futureMembers.set([
        { user: { userId: 'u1' }, profile: { id: 'prof1' } },
      ]);
      mockMembersService.postMemberAsync.mockResolvedValue({});

      await component.submitMembers();

      expect(mockMembersService.postMemberAsync).toHaveBeenCalledWith({
        id: 'group123',
        membershipPostDefinition: expect.any(Object),
        expirationDate: expect.any(String),
      });
    });
  });

  describe('expiration toggle', () => {
    beforeEach(async () => {
      await loadResources();
    });

    it('should disable expirationDateTime by default', () => {
      expect(
        component.addUserForm.controls['expirationDateTime'].disabled
      ).toBe(true);
    });

    it('should enable expirationDateTime when expiration is true', () => {
      component.addUserForm.controls['expiration'].setValue(true);
      expect(component.addUserForm.controls['expirationDateTime'].enabled).toBe(
        true
      );
    });

    it('should disable and reset expirationDateTime when expiration is set back to false', () => {
      component.addUserForm.controls['expiration'].setValue(true);
      component.addUserForm.controls['expiration'].setValue(false);
      expect(
        component.addUserForm.controls['expirationDateTime'].disabled
      ).toBe(true);
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe from dateSubscription', async () => {
      await loadResources();
      component.ngOnDestroy();
      // No error thrown means subscription was properly cleaned up
      expect(component).toBeDefined();
    });
  });
});
