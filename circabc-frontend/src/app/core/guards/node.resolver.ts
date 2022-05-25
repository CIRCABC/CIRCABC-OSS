import {
  ActivatedRouteSnapshot,
  Resolve,
  RouterStateSnapshot,
} from '@angular/router';

import { catchError, Observable, of } from 'rxjs';

import { Injectable } from '@angular/core';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';

@Injectable({ providedIn: 'root' })
export class NodeResolver implements Resolve<ModelNode> {
  public constructor(private nodesService: NodesService) {}

  resolve(
    route: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<ModelNode> {
    const id = route.params.id;
    return this.nodesService
      .getNode(id)
      .pipe(catchError((_error) => of({ id: id })));
  }
}
