import { TestBed } from '@angular/core/testing';
import { User, UserService } from 'app/core/generated/circabc';
import { UserCacheService } from 'app/core/user-cache.service';
import { vi } from 'vitest';

describe('UserCacheService', () => {
  let service: UserCacheService;
  const mockUserService = { getUserAsync: vi.fn() };

  const mockUser: User = {
    userId: 'user1',
    firstname: 'John',
    lastname: 'Doe',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [{ provide: UserService, useValue: mockUserService }],
    });
    service = TestBed.inject(UserCacheService);
  });

  it('should fetch user from UserService', async () => {
    mockUserService.getUserAsync.mockResolvedValue(mockUser);

    const result = await service.getUser('user1');

    expect(result).toEqual(mockUser);
    expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
      userId: 'user1',
    });
  });

  it('should return cached result on subsequent calls', async () => {
    mockUserService.getUserAsync.mockResolvedValue(mockUser);

    await service.getUser('user1');
    await service.getUser('user1');

    expect(mockUserService.getUserAsync).toHaveBeenCalledTimes(1);
  });

  it('should remove from cache on error', async () => {
    mockUserService.getUserAsync.mockRejectedValue(new Error('fail'));

    await expect(service.getUser('user1')).rejects.toThrow('fail');

    // After error, cache should be cleared — next call should hit service again
    mockUserService.getUserAsync.mockResolvedValue(mockUser);
    const result = await service.getUser('user1');

    expect(result).toEqual(mockUser);
    expect(mockUserService.getUserAsync).toHaveBeenCalledTimes(2);
  });

  it('should invalidate a specific user', async () => {
    mockUserService.getUserAsync.mockResolvedValue(mockUser);
    await service.getUser('user1');

    service.invalidate('user1');
    await service.getUser('user1');

    expect(mockUserService.getUserAsync).toHaveBeenCalledTimes(2);
  });

  it('should invalidate all users when called without argument', async () => {
    mockUserService.getUserAsync.mockImplementation((id: string) =>
      Promise.resolve({ userId: id })
    );

    await service.getUser('user1');
    await service.getUser('user2');

    service.invalidate();

    await service.getUser('user1');
    await service.getUser('user2');

    expect(mockUserService.getUserAsync).toHaveBeenCalledTimes(4);
  });
});
