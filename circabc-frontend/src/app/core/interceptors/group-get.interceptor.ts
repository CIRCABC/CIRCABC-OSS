/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, Injector } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { Observable } from 'rxjs';

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
    return next.handle(req);
  }

  private isGroupGet(req: HttpRequest<{}>): boolean {
    return (
      req.method === 'GET' &&
      /\/service\/circabc\/groups\/[a-f0-9-]{36}$/.test(req.url)
    );
  }
}
