import { TestBed } from '@angular/core/testing';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

describe('EULoginService', () => {
  let service: EULoginService;
  const mockLoginService = { isGuest: vi.fn() };
  let originalHref: string;

  beforeEach(() => {
    originalHref = globalThis.location.href;

    TestBed.configureTestingModule({
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });

    service = TestBed.inject(EULoginService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    // Reset location mock
    Object.defineProperty(globalThis, 'location', {
      value: { href: originalHref },
      writable: true,
    });
  });

  describe('euLogin', () => {
    it('should redirect to euloginUrl when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      Object.defineProperty(globalThis, 'location', {
        value: { href: '' },
        writable: true,
      });

      service.euLogin();

      expect(globalThis.location.href).toBe(environment.euloginUrl);
    });

    it('should not redirect when user is not guest', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      Object.defineProperty(globalThis, 'location', {
        value: { href: '' },
        writable: true,
      });

      service.euLogin();

      expect(globalThis.location.href).toBe('');
    });
  });

  describe('logout', () => {
    it('should redirect to eulogoutUrl', () => {
      Object.defineProperty(globalThis, 'location', {
        value: { href: '' },
        writable: true,
      });

      service.logout();

      expect(globalThis.location.href).toBe(environment.eulogoutUrl);
    });
  });
});
