import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateNodeAccess } from './access-guard.service';

describe('canActivateNodeAccess', () => {
  const mockGuardsService = { getGuardAccessAsync: vi.fn() };
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

  function createRoute(
    params: Record<string, string>,
    queryParams: Record<string, string> = {}
  ): ActivatedRouteSnapshot {
    return {
      paramMap: { get: (key: string) => params[key] ?? null },
      queryParamMap: { get: (key: string) => queryParams[key] ?? null },
    } as unknown as ActivatedRouteSnapshot;
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

  it('should return false when no nodeId is found in route', async () => {
    const route = createRoute({});
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return false when nodeId is "0"', async () => {
    const route = createRoute({ nodeId: '0' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(result).toBe(false);
  });

  it('should return true when access is granted', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: true });
    const route = createRoute({ nodeId: '123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(result).toBe(true);
  });

  it('should navigate to /no-content and return false when API throws', async () => {
    mockGuardsService.getGuardAccessAsync.mockRejectedValue(new Error('fail'));
    const route = createRoute({ nodeId: '123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/no-content']);
    expect(result).toBe(false);
  });

  it('should navigate to /denied when access is denied and user is not guest', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(false);
    const route = createRoute({ nodeId: '123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
    expect(result).toBe(false);
  });

  it('should call mustRedirect when access is denied and user is guest', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(true);
    const route = createRoute({ nodeId: '123' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(result).toBe(false);
  });

  it('should not navigate to /denied when fromLink is true', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(false);
    const route = createRoute({ nodeId: '123' }, { fromLink: 'true' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockRouter.navigate).not.toHaveBeenCalledWith(['/denied']);
    expect(result).toBe(false);
  });

  it('should resolve nodeId from forumId param', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: true });
    const route = createRoute({ forumId: '456' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockGuardsService.getGuardAccessAsync).toHaveBeenCalledWith({
      id: '456',
    });
    expect(result).toBe(true);
  });

  it('should resolve nodeId from topicId param', async () => {
    mockGuardsService.getGuardAccessAsync.mockResolvedValue({ granted: true });
    const route = createRoute({ topicId: '789' });
    const result = await TestBed.runInInjectionContext(() =>
      canActivateNodeAccess(route, {} as never)
    );
    expect(mockGuardsService.getGuardAccessAsync).toHaveBeenCalledWith({
      id: '789',
    });
    expect(result).toBe(true);
  });
});
