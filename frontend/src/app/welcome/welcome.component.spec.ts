import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { CookieService } from 'ngx-cookie-service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { WelcomeComponent } from './welcome.component';

describe('WelcomeComponent', () => {
  let component: WelcomeComponent;
  let mockCookieService: {
    get: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    loadUser: ReturnType<typeof vi.fn>;
  };
  let mockEULoginService: { euLogin: ReturnType<typeof vi.fn> };
  let mockRedirectionService: { redirect: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockCookieService = { get: vi.fn().mockReturnValue(''), delete: vi.fn() };
    mockLoginService = {
      isGuest: vi.fn().mockReturnValue(true),
      loadUser: vi.fn().mockResolvedValue(true),
    };
    mockEULoginService = { euLogin: vi.fn() };
    mockRedirectionService = { redirect: vi.fn().mockResolvedValue(undefined) };
    mockRouter = { navigate: vi.fn().mockResolvedValue(true) };

    await TestBed.configureTestingModule({
      imports: [WelcomeComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: EULoginService, useValue: mockEULoginService },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: Router, useValue: mockRouter },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(WelcomeComponent, {
        set: {
          imports: [TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
          providers: [{ provide: CookieService, useValue: mockCookieService }],
        },
      })
      .compileComponents();

    const fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should redirect when user is not guest and no credentials', async () => {
      mockLoginService.isGuest.mockReturnValue(false);
      await component.initializeSession();
      expect(mockRedirectionService.redirect).toHaveBeenCalled();
    });

    it('should login with credentials from cookies', async () => {
      mockCookieService.get.mockImplementation((key: string) => {
        const map: Record<string, string> = {
          username: 'user1',
          ticket: 'ticket1',
          route: '',
        };
        return map[key] ?? '';
      });
      await component.initializeSession();
      expect(mockLoginService.loadUser).toHaveBeenCalledWith(
        'user1',
        'ticket1'
      );
      expect(mockRedirectionService.redirect).toHaveBeenCalled();
    });

    it('should navigate to mapped route after login', async () => {
      mockCookieService.get.mockImplementation((key: string) => {
        const map: Record<string, string> = {
          username: 'user1',
          ticket: 'ticket1',
          route: 'calendar',
        };
        return map[key] ?? '';
      });
      await component.initializeSession();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/me/calendar']);
    });

    it('should navigate to /me for unknown route after login', async () => {
      mockCookieService.get.mockImplementation((key: string) => {
        const map: Record<string, string> = {
          username: 'user1',
          ticket: 'ticket1',
          route: 'unknown',
        };
        return map[key] ?? '';
      });
      await component.initializeSession();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/me']);
    });

    it('should reset waitingAfterLogin when loadUser fails', async () => {
      mockCookieService.get.mockImplementation((key: string) => {
        const map: Record<string, string> = {
          username: 'user1',
          ticket: 'ticket1',
          route: '',
        };
        return map[key] ?? '';
      });
      mockLoginService.loadUser.mockResolvedValue(false);
      await component.initializeSession();
      expect(component.waitingAfterLogin()).toBe(false);
    });

    it('should not redirect when user is guest and no credentials', async () => {
      await component.initializeSession();
      expect(mockRedirectionService.redirect).not.toHaveBeenCalled();
    });

    it('should delete cookies after reading them', async () => {
      mockCookieService.get.mockImplementation((key: string) => {
        const map: Record<string, string> = {
          username: 'user1',
          ticket: 'ticket1',
          route: '',
        };
        return map[key] ?? '';
      });
      await component.initializeSession();
      expect(mockCookieService.delete).toHaveBeenCalledWith('username', '/');
      expect(mockCookieService.delete).toHaveBeenCalledWith('ticket', '/');
      expect(mockCookieService.delete).toHaveBeenCalledWith('route', '/');
    });
  });

  describe('euLogin', () => {
    it('should call euLoginService.euLogin when user is guest', async () => {
      await component.euLogin();
      expect(mockEULoginService.euLogin).toHaveBeenCalled();
    });

    it('should navigate to /me when user is not guest', async () => {
      mockLoginService.isGuest.mockReturnValue(false);
      await component.euLogin();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/me']);
    });
  });

  describe('useEULogin', () => {
    it('should return true', () => {
      expect(component.useEULogin).toBe(true);
    });
  });

  describe('refreshUILang', () => {
    it('should set active language', () => {
      const translocoService = TestBed.inject(TranslocoService);
      const spy = vi.spyOn(translocoService, 'setActiveLang');
      component.refreshUILang('fr');
      expect(spy).toHaveBeenCalledWith('fr');
    });
  });

  describe('currentLang', () => {
    it('should return the active language', () => {
      const translocoService = TestBed.inject(TranslocoService);
      vi.spyOn(translocoService, 'getActiveLang').mockReturnValue('de');
      expect(component.currentLang).toBe('de');
    });
  });
});
