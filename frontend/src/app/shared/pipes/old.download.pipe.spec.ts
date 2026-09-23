import { TestBed } from '@angular/core/testing';
import { SERVER_URL } from '../../core/variables';
import { OldDownloadPipe } from './old.download.pipe';

describe('OldDownloadPipe', () => {
  let pipe: OldDownloadPipe;
  const serverUrl = 'http://localhost:8080/';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OldDownloadPipe,
        { provide: SERVER_URL, useValue: serverUrl },
      ],
    });
    pipe = TestBed.inject(OldDownloadPipe);
  });

  it('should return the download URL when id and name are provided', () => {
    const result = pipe.transform('abc-123', 'file.pdf');
    expect(result).toBe(
      'http://localhost:8080/d/a/workspace/SpacesStore/abc-123/file.pdf'
    );
  });

  it('should throw when id is undefined', () => {
    expect(() => pipe.transform(undefined, 'file.pdf')).toThrow(
      'id and name should be provided'
    );
  });

  it('should throw when name is undefined', () => {
    expect(() => pipe.transform('abc-123', undefined)).toThrow(
      'id and name should be provided'
    );
  });

  it('should throw when both id and name are undefined', () => {
    expect(() => pipe.transform(undefined, undefined)).toThrow(
      'id and name should be provided'
    );
  });
});
