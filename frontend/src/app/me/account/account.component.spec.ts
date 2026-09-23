import { Location } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import {
  AppMessageService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AccountComponent } from './account.component';

const mockUser: User = {
  userId: 'user1',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john@example.com',
  phone: '123456',
  uiLang: 'en',
  visibility: true,
  avatar: 'avatar.png',
  properties: {
    title: 'Mr',
    description: 'desc',
    organisation: 'EC',
    signature: 'sig',
    fax: '789',
    postalAddress: '123 Street',
    urlAddress: 'http://example.com',
    globalNotificationEnabled: 'true',
  },
};

describe('AccountComponent', () => {
  const mockUserService = {
    getUser: vi.fn().mockReturnValue(of(mockUser)),
    getUserAsync: vi.fn().mockResolvedValue(mockUser),
    getUserFromDBAsync: vi.fn().mockResolvedValue(mockUser),
    putUser: vi.fn().mockReturnValue(of({})),
    putUserAsync: vi.fn().mockResolvedValue(undefined),
    deleteAvatar: vi.fn().mockReturnValue(of({})),
    deleteAvatarAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = { getUser: vi.fn().mockReturnValue(mockUser) };

  const mockAppMessageService = {
    getDistributionEmailSubscriptionAsync: vi
      .fn()
      .mockResolvedValue({ id: 1, emailAddress: 'john@example.com' }),
    addDistributionEmails: vi.fn().mockReturnValue(of({})),
    addDistributionEmailsAsync: vi.fn().mockResolvedValue(undefined),
    deleteDistributionEmails: vi.fn().mockReturnValue(of({})),
    deleteDistributionEmailsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
    addSuccessMessage: vi.fn(),
  };

  const mockAnalyticsService = {
    getAgreeWithCookies: vi.fn().mockReturnValue(false),
    init: vi.fn(),
  };

  const mockLocation = { back: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUserService.getUser.mockReturnValue(of(mockUser));
    mockLoginService.getUser.mockReturnValue(mockUser);
    mockAppMessageService.getDistributionEmailSubscriptionAsync.mockResolvedValue(
      { id: 1, emailAddress: 'john@example.com' }
    );
    mockAnalyticsService.getAgreeWithCookies.mockReturnValue(false);

    TestBed.configureTestingModule({
      imports: [AccountComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: AppMessageService, useValue: mockAppMessageService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: Location, useValue: mockLocation },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(AccountComponent, {
      set: {
        imports: [TranslocoModule],
        // Unit tests exercise component logic, not the full DOM tree (which
        // pulls in unrelated child-component providers, e.g. DownloadPipe's
        // `serverURL` token). Render a minimal template that still provides
        // the required `nameInputFieldElement` view-child ref.
        template: '<input #nameInputFieldElement />',
        schemas: [NO_ERRORS_SCHEMA],
      },
    });
  });

  async function createComponent(): Promise<AccountComponent> {
    const fixture: ComponentFixture<AccountComponent> =
      TestBed.createComponent(AccountComponent);
    const comp = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    return comp;
  }

  describe('loading user details', () => {
    it('should load user details and set ready to true', async () => {
      const comp = await createComponent();
      expect(comp.ready()).toBe(true);
      expect(comp.user()).toEqual(mockUser);
    });

    it('should fill form with user data', async () => {
      const comp = await createComponent();
      expect(comp.updateUserForm.controls['firstname'].value).toBe('John');
      expect(comp.updateUserForm.controls['lastname'].value).toBe('Doe');
      expect(comp.updateUserForm.controls['email'].value).toBe(
        'john@example.com'
      );
      expect(comp.updateUserForm.controls['phone'].value).toBe('123456');
      expect(comp.updateUserForm.controls['uiLanguage'].value).toBe('en');
    });

    it('should set globalDistributionEnabled to true when distributionMail has id', async () => {
      const comp = await createComponent();
      expect(
        comp.updateUserForm.controls['globalDistributionEnabled'].value
      ).toBe(true);
    });

    it('should set globalDistributionEnabled to false when no distributionMail id', async () => {
      mockAppMessageService.getDistributionEmailSubscriptionAsync.mockResolvedValue(
        {}
      );
      const comp = await createComponent();
      expect(
        comp.updateUserForm.controls['globalDistributionEnabled'].value
      ).toBe(false);
    });
  });

  describe('cutDate', () => {
    it('should return first 10 characters of a date string', async () => {
      const comp = await createComponent();
      expect(comp.cutDate('2023-01-15T10:30:00')).toBe('2023-01-15');
    });

    it('should return empty string for undefined', async () => {
      const comp = await createComponent();
      expect(comp.cutDate(undefined as unknown as string)).toBe('');
    });
  });

  describe('goBack', () => {
    it('should call location.back()', async () => {
      const comp = await createComponent();
      comp.goBack();
      expect(mockLocation.back).toHaveBeenCalled();
    });
  });

  describe('changeAvatar', () => {
    it('should set launchChangeAvatar to true', async () => {
      const comp = await createComponent();
      comp.changeAvatar();
      expect(comp.launchChangeAvatar).toBe(true);
    });
  });

  describe('changeAvatarClosed', () => {
    it('should set launchChangeAvatar to false', async () => {
      const comp = await createComponent();
      comp.launchChangeAvatar = true;
      comp.changeAvatarClosed();
      expect(comp.launchChangeAvatar).toBe(false);
    });
  });

  describe('refreshFromCentralDB', () => {
    it('should call getUserFromDB and update user', async () => {
      const comp = await createComponent();
      await comp.refreshFromCentralDB();
      expect(mockUserService.getUserFromDBAsync).toHaveBeenCalledWith({
        userId: 'user1',
      });
      expect(comp.processing()).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should reload user data from service', async () => {
      const comp = await createComponent();
      mockUserService.getUserAsync.mockClear();
      await comp.cancel();
      expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
        userId: 'user1',
      });
    });
  });

  describe('update', () => {
    it('should call putUser with updated user data', async () => {
      const comp = await createComponent();
      comp.updateUserForm.controls['firstname'].setValue('Jane');
      await comp.update();
      expect(mockUserService.putUserAsync).toHaveBeenCalledWith({
        userId: 'user1',
        user: expect.objectContaining({ firstname: 'Jane' }),
      });
      expect(comp.processing()).toBe(false);
    });

    it('should show success message after update', async () => {
      const comp = await createComponent();
      const transloco = TestBed.inject(TranslocoService);
      vi.spyOn(transloco, 'translate').mockReturnValue('Success');
      await comp.update();
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalledWith(
        'Success',
        true
      );
    });

    it('should throw error when user properties are undefined', async () => {
      const comp = await createComponent();
      comp.user.set({ userId: 'user1' } as User);
      await expect(comp.update()).rejects.toThrow('"user" is undefined.');
    });
  });

  describe('emailControl', () => {
    it('should return the email form control', async () => {
      const comp = await createComponent();
      expect(comp.emailControl).toBe(comp.updateUserForm.controls['email']);
    });
  });

  describe('urlAddressControl', () => {
    it('should return the urlAddress form control', async () => {
      const comp = await createComponent();
      expect(comp.urlAddressControl).toBe(
        comp.updateUserForm.controls['urlAddress']
      );
    });
  });
});
