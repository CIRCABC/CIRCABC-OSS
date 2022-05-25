import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LibraryIdService {
  private libraryIdSubject: Subject<string> = new Subject<string>();

  public libraryIdSubject$: Observable<string> =
    this.libraryIdSubject.asObservable();

  public updateLibraryId(libraryId: string): void {
    this.libraryIdSubject.next(libraryId);
  }
}
