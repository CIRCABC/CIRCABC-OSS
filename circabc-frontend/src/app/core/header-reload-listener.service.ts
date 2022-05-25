import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class HeaderReloadListenerService {
  private refreshSource: Subject<void> = new Subject<void>();
  public refreshAnnounced$: Observable<void> =
    this.refreshSource.asObservable();

  public propagateHeaderRefresh(): void {
    this.refreshSource.next();
  }
}
