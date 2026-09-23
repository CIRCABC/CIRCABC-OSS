import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn } from '@angular/router';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { catchError, Observable, of } from 'rxjs';

/**
 * Functional route resolver that pre-fetches the {@link InterestGroup} for a
 * route before the associated component is activated.
 *
 * It reads the `id` route parameter and asks the {@link InterestGroupService}
 * for the matching interest group. If the lookup fails (for example, when the
 * group cannot be found or the request errors), it gracefully falls back to a
 * minimal object containing only the requested `id` so navigation can still
 * proceed.
 *
 * {@link InterestGroupService} (generated CIRCABC API client) is resolved with
 * `inject()` within the resolver's execution context.
 *
 * @param route - Snapshot of the activated route; its `params.id` identifies
 * the interest group to resolve.
 * @returns An observable that emits the resolved {@link InterestGroup}, or a
 * `{ id: string }` fallback when the group cannot be retrieved.
 */
export const resolveGroup: ResolveFn<InterestGroup | { id: string }> = (
  route: ActivatedRouteSnapshot
): Observable<InterestGroup | { id: string }> => {
  const id = route.params.id;

  return inject(InterestGroupService)
    .getInterestGroup({ id })
    .pipe(catchError(() => of({ id })));
};
