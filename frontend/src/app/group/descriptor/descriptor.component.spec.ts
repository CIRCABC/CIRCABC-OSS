import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { EULoginService } from 'app/core/eulogin.service';

import { type InterestGroup, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { RedirectionService } from 'app/core/redirection.service';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DescriptorComponent } from './descriptor.component';

describe('DescriptorComponent', () => {
  let component: DescriptorComponent;
  let componentRef: ComponentRef<DescriptorComponent>;
  let fixture: ComponentFixture<DescriptorComponent>;
  let translocoService: TranslocoService;

  const mockGroup: InterestGroup = {
    id: 'group-1',
    name: 'Test Group',
    permissions: {},
    description: { en: 'English desc', fr: 'French desc' },
    allowApply: true,
    logoUrl: 'ref123/logo.png',
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({ userId: 'user1' }),
  };

  const mockUserService = {
    getUserMembershipAsync: vi.fn().mockResolvedValue([]),
  };

  const mockRedirectionService = {
    mustRedirect: vi.fn(),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
  };

  const mockEuLoginService = {
    euLogin: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getUser.mockReturnValue({ userId: 'user1' });
    mockUserService.getUserMembershipAsync.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [DescriptorComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en', 'fr'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: EULoginService, useValue: mockEuLoginService },
      ],
    })
      .overrideComponent(DescriptorComponent, {
        set: {
          template: '',
          imports: [TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DescriptorComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    translocoService = TestBed.inject(TranslocoService);
  });

  it('should create', () => {
    componentRef.setInput('group', mockGroup);
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('membership resolution', () => {
    it('should redirect when user is guest', async () => {
      mockLoginService.isGuest.mockReturnValue(true);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    });

    it('should set alreadyMember to true when user is member of the group', async () => {
      mockUserService.getUserMembershipAsync.mockResolvedValue([
        { interestGroup: { id: 'group-1', name: 'Test', permissions: {} } },
      ]);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.alreadyMember()).toBe(true);
    });

    it('should leave alreadyMember false when user is not member', async () => {
      mockUserService.getUserMembershipAsync.mockResolvedValue([
        {
          interestGroup: {
            id: 'other-group',
            name: 'Other',
            permissions: {},
          },
        },
      ]);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.alreadyMember()).toBe(false);
    });

    it('should use "guest" as userId when userId is undefined', async () => {
      mockLoginService.getUser.mockReturnValue({ userId: undefined });
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockUserService.getUserMembershipAsync).toHaveBeenCalledWith({
        userId: 'guest',
      });
    });
  });

  describe('getLang', () => {
    it('should return active lang when description contains it', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });

    it('should return default lang when description does not contain active lang', () => {
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('de');
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });

    it('should return default lang when description is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, description: undefined });
      fixture.detectChanges();
      expect(component.getLang()).toBe('en');
    });
  });

  describe('getLogoRef', () => {
    it('should return the first part of logoUrl', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLogoRef()).toBe('ref123');
    });

    it('should return empty string when logoUrl is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, logoUrl: undefined });
      fixture.detectChanges();
      expect(component.getLogoRef()).toBe('');
    });
  });

  describe('getLogoName', () => {
    it('should return the second part of logoUrl', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.getLogoName()).toBe('logo.png');
    });
  });

  describe('isJoinEnabled', () => {
    it('should return true when allowApply is true, not guest, and not already member', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isJoinEnabled()).toBe(true);
    });

    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isJoinEnabled()).toBe(false);
    });

    it('should return false when allowApply is false', () => {
      componentRef.setInput('group', { ...mockGroup, allowApply: false });
      fixture.detectChanges();
      expect(component.isJoinEnabled()).toBe(false);
    });

    it('should return false when already a member', async () => {
      mockUserService.getUserMembershipAsync.mockResolvedValue([
        { interestGroup: { id: 'group-1', name: 'Test', permissions: {} } },
      ]);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.isJoinEnabled()).toBe(false);
    });
  });

  describe('isJoinEnabledGuest', () => {
    it('should return true when allowApply is true and user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isJoinEnabledGuest()).toBe(true);
    });

    it('should return false when user is not guest', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isJoinEnabledGuest()).toBe(false);
    });
  });

  describe('isContactLeaderAvailable', () => {
    it('should return true when user is not guest', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isContactLeaderAvailable()).toBe(true);
    });

    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isContactLeaderAvailable()).toBe(false);
    });
  });

  describe('onRequestCanceled', () => {
    it('should hide application modal', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      component.showApplicationModal = true;
      component.onRequestCanceled({} as ActionEmitterResult);
      expect(component.showApplicationModal).toBe(false);
    });
  });

  describe('onRequestFinished', () => {
    it('should hide modal and show success message on successful membership application', async () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      component.showApplicationModal = true;

      await component.onRequestFinished({
        result: ActionResult.SUCCEED,
        type: ActionType.APPLY_FOR_MEMBERSHIP,
      });

      expect(component.showApplicationModal).toBe(false);
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });

    it('should not hide modal when result is not SUCCEED', async () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      component.showApplicationModal = true;

      await component.onRequestFinished({
        result: ActionResult.FAILED,
        type: ActionType.APPLY_FOR_MEMBERSHIP,
      });

      expect(component.showApplicationModal).toBe(true);
    });
  });

  describe('hasDescription', () => {
    it('should return true when description has value for lang', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasDescription()).toBe(true);
    });

    it('should return false when description is undefined', () => {
      componentRef.setInput('group', { ...mockGroup, description: undefined });
      fixture.detectChanges();
      expect(component.hasDescription()).toBe(false);
    });

    it('should return false when description has empty value for lang', () => {
      componentRef.setInput('group', { ...mockGroup, description: { en: '' } });
      fixture.detectChanges();
      expect(component.hasDescription()).toBe(false);
    });
  });

  describe('hasMLValue', () => {
    it('should return true when obj has non-empty value for active lang', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ en: 'value' })).toBe(true);
    });

    it('should return false when obj has empty value', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ en: '' })).toBe(false);
    });

    it('should return false when lang key is missing', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.hasMLValue({ de: 'value' })).toBe(false);
    });
  });

  describe('isGuest', () => {
    it('should delegate to loginService.isGuest', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.isGuest()).toBe(false);
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });
  });

  describe('useEULogin', () => {
    it('should return based on circabcRelease value', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      expect(component.useEULogin).toBe(environment.circabcRelease !== 'oss');
    });
  });

  describe('leaderContactRefresh', () => {
    it('should hide contact leaders modal', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      component.showContactLeadersModal = true;
      component.leaderContactRefresh({} as ActionEmitterResult);
      expect(component.showContactLeadersModal).toBe(false);
    });
  });

  describe('euLogin', () => {
    it('should call euLoginService.euLogin', () => {
      componentRef.setInput('group', mockGroup);
      fixture.detectChanges();
      component.euLogin();
      expect(mockEuLoginService.euLogin).toHaveBeenCalled();
    });
  });
});
