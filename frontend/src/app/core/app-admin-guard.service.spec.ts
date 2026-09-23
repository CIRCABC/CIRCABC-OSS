import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { vi } from 'vitest';
import { canActivateAppAdmin } from './app-admin-guard.service';

describe('canActivateAppAdmin', () => {
  let mockLoginService: { getUser: ReturnType<typeof vi.fn> };
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/admin' } as RouterStateSnapshot;

  beforeEach(() => {
    mockLoginService = { getUser: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });
  });

  function runGuard(): boolean {
    return TestBed.runInInjectionContext(() =>
      canActivateAppAdmin(route, state)
    ) as boolean;
  }

  it('should return true when user is admin', () => {
    const user: User = {
      userId: 'admin1',
      properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
    };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(true);
  });

  it('should return true when user is circabc admin', () => {
    const user: User = {
      userId: 'admin2',
      properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
    };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(true);
  });

  it('should return false when user is guest', () => {
    const user: User = { userId: 'guest', properties: {} };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(false);
  });

  it('should return false when userId is empty', () => {
    const user: User = { userId: '', properties: {} };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(false);
  });

  it('should return false when properties is null', () => {
    const user = { userId: 'user1', properties: null } as unknown as User;
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(false);
  });

  it('should return false when properties is undefined', () => {
    const user: User = { userId: 'user1' };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(false);
  });

  it('should return false when user is not admin', () => {
    const user: User = {
      userId: 'user1',
      properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
    };
    mockLoginService.getUser.mockReturnValue(user);
    expect(runGuard()).toBe(false);
  });
});
