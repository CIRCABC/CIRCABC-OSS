import { TestBed } from '@angular/core/testing';
import { SERVER_URL } from 'app/core/variables';
import { DownloadUtilService } from 'app/shared/services/download-util.service';

describe('DownloadUtilService', () => {
  let service: DownloadUtilService;
  const serverUrl = 'http://localhost:8080/';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: SERVER_URL, useValue: serverUrl }],
    });
    service = TestBed.inject(DownloadUtilService);
  });

  it('should return download URL when id is provided', () => {
    expect(service.getDownloadUrl('abc123')).toBe(
      'http://localhost:8080/rest/download/abc123'
    );
  });

  it('should throw when id is undefined', () => {
    expect(() => service.getDownloadUrl(undefined)).toThrow(
      'id should be provided'
    );
  });

  it('should throw when id is empty string', () => {
    expect(() => service.getDownloadUrl('')).toThrow('id should be provided');
  });
});
