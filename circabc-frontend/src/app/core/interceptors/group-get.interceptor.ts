/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class GroupGetInterceptor implements HttpInterceptor {
  constructor(private inj: Injector) {}

  intercept(
    req: HttpRequest<{}>,
    next: HttpHandler
  ): Observable<HttpEvent<{}>> {
    if (!this.isGroupGet(req)) {
      return next.handle(req);
    }

    const router: Router = this.inj.get<Router>(Router);
    const loginService: LoginService = this.inj.get<LoginService>(LoginService);
    const visitedGroupService: VisitedGroupService =
      this.inj.get<VisitedGroupService>(VisitedGroupService);

    const id = req.url.substring(req.url.lastIndexOf('/') + 1);

    if (loginService.isGuest() || visitedGroupService.isVisited(id)) {
      const newRequest = req.clone({
        params: req.params.set('log', 'false'),
      });
      return next.handle(newRequest);
    }
    return next.handle(req).pipe(
      tap(
        // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
        (_event: HttpEvent<any>) => {},
        (err: any) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 403) {
              router.navigate(['no-content']);
            }
          }
        }
      )
    );
  }

  private isGroupGet(req: HttpRequest<{}>): boolean {
    return (
      req.method === 'GET' &&
      /\/service\/circabc\/groups\/[a-f0-9-]{36}$/.test(req.url)
    );
  }
}
