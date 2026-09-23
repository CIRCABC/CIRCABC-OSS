import { Service } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide service that broadcasts the currently active library
 * identifier to interested consumers.
 *
 * Registered as a root-level singleton (`providedIn: 'root'`), it acts as a
 * lightweight event bus: producers push the current library id via
 * {@link LibraryIdService.updateLibraryId} and subscribers react to changes
 * through the {@link LibraryIdService.libraryIdSubject$} observable stream.
 * This decouples components that know the active library id from those that
 * need to be notified when it changes.
 */
@Service()
export class LibraryIdService {
  /**
   * Internal RxJS {@link Subject} used to emit library id changes. Kept private
   * so that only this service can push new values, while consumers subscribe to
   * the read-only {@link LibraryIdService.libraryIdSubject$} projection.
   */
  private readonly libraryIdSubject: Subject<string> = new Subject<string>();

  /**
   * Read-only observable stream of library id updates. Emits the new library id
   * each time {@link LibraryIdService.updateLibraryId} is called. Being a
   * `Subject`-backed stream, it is hot and only delivers values emitted after a
   * subscription is established (no initial/current value is replayed).
   */
  public libraryIdSubject$: Observable<string> =
    this.libraryIdSubject.asObservable();

  /**
   * Publishes a new active library id to all current subscribers of
   * {@link LibraryIdService.libraryIdSubject$}.
   *
   * @param libraryId - The identifier of the library that has become active.
   */
  public updateLibraryId(libraryId: string): void {
    this.libraryIdSubject.next(libraryId);
  }
}
