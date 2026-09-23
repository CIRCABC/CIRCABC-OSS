import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';

/**
 * Determines whether the given request is a `GET` for a single group's detail
 * endpoint.
 *
 * Matches URLs ending in `/service/circabc/groups/{uuid}`, where `{uuid}` is a
 * 36-character hexadecimal/dashed identifier.
 *
 * @param req The HTTP request to test.
 * @returns `true` if the request is a group detail `GET`, otherwise `false`.
 */
const isGroupGet = (req: HttpRequest<unknown>): boolean =>
  req.method === 'GET' &&
  /\/service\/circabc\/groups\/[a-f0-9-]{36}$/.test(req.url);

/**
 * Functional HTTP interceptor that suppresses server-side visit logging for
 * group detail `GET` requests.
 *
 * When a request targets a single interest group endpoint
 * (`/service/circabc/groups/{uuid}`) and the current user is a guest, or the
 * group has already been visited during the session, it appends a `log=false`
 * query parameter so the backend skips recording the visit. All other requests
 * pass through untouched.
 *
 * {@link LoginService} and {@link VisitedGroupService} are resolved with
 * `inject()` at request time.
 */
export const groupGetInterceptor: HttpInterceptorFn = (req, next) => {
  if (!isGroupGet(req)) {
    return next(req);
  }

  const loginService = inject(LoginService);
  const visitedGroupService = inject(VisitedGroupService);

  const id = req.url.substring(req.url.lastIndexOf('/') + 1);

  if (loginService.isGuest() || visitedGroupService.isVisited(id)) {
    return next(req.clone({ params: req.params.set('log', 'false') }));
  }

  return next(req);
};
