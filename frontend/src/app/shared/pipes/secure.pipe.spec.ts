import { ChangeDetectorRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { UrlHelperService } from 'app/core/url-helper.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { SecurePipe } from './secure.pipe';

describe('SecurePipe', () => {
  let pipe: SecurePipe;
  let mockUrlHelperService: { get: ReturnType<typeof vi.fn> };
  let mockSanitizer: { bypassSecurityTrustUrl: ReturnType<typeof vi.fn> };
  let mockCdr: { markForCheck: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockUrlHelperService = { get: vi.fn() };
    mockSanitizer = { bypassSecurityTrustUrl: vi.fn((v: string) => v) };
    mockCdr = { markForCheck: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        SecurePipe,
        { provide: UrlHelperService, useValue: mockUrlHelperService },
        { provide: DomSanitizer, useValue: mockSanitizer },
        { provide: ChangeDetectorRef, useValue: mockCdr },
      ],
    });

    pipe = TestBed.inject(SecurePipe);
  });

  afterEach(() => {
    pipe.ngOnDestroy();
  });

  it('should return empty object for falsy url', () => {
    const result = pipe.transform('');
    expect(result).toEqual({});
  });

  it('should fetch url and return sanitized value on subsequent calls', () => {
    const blobUrl = 'blob:http://localhost/abc';
    mockUrlHelperService.get.mockReturnValue(of(blobUrl));

    // First call subscribes
    pipe.transform('http://example.com/img.png');
    // Second call returns the resolved value
    const result = pipe.transform('http://example.com/img.png');

    expect(mockUrlHelperService.get).toHaveBeenCalledWith(
      'http://example.com/img.png'
    );
    expect(mockSanitizer.bypassSecurityTrustUrl).toHaveBeenCalledWith(blobUrl);
    expect(result).toBe(blobUrl);
    expect(mockCdr.markForCheck).toHaveBeenCalled();
  });

  it('should refetch when url changes', () => {
    const blob1 = 'blob:http://localhost/1';
    const blob2 = 'blob:http://localhost/2';
    mockUrlHelperService.get
      .mockReturnValueOnce(of(blob1))
      .mockReturnValueOnce(of(blob2));

    pipe.transform('http://example.com/a.png');
    pipe.transform('http://example.com/a.png');

    pipe.transform('http://example.com/b.png');
    const result = pipe.transform('http://example.com/b.png');

    expect(mockUrlHelperService.get).toHaveBeenCalledTimes(2);
    expect(result).toBe(blob2);
  });

  it('should handle async emission after transform', () => {
    const subject = new Subject<string>();
    mockUrlHelperService.get.mockReturnValue(subject.asObservable());

    const result1 = pipe.transform('http://example.com/img.png');
    expect(result1).toBeNull();

    subject.next('blob:http://localhost/delayed');

    const result2 = pipe.transform('http://example.com/img.png');
    expect(result2).toBe('blob:http://localhost/delayed');
  });

  it('should clean up subscriptions on destroy', () => {
    mockUrlHelperService.get.mockReturnValue(of('blob:x'));
    pipe.transform('http://example.com/img.png');

    expect(() => pipe.ngOnDestroy()).not.toThrow();
  });
});
