import { Injectable } from '@angular/core';
import { ActionEmitterResult } from 'app/action-result';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ActionService {
  private actionFinishedSource: Subject<ActionEmitterResult> =
    new Subject<ActionEmitterResult>();
  public actionFinished$: Observable<ActionEmitterResult> =
    this.actionFinishedSource.asObservable();

  public propagateActionFinished(actionFinished: ActionEmitterResult): void {
    this.actionFinishedSource.next(actionFinished);
  }
}
