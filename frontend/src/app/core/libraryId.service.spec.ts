import { TestBed } from '@angular/core/testing';
import { LibraryIdService } from 'app/core/libraryId.service';

describe('LibraryIdService', () => {
  let service: LibraryIdService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LibraryIdService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit value when updateLibraryId is called', () => {
    const emitted: string[] = [];
    service.libraryIdSubject$.subscribe((value) => emitted.push(value));

    service.updateLibraryId('abc-123');

    expect(emitted).toEqual(['abc-123']);
  });

  it('should emit multiple values in order', () => {
    const emitted: string[] = [];
    service.libraryIdSubject$.subscribe((value) => emitted.push(value));

    service.updateLibraryId('first');
    service.updateLibraryId('second');

    expect(emitted).toEqual(['first', 'second']);
  });

  it('should not emit values to subscribers that subscribed after emission', () => {
    service.updateLibraryId('before-subscribe');

    const emitted: string[] = [];
    service.libraryIdSubject$.subscribe((value) => emitted.push(value));

    expect(emitted).toEqual([]);
  });
});
