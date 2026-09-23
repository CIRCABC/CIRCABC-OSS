import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NodesService } from 'app/core/generated/circabc';
import { vi } from 'vitest';
import { MigratedLinkService } from './migrated-link.service';

describe('MigratedLinkService', () => {
  const mockNodesService = {
    resolveByOriginalNodeRefAsync: vi.fn(),
    getGroupAsync: vi.fn(),
  };
  const mockRouter = { navigateByUrl: vi.fn() };

  let service: MigratedLinkService;

  const IG_OLD = '11111111-1111-1111-1111-111111111111';
  const IG_NEW = '22222222-2222-2222-2222-222222222222';
  const DOC_OLD = '33333333-3333-3333-3333-333333333333';
  const DOC_NEW = '44444444-4444-4444-4444-444444444444';

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        MigratedLinkService,
        { provide: NodesService, useValue: mockNodesService },
        { provide: Router, useValue: mockRouter },
      ],
    });
    service = TestBed.inject(MigratedLinkService);
  });

  it('returns false for empty url', async () => {
    const result = await service.tryRedirectFromOriginal('');
    expect(result).toBe(false);
    expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
  });

  it('returns false for non-group urls', async () => {
    const result = await service.tryRedirectFromOriginal('/user/foo');
    expect(result).toBe(false);
    expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
  });

  it('returns false when no segment resolves', async () => {
    mockNodesService.resolveByOriginalNodeRefAsync.mockRejectedValue(
      new Error('404')
    );
    const result = await service.tryRedirectFromOriginal(`/group/${IG_OLD}`);
    expect(result).toBe(false);
    expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
  });

  it('rewrites resolvable uuid segments and redirects (keeping trailing path + query)', async () => {
    mockNodesService.resolveByOriginalNodeRefAsync.mockImplementation(
      ({ originalId }: { originalId: string }) => {
        if (originalId === IG_OLD) return Promise.resolve({ id: IG_NEW });
        if (originalId === DOC_OLD) return Promise.resolve({ id: DOC_NEW });
        return Promise.reject(new Error('404'));
      }
    );

    const result = await service.tryRedirectFromOriginal(
      `/group/${IG_OLD}/library/document/${DOC_OLD}?foo=bar`
    );

    expect(result).toBe(true);
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith(
      `/group/${IG_NEW}/library/document/${DOC_NEW}?foo=bar`,
      { replaceUrl: true }
    );
  });

  it('derives the new IG id from a resolved child when the IG segment is not migrated', async () => {
    mockNodesService.resolveByOriginalNodeRefAsync.mockImplementation(
      ({ originalId }: { originalId: string }) => {
        if (originalId === DOC_OLD) return Promise.resolve({ id: DOC_NEW });
        // IG_OLD itself was not stamped with an original ref
        return Promise.reject(new Error('404'));
      }
    );
    mockNodesService.getGroupAsync.mockResolvedValue({ id: IG_NEW });

    const result = await service.tryRedirectFromOriginal(
      `/group/${IG_OLD}/library/document/${DOC_OLD}`
    );

    expect(result).toBe(true);
    expect(mockNodesService.getGroupAsync).toHaveBeenCalledWith({
      id: DOC_NEW,
    });
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith(
      `/group/${IG_NEW}/library/document/${DOC_NEW}`,
      { replaceUrl: true }
    );
  });
});
