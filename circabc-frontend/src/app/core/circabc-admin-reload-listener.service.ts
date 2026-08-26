import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CiracbcAdminReloadListenerService {
  private refreshSource: Subject<void> = new Subject<void>();
  public refreshAnnounced$: Observable<void> =
    this.refreshSource.asObservable();

  public propagateCircabcAdminRefresh(): void {
    this.refreshSource.next();
  }
}
