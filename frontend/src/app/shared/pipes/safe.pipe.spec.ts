import { TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { SafePipe } from 'app/shared/pipes/safe.pipe';
import { vi } from 'vitest';

describe('SafePipe', () => {
  let pipe: SafePipe;
  const mockSanitizer = {
    bypassSecurityTrustResourceUrl: vi.fn().mockReturnValue('sanitized-url'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SafePipe, { provide: DomSanitizer, useValue: mockSanitizer }],
    });
    pipe = TestBed.inject(SafePipe);
  });

  it('should return a safe resource URL when url is provided', () => {
    const result = pipe.transform('http://example.com');
    expect(mockSanitizer.bypassSecurityTrustResourceUrl).toHaveBeenCalledWith(
      'http://example.com'
    );
    expect(result).toBe('sanitized-url');
  });

  it('should throw an error when url is undefined', () => {
    expect(() => pipe.transform(undefined)).toThrow(
      'Only http and https resource URLs are allowed'
    );
  });
});
