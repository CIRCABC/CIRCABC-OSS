import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateGroupMembersAdmin } from './group-members-admin-guard.service';

describe('canActivateGroupMembersAdmin', () => {
  const mockGuardsService = { getGuardGroupMembersAdminAsync: vi.fn() };
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
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return false when nodeId is 0', async () => {
    const route = createRoute({ id: '0' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return true when guard grants access', async () => {
    mockGuardsService.getGuardGroupMembersAdminAsync.mockResolvedValue({
      granted: true,
    });
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(true);
    expect(
      mockGuardsService.getGuardGroupMembersAdminAsync
    ).toHaveBeenCalledWith({
      id: 'abc123',
    });
  });

  it('should navigate to /denied when access is not granted', async () => {
    mockGuardsService.getGuardGroupMembersAdminAsync.mockResolvedValue({
      granted: false,
    });
    mockLoginService.isGuest.mockReturnValue(false);
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should call mustRedirect when user is guest and access denied', async () => {
    mockGuardsService.getGuardGroupMembersAdminAsync.mockResolvedValue({
      granted: false,
    });
    mockLoginService.isGuest.mockReturnValue(true);
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(false);
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should return false when response is undefined', async () => {
    mockGuardsService.getGuardGroupMembersAdminAsync.mockResolvedValue(
      undefined
    );
    const route = createRoute({ id: 'abc123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateGroupMembersAdmin(route, {} as never)
    );
    expect(result).toBe(false);
  });
});
