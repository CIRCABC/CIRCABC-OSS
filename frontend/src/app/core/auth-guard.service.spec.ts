import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateAuth } from './auth-guard.service';

describe('canActivateAuth', () => {
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: Router, useValue: mockRouter },
      ],
    });
    vi.clearAllMocks();
  });

  it('should return true when user is not a guest', async () => {
    mockLoginService.isGuest.mockReturnValue(false);

    const result = await TestBed.runInInjectionContext(() =>
      canActivateAuth({} as never, {} as never)
    );

    expect(result).toBe(true);
    expect(mockRedirectionService.mustRedirect).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should store redirect, navigate to /denied, and return false when user is a guest', async () => {
    mockLoginService.isGuest.mockReturnValue(true);

    const result = await TestBed.runInInjectionContext(() =>
      canActivateAuth({} as never, {} as never)
    );

    expect(result).toBe(false);
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });
});
