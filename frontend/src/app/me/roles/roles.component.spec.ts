import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  Category,
  InterestGroup,
  InterestGroupProfile,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RolesComponent } from './roles.component';

const mockUser: User = {
  userId: 'testuser',
  firstname: 'Test',
  lastname: 'User',
  properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
};

const mockMemberships: InterestGroupProfile[] = [
  {
    interestGroup: { name: 'Group1', permissions: {} },
    profile: { name: 'Access' },
  },
];

const mockCategories: Category[] = [{ name: 'Category1' }];

describe('RolesComponent', () => {
  let component: RolesComponent;
  let fixture: ComponentFixture<RolesComponent>;

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue(mockUser),
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockUserService = {
    getUserMembershipAsync: vi.fn().mockResolvedValue(mockMemberships),
    getUserCategoriesAsync: vi.fn().mockResolvedValue(mockCategories),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue('translated'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RolesComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RolesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('loading memberships and categories', () => {
    it('should load memberships and categories', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockUserService.getUserMembershipAsync).toHaveBeenCalledWith({
        userId: 'testuser',
      });
      expect(mockUserService.getUserCategoriesAsync).toHaveBeenCalledWith({
        userId: 'testuser',
      });
      expect(component.memberships()).toEqual(mockMemberships);
      expect(component.categories()).toEqual(mockCategories);
      expect(component.username).toBe('testuser');
    });

    it('should set isAdmin when user is admin', async () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });

      fixture = TestBed.createComponent(RolesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isAdmin).toBe(true);
      expect(component.isCicabcAdmin).toBe(false);
    });

    it('should set isCicabcAdmin when user is circabc admin', async () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
      });

      fixture = TestBed.createComponent(RolesComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.isCicabcAdmin).toBe(true);
    });
  });

  describe('getNameOrTitle', () => {
    it('should return empty string for undefined', () => {
      expect(component.getNameOrTitle(undefined)).toBe('');
    });

    it('should return translated title when title exists', () => {
      const group: InterestGroup = {
        name: 'GroupName',
        title: { en: 'English Title' },
        permissions: {},
      };

      expect(component.getNameOrTitle(group)).toBe('translated');
      expect(mockI18nPipe.transform).toHaveBeenCalledWith({
        en: 'English Title',
      });
    });

    it('should return name when title is empty', () => {
      const group: InterestGroup = {
        name: 'GroupName',
        title: {},
        permissions: {},
      };

      expect(component.getNameOrTitle(group)).toBe('GroupName');
    });

    it('should return name when i18nPipe returns empty string', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const group: InterestGroup = {
        name: 'GroupName',
        title: { en: 'Title' },
        permissions: {},
      };

      expect(component.getNameOrTitle(group)).toBe('GroupName');
    });
  });

  describe('showConfirmation', () => {
    it('should set displayQuitGroup and selectedGroup when group provided', () => {
      const group: InterestGroup = { name: 'TestGroup', permissions: {} };

      component.showConfirmation(group);

      expect(component.displayQuitGroup).toBe(true);
      expect(component.selectedGroup).toBe(group);
    });

    it('should not set displayQuitGroup when group is undefined', () => {
      component.showConfirmation(undefined);

      expect(component.displayQuitGroup).toBe(false);
    });
  });

  describe('refresh', () => {
    it('should reload memberships', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      mockUserService.getUserMembershipAsync.mockClear();

      component.refresh();
      await fixture.whenStable();

      expect(mockUserService.getUserMembershipAsync).toHaveBeenCalledWith({
        userId: 'testuser',
      });
    });
  });

  describe('createUserWizardClosed', () => {
    it('should set showWizard to false', async () => {
      component.showWizard = true;

      await component.createUserWizardClosed({} as never);

      expect(component.showWizard).toBe(false);
    });
  });
});
