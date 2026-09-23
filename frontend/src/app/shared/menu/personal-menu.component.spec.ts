import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UserCacheService } from 'app/core/user-cache.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { PersonalMenuComponent } from './personal-menu.component';

@Pipe({ name: 'cbcDownload' })
class MockDownloadPipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Pipe({ name: 'cbcSecure' })
class MockSecurePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

const mockUser: User = {
  userId: 'testuser',
  firstname: 'Test',
  lastname: 'User',
  email: 'test@ec.europa.eu',
  properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
};

describe('PersonalMenuComponent', () => {
  let component: PersonalMenuComponent;
  let fixture: ComponentFixture<PersonalMenuComponent>;

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(true),
    getCurrentUsername: vi.fn().mockReturnValue('guest'),
    getUser: vi.fn().mockReturnValue(mockUser),
  };

  const mockUserCacheService = {
    getUser: vi.fn().mockResolvedValue(mockUser),
  };

  const mockEULoginService = {
    euLogin: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonalMenuComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserCacheService, useValue: mockUserCacheService },
        { provide: EULoginService, useValue: mockEULoginService },
      ],
    })
      .overrideComponent(PersonalMenuComponent, {
        set: {
          imports: [TranslocoModule, MockDownloadPipe, MockSecurePipe],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(PersonalMenuComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should not call setUser when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      fixture.detectChanges();
      expect(mockUserCacheService.getUser).not.toHaveBeenCalled();
    });

    it('should call setUser when user is not guest', async () => {
      mockLoginService.isGuest.mockReturnValue(false);
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockUserCacheService.getUser).toHaveBeenCalledWith('testuser');
    });
  });

  describe('isGuest', () => {
    it('should return true when login service says guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });

    it('should return false when login service says not guest', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      expect(component.isGuest()).toBe(false);
    });
  });

  describe('isUser', () => {
    it('should return false when guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isUser()).toBe(false);
    });

    it('should return false when not guest but user is undefined', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      component.user.set(undefined);
      expect(component.isUser()).toBe(false);
    });

    it('should return true when not guest and user is defined', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      component.user.set(mockUser);
      expect(component.isUser()).toBe(true);
    });
  });

  describe('userName', () => {
    it('should return current username from login service', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('admin');
      expect(component.userName()).toBe('admin');
    });
  });

  describe('isAppAdmin', () => {
    it('should return false when properties is null', () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        properties: null,
      });
      expect(component.isAppAdmin()).toBe(false);
    });

    it('should return false when userId is empty', () => {
      mockLoginService.getUser.mockReturnValue({ ...mockUser, userId: '' });
      expect(component.isAppAdmin()).toBe(false);
    });

    it('should return false when userId is guest', () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        userId: 'guest',
      });
      expect(component.isAppAdmin()).toBe(false);
    });

    it('should return true when isAdmin is true', () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });
      expect(component.isAppAdmin()).toBe(true);
    });

    it('should return true when isCircabcAdmin is true', () => {
      mockLoginService.getUser.mockReturnValue({
        ...mockUser,
        properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
      });
      expect(component.isAppAdmin()).toBe(true);
    });

    it('should return false when neither admin flag is true', () => {
      mockLoginService.getUser.mockReturnValue(mockUser);
      expect(component.isAppAdmin()).toBe(false);
    });
  });

  describe('euLogin', () => {
    it('should call euLoginService.euLogin', () => {
      component.euLogin();
      expect(mockEULoginService.euLogin).toHaveBeenCalled();
    });
  });
});
