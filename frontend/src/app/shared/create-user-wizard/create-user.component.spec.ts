import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  MembersService,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateUserComponent } from './create-user.component';

const mockUserService = {
  postUser: vi.fn().mockReturnValue(of({})),
  postUserAsync: vi.fn().mockResolvedValue({}),
};

const mockMembersService = {
  postMember: vi.fn().mockReturnValue(of({})),
  postMemberAsync: vi.fn().mockResolvedValue({}),
};

const mockProfileService = {
  getProfilesAsync: vi
    .fn()
    .mockResolvedValue([
      { id: 'prof1', name: 'ACCESS' } as Profile,
      { id: 'prof2', name: 'guest' } as Profile,
      { id: 'prof3', name: 'EVERYONE' } as Profile,
    ]),
};

const mockUiMessageService = {
  addSuccessMessage: vi.fn(),
  addErrorMessage: vi.fn(),
};

describe('CreateUserComponent', () => {
  let component: CreateUserComponent;
  let fixture: ComponentFixture<CreateUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateUserComponent],
      providers: [
        provideNativeDateAdapter(),
        { provide: UserService, useValue: mockUserService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(CreateUserComponent, {
        set: {
          imports: [ReactiveFormsModule, TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(CreateUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should build the forms on init', () => {
      expect(component.addUserForm).toBeDefined();
      expect(component.notificationForm).toBeDefined();
    });

    it('should set isOSS to false by default (ent release)', () => {
      expect(component.isOSS).toBe(false);
    });
  });

  describe('buildForm', () => {
    it('should create addUserForm with required controls', () => {
      const controls = component.addUserForm.controls;
      expect(controls['username']).toBeDefined();
      expect(controls['firstname']).toBeDefined();
      expect(controls['lastname']).toBeDefined();
      expect(controls['email']).toBeDefined();
      expect(controls['phone']).toBeDefined();
      expect(controls['password']).toBeDefined();
      expect(controls['passwordVerify']).toBeDefined();
    });

    it('should have expirationDateTime disabled initially', () => {
      expect(
        component.notificationForm.controls['expirationDateTime'].disabled
      ).toBe(true);
    });

    it('should enable expirationDateTime when expiration is set to true', () => {
      component.notificationForm.controls['expiration'].setValue(true);
      expect(
        component.notificationForm.controls['expirationDateTime'].enabled
      ).toBe(true);
    });

    it('should disable expirationDateTime when expiration is set back to false', () => {
      component.notificationForm.controls['expiration'].setValue(true);
      component.notificationForm.controls['expiration'].setValue(false);
      expect(
        component.notificationForm.controls['expirationDateTime'].disabled
      ).toBe(true);
    });
  });

  describe('profiles loading', () => {
    it('should filter out guest and EVERYONE profiles', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.availableProfiles()).toHaveLength(1);
      expect(component.availableProfiles()[0].name).toBe('ACCESS');
    });

    it('should set selectedProfile to first available profile', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.notificationForm.controls['selectedProfile'].value).toBe(
        'prof1'
      );
    });

    it('should do nothing if groupId is undefined', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockProfileService.getProfilesAsync).not.toHaveBeenCalled();
    });
  });

  describe('resetForm', () => {
    it('should reset all form fields to empty', () => {
      component.addUserForm.controls['username'].setValue('testuser');
      component.addUserForm.controls['firstname'].setValue('John');

      component.resetForm();

      expect(component.addUserForm.controls['username'].value).toBe('');
      expect(component.addUserForm.controls['firstname'].value).toBe('');
    });
  });

  describe('cancelWizard', () => {
    it('should hide wizards and reset form', () => {
      component.showCreateWizard.set(true);
      component.showInviteWizard.set(true);

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard();

      expect(component.showCreateWizard()).toBe(false);
      expect(component.showInviteWizard()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({ result: 0 });
    });
  });

  describe('launchInviteWizard', () => {
    it('should switch from create to invite wizard', () => {
      component.showCreateWizard.set(true);
      component.launchInviteWizard();

      expect(component.showCreateWizard()).toBe(false);
      expect(component.showInviteWizard()).toBe(true);
    });
  });

  describe('submitNewUser', () => {
    it('should not submit if form is invalid', async () => {
      await component.submitNewUser();
      expect(mockUserService.postUserAsync).not.toHaveBeenCalled();
    });

    it('should call postUser when form is valid and no groupId', async () => {
      fillValidUserForm(component);

      await component.submitNewUser();

      expect(mockUserService.postUserAsync).toHaveBeenCalled();
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });

    it('should launch invite wizard after creating user when groupId is set', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fillValidUserForm(component);

      await component.submitNewUser();

      expect(component.showInviteWizard()).toBe(true);
    });

    it('should show error message on failure', async () => {
      fillValidUserForm(component);
      mockUserService.postUserAsync.mockRejectedValueOnce({
        status: 500,
        message: 'Server error',
      });

      await component.submitNewUser();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });

    it('should handle 409 conflict error', async () => {
      fillValidUserForm(component);
      mockUserService.postUserAsync.mockRejectedValueOnce({
        status: 409,
        error: { message: 'User already exists' },
      });

      await component.submitNewUser();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });
  });

  describe('submitMembers', () => {
    it('should not submit if no profile is selected', async () => {
      component.notificationForm.controls['selectedProfile'].setValue('');
      await component.submitMembers();
      expect(mockMembersService.postMemberAsync).not.toHaveBeenCalled();
    });

    it('should call postMember when profile is selected and groupId exists', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();
      fillValidUserForm(component);

      await component.submitMembers();

      expect(mockMembersService.postMemberAsync).toHaveBeenCalledWith({
        id: 'group123',
        membershipPostDefinition: expect.objectContaining({
          memberships: expect.any(Array),
        }),
      });
    });

    it('should pass expirationDateTime when expiration is enabled', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();
      fillValidUserForm(component);

      component.notificationForm.controls['expiration'].setValue(true);
      const futureDate = new Date('2027-01-01');
      component.notificationForm.controls['expirationDateTime'].setValue(
        futureDate
      );

      await component.submitMembers();

      expect(mockMembersService.postMemberAsync).toHaveBeenCalledWith({
        id: 'group123',
        membershipPostDefinition: expect.any(Object),
        expirationDate: futureDate.toISOString(),
      });
    });

    it('should show error message on failure', async () => {
      fixture.componentRef.setInput('groupId', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();
      fillValidUserForm(component);

      mockMembersService.postMemberAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      await component.submitMembers();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });
  });

  describe('hasSelectedProfile', () => {
    it('should return false when no profile is selected', () => {
      component.notificationForm.controls['selectedProfile'].setValue('');
      expect(component.hasSelectedProfile()).toBe(false);
    });

    it('should return true when a profile is selected', () => {
      component.notificationForm.controls['selectedProfile'].setValue('prof1');
      expect(component.hasSelectedProfile()).toBe(true);
    });
  });

  describe('accessor properties', () => {
    it('should return form controls via getters', () => {
      expect(component.usernameControl).toBe(
        component.addUserForm.controls['username']
      );
      expect(component.firstnameControl).toBe(
        component.addUserForm.controls['firstname']
      );
      expect(component.lastnameControl).toBe(
        component.addUserForm.controls['lastname']
      );
      expect(component.emailControl).toBe(
        component.addUserForm.controls['email']
      );
      expect(component.phoneControl).toBe(
        component.addUserForm.controls['phone']
      );
      expect(component.faxControl).toBe(component.addUserForm.controls['fax']);
      expect(component.urlAddressControl).toBe(
        component.addUserForm.controls['urlAddress']
      );
      expect(component.postalAddressControl).toBe(
        component.addUserForm.controls['postalAddress']
      );
      expect(component.passwordControl).toBe(
        component.addUserForm.controls['password']
      );
      expect(component.passwordVerifyControl).toBe(
        component.addUserForm.controls['passwordVerify']
      );
      expect(component.expirationDateTimeControl).toBe(
        component.notificationForm.controls['expirationDateTime']
      );
    });
  });

  describe('password verification', () => {
    it('should invalidate passwordVerify when passwords do not match', () => {
      component.addUserForm.controls['password'].setValue('Password1!');
      component.addUserForm.controls['passwordVerify'].setValue('Different1!');
      expect(
        component.addUserForm.controls['passwordVerify'].hasError(
          'invalidPasswordConfirm'
        )
      ).toBe(true);
    });

    it('should validate passwordVerify when passwords match', () => {
      component.addUserForm.controls['password'].setValue('Password1!');
      component.addUserForm.controls['passwordVerify'].setValue('Password1!');
      expect(
        component.addUserForm.controls['passwordVerify'].hasError(
          'invalidPasswordConfirm'
        )
      ).toBe(false);
    });
  });
});

function fillValidUserForm(component: CreateUserComponent): void {
  component.addUserForm.controls['username'].setValue('testuser');
  component.addUserForm.controls['firstname'].setValue('John');
  component.addUserForm.controls['lastname'].setValue('Doe');
  component.addUserForm.controls['email'].setValue('john.doe@ec.europa.eu');
  component.addUserForm.controls['phone'].setValue('+32123456789');
  component.addUserForm.controls['postalAddress'].setValue('Brussels');
  component.addUserForm.controls['password'].setValue('Password1!');
  component.addUserForm.controls['passwordVerify'].setValue('Password1!');
}
