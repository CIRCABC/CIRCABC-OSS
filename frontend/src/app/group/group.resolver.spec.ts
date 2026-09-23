import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { resolveGroup } from './group.resolver';

describe('resolveGroup', () => {
  let mockInterestGroupService: { getInterestGroup: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockInterestGroupService = {
      getInterestGroup: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: InterestGroupService,
          useValue: mockInterestGroupService,
        },
      ],
    });
  });

  const runResolver = (route: ActivatedRouteSnapshot) =>
    TestBed.runInInjectionContext(() =>
      resolveGroup(route, {} as RouterStateSnapshot)
    ) as Observable<InterestGroup | { id: string }>;

  it('should return the interest group on success', () => {
    const group = { id: '123', name: 'Test Group' } as InterestGroup;
    mockInterestGroupService.getInterestGroup.mockReturnValue(of(group));

    const route = {
      params: { id: '123' },
    } as unknown as ActivatedRouteSnapshot;

    runResolver(route).subscribe((result) => {
      expect(result).toEqual(group);
    });

    expect(mockInterestGroupService.getInterestGroup).toHaveBeenCalledWith({
      id: '123',
    });
  });

  it('should return an object with id when the service errors', () => {
    mockInterestGroupService.getInterestGroup.mockReturnValue(
      throwError(() => new Error('Not found'))
    );

    const route = {
      params: { id: '456' },
    } as unknown as ActivatedRouteSnapshot;

    runResolver(route).subscribe((result) => {
      expect(result).toEqual({ id: '456' });
    });

    expect(mockInterestGroupService.getInterestGroup).toHaveBeenCalledWith({
      id: '456',
    });
  });
});
