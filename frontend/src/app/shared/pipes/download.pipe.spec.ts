import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { DownloadUtilService } from '../services/download-util.service';
import { DownloadPipe } from './download.pipe';

describe('DownloadPipe', () => {
  let pipe: DownloadPipe;
  const mockDownloadUtil = {
    getDownloadUrl: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DownloadPipe,
        { provide: DownloadUtilService, useValue: mockDownloadUtil },
      ],
    });
    pipe = TestBed.inject(DownloadPipe);
  });

  it('should return download URL for a given id', () => {
    mockDownloadUtil.getDownloadUrl.mockReturnValue(
      'http://localhost/rest/download/123'
    );
    expect(pipe.transform('123')).toBe('http://localhost/rest/download/123');
    expect(mockDownloadUtil.getDownloadUrl).toHaveBeenCalledWith('123');
  });

  it('should throw when id is undefined', () => {
    mockDownloadUtil.getDownloadUrl.mockImplementation(() => {
      throw new Error('id should be provided');
    });
    expect(() => pipe.transform(undefined)).toThrow('id should be provided');
  });
});
