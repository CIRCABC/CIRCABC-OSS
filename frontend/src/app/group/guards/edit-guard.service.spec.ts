import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateNodeEdit } from './edit-guard.service';

describe('canActivateNodeEdit', () => {
  const mockGuardsService = { getGuardEditAsync: vi.fn() };
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

  function createRoute(params: Record<string, string>): ActivatedRouteSnapshot {
    return {
      paramMap: { get: (key: string) => params[key] ?? null },
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

  function runGuard(route: ActivatedRouteSnapshot): Promise<boolean> {
    return TestBed.runInInjectionContext(() =>
      canActivateNodeEdit(route, {} as never)
    ) as Promise<boolean>;
  }

  it('should return false when no nodeId param exists', async () => {
    const result = await runGuard(createRoute({}));
    expect(result).toBe(false);
  });

  it('should return false when nodeId is "0"', async () => {
    const result = await runGuard(createRoute({ nodeId: '0' }));
    expect(result).toBe(false);
  });

  it('should return true when guard grants access', async () => {
    mockGuardsService.getGuardEditAsync.mockResolvedValue({ granted: true });
    const result = await runGuard(createRoute({ nodeId: 'abc' }));
    expect(result).toBe(true);
  });

  it('should navigate to /denied when access is not granted and user is not guest', async () => {
    mockGuardsService.getGuardEditAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(false);

    const result = await runGuard(createRoute({ nodeId: 'abc' }));

    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
    expect(mockRedirectionService.mustRedirect).not.toHaveBeenCalled();
  });

  it('should call mustRedirect and navigate to /denied when user is guest', async () => {
    mockGuardsService.getGuardEditAsync.mockResolvedValue({ granted: false });
    mockLoginService.isGuest.mockReturnValue(true);

    const result = await runGuard(createRoute({ nodeId: 'abc' }));

    expect(result).toBe(false);
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should navigate to /no-content when API call throws', async () => {
    mockGuardsService.getGuardEditAsync.mockRejectedValue(new Error('fail'));

    const result = await runGuard(createRoute({ nodeId: 'abc' }));

    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/no-content']);
  });

  it('should use forumId param when available', async () => {
    mockGuardsService.getGuardEditAsync.mockResolvedValue({ granted: true });
    const result = await runGuard(createRoute({ forumId: 'forum1' }));
    expect(result).toBe(true);
    expect(mockGuardsService.getGuardEditAsync).toHaveBeenCalledWith({
      id: 'forum1',
    });
  });

  it('should use topicId param when forumId is absent', async () => {
    mockGuardsService.getGuardEditAsync.mockResolvedValue({ granted: true });
    const result = await runGuard(createRoute({ topicId: 'topic1' }));
    expect(result).toBe(true);
    expect(mockGuardsService.getGuardEditAsync).toHaveBeenCalledWith({
      id: 'topic1',
    });
  });
});
