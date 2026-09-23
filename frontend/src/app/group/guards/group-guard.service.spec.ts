import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateGroup } from './group-guard.service';

describe('canActivateGroup', () => {
  const mockGuardsService = { getGuardGroupAsync: vi.fn() };
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

  function createRoute(id: string | null): ActivatedRouteSnapshot {
    return {
      paramMap: { get: vi.fn().mockReturnValue(id) },
    } as unknown as ActivatedRouteSnapshot;
  }

  async function runGuard(route: ActivatedRouteSnapshot): Promise<boolean> {
    return TestBed.runInInjectionContext(() =>
      canActivateGroup(route, {} as never)
    ) as Promise<boolean>;
  }

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: GuardsService, useValue: mockGuardsService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  it('should return false when groupId is null', async () => {
    expect(await runGuard(createRoute(null))).toBe(false);
  });

  it('should return false when groupId is "0"', async () => {
    expect(await runGuard(createRoute('0'))).toBe(false);
  });

  it('should return true when access is granted', async () => {
    mockGuardsService.getGuardGroupAsync.mockResolvedValue({ granted: true });
    expect(await runGuard(createRoute('123'))).toBe(true);
  });

  it('should navigate to /denied when access is not granted', async () => {
    mockGuardsService.getGuardGroupAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(false);

    expect(await runGuard(createRoute('123'))).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should call mustRedirect when guest is denied', async () => {
    mockGuardsService.getGuardGroupAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(true);

    await runGuard(createRoute('123'));
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should navigate to /no-content on API error', async () => {
    mockGuardsService.getGuardGroupAsync.mockRejectedValue(new Error('fail'));

    expect(await runGuard(createRoute('123'))).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/no-content']);
  });
});
