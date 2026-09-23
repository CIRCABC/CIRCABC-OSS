import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateAdmin } from './admin-guard.service';

describe('canActivateAdmin', () => {
  const mockGuardsService = { getGuardAdminAsync: vi.fn() };
  const mockRouter = { navigate: vi.fn() };
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };

  function createRoute(params: Record<string, string>): ActivatedRouteSnapshot {
    return {
      paramMap: {
        has: (key: string) => key in params,
        get: (key: string) => params[key] ?? null,
      },
    } as unknown as ActivatedRouteSnapshot;
  }

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: GuardsService, useValue: mockGuardsService },
        { provide: Router, useValue: mockRouter },
        { provide: LoginService, useValue: mockLoginService },
        { provide: RedirectionService, useValue: mockRedirectionService },
      ],
    });
  });

  it('should return false when route has no id param', async () => {
    const route = createRoute({});
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return false when nodeId is 0', async () => {
    const route = createRoute({ id: '0' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return true when guard grants access', async () => {
    mockGuardsService.getGuardAdminAsync.mockResolvedValue({ granted: true });
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(true);
    expect(mockGuardsService.getGuardAdminAsync).toHaveBeenCalledWith({
      id: 'abc123',
    });
  });

  it('should use nodeId over id when both are present', async () => {
    mockGuardsService.getGuardAdminAsync.mockResolvedValue({ granted: true });
    const route = createRoute({ id: 'groupId', nodeId: 'nodeId123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(true);
    expect(mockGuardsService.getGuardAdminAsync).toHaveBeenCalledWith({
      id: 'nodeId123',
    });
  });

  it('should navigate to /denied when access is not granted', async () => {
    mockGuardsService.getGuardAdminAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(false);
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should call mustRedirect when user is guest and access denied', async () => {
    mockGuardsService.getGuardAdminAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(true);
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(false);
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
  });

  it('should navigate to /denied on error', async () => {
    mockGuardsService.getGuardAdminAsync.mockRejectedValue(new Error('fail'));
    mockLoginService.isGuest.mockReturnValue(false);
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateAdmin(route, {} as never)
    );
    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });
});
