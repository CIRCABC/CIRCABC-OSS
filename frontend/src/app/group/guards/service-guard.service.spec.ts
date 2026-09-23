import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';
import { canActivateService } from './service-guard.service';

describe('canActivateService', () => {
  const mockGuardsService = { getGuardGroupServiceAsync: vi.fn() };
  const mockLoginService = { isGuest: vi.fn() };
  const mockRedirectionService = { mustRedirect: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

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

  function createRoute(id: string | null): ActivatedRouteSnapshot {
    return {
      parent: {
        parent: {
          paramMap: { get: (_key: string) => id },
        },
      },
    } as unknown as ActivatedRouteSnapshot;
  }

  function createState(url: string): RouterStateSnapshot {
    return { url } as RouterStateSnapshot;
  }

  async function runGuard(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    return TestBed.runInInjectionContext(() =>
      canActivateService(route, state)
    ) as Promise<boolean>;
  }

  it('should return false when nodeId is missing', async () => {
    const route = {
      parent: { parent: { paramMap: { get: () => null } } },
    } as unknown as ActivatedRouteSnapshot;
    const result = await runGuard(route, createState('/members'));
    expect(result).toBe(false);
  });

  it('should return false when nodeId is 0', async () => {
    const result = await runGuard(createRoute('0'), createState('/members'));
    expect(result).toBe(false);
  });

  it('should return false when parent.parent is missing', async () => {
    const route = {
      parent: { parent: null },
    } as unknown as ActivatedRouteSnapshot;
    const result = await runGuard(route, createState('/members'));
    expect(result).toBe(false);
  });

  it('should return true when access is granted', async () => {
    mockGuardsService.getGuardGroupServiceAsync.mockResolvedValue({
      granted: true,
    });
    const result = await runGuard(
      createRoute('123'),
      createState('/group/123/members')
    );
    expect(result).toBe(true);
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '123',
      name: 'members',
    });
  });

  it('should navigate to /denied when access is not granted', async () => {
    mockGuardsService.getGuardGroupServiceAsync.mockResolvedValue({
      granted: false,
    });
    mockLoginService.isGuest.mockReturnValue(false);
    const result = await runGuard(
      createRoute('123'),
      createState('/group/123/information')
    );
    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should call mustRedirect when user is guest and access denied', async () => {
    mockGuardsService.getGuardGroupServiceAsync.mockResolvedValue({
      granted: false,
    });
    mockLoginService.isGuest.mockReturnValue(true);
    const result = await runGuard(
      createRoute('123'),
      createState('/group/123/members')
    );
    expect(result).toBe(false);
    expect(mockRedirectionService.mustRedirect).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should navigate to /denied on API error (granted defaults to false)', async () => {
    mockGuardsService.getGuardGroupServiceAsync.mockRejectedValue(
      new Error('fail')
    );
    mockLoginService.isGuest.mockReturnValue(false);
    const result = await runGuard(
      createRoute('456'),
      createState('/group/456/applicants')
    );
    expect(result).toBe(false);
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '456',
      name: 'applicants',
    });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should detect service name from URL correctly', async () => {
    mockGuardsService.getGuardGroupServiceAsync.mockResolvedValue({
      granted: true,
    });

    await runGuard(createRoute('1'), createState('/group/1/members'));
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '1',
      name: 'members',
    });

    await runGuard(createRoute('2'), createState('/group/2/applicants'));
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '2',
      name: 'applicants',
    });

    await runGuard(createRoute('3'), createState('/group/3/information'));
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '3',
      name: 'information',
    });

    await runGuard(createRoute('4'), createState('/group/4/library'));
    expect(mockGuardsService.getGuardGroupServiceAsync).toHaveBeenCalledWith({
      id: '4',
      name: '',
    });
  });
});
