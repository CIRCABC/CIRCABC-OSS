import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GroupReloadListenerService {
  private refreshSource: Subject<string> = new Subject<string>();
  public refreshAnnounced$: Observable<string> =
    this.refreshSource.asObservable();

  public propagateGroupRefresh(groupId: string): void {
    this.refreshSource.next(groupId);
  }
}
