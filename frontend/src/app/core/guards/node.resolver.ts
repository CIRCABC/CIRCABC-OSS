import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn } from '@angular/router';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { catchError, Observable, of } from 'rxjs';

/**
 * Functional route resolver that pre-fetches the {@link ModelNode} identified
 * by the route's `id` parameter before the target route is activated.
 *
 * It relies on {@link NodesService} (the generated CIRCABC API client) to
 * retrieve the node. If the request fails, it degrades gracefully by resolving
 * to a minimal node that only carries the requested `id`, so that navigation is
 * never blocked by a fetch error.
 *
 * @param route - Snapshot of the route being activated; its `params.id`
 * provides the identifier of the node to fetch.
 * @returns An observable emitting the fetched {@link ModelNode}, or a fallback
 * node containing only the requested `id` if the fetch fails.
 */
export const resolveNode: ResolveFn<ModelNode> = (
  route: ActivatedRouteSnapshot
): Observable<ModelNode> => {
  const id = route.params.id;

  return inject(NodesService)
    .getNode({ id })
    .pipe(catchError(() => of({ id })));
};
