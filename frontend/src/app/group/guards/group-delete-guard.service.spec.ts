import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { InterestGroupService } from 'app/core/generated/circabc';
import { vi } from 'vitest';
import { canActivateGroupDelete } from './group-delete-guard.service';

describe('canActivateGroupDelete', () => {
  const mockInterestGroupService = {
    getInterestGroupAsync: vi.fn(),
  };
  const mockRouter = { navigate: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: Router, useValue: mockRouter },
      ],
    });
    vi.clearAllMocks();
  });

  function runGuard(route: ActivatedRouteSnapshot): Promise<boolean> {
    return TestBed.runInInjectionContext(() =>
      canActivateGroupDelete(route, {} as never)
    ) as Promise<boolean>;
  }

  function createRoute(id: string | null): ActivatedRouteSnapshot {
    return { paramMap: { get: () => id } } as unknown as ActivatedRouteSnapshot;
  }

  it('should return true when group has IgDelete permission', async () => {
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue({
      name: 'g',
      permissions: { IgDelete: 'true' },
    });

    const result = await runGuard(createRoute('123'));

    expect(result).toBe(true);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should navigate to /denied and return false when IgDelete is not true', async () => {
    mockInterestGroupService.getInterestGroupAsync.mockResolvedValue({
      name: 'g',
      permissions: { IgDelete: 'false' },
    });

    const result = await runGuard(createRoute('123'));

    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });

  it('should navigate to /denied and return false when no group id in route', async () => {
    const result = await runGuard(createRoute(null));

    expect(result).toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/denied']);
  });
});
