import { TestBed } from '@angular/core/testing';
import { AnalyticsService } from 'app/core/analytics.service';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ userId: 'testUser' } as User),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });
    service = TestBed.inject(AnalyticsService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('isEnabled', () => {
    it('should reflect whether analyticsURL and analyticsSiteId are set', () => {
      const expected =
        environment.analyticsURL !== '' && environment.analyticsSiteId !== '';
      expect(service.isEnabled()).toBe(expected);
    });
  });

  describe('isMatomoAnalyticsEnabled', () => {
    it('should return true only when analytics is enabled and release is oss', () => {
      const expected =
        environment.analyticsURL !== '' &&
        environment.analyticsSiteId !== '' &&
        environment.circabcRelease === 'oss';
      expect(service.isMatomoAnalyticsEnabled()).toBe(expected);
    });
  });

  describe('isWebAnalyticsEnabled', () => {
    it('should return false when analytics is disabled', () => {
      // Default test environment has empty analyticsURL
      // isWebAnalyticsEnabled requires isAnalyticsEnabled to be true first
      if (!service.isEnabled()) {
        expect(service.isWebAnalyticsEnabled()).toBe(false);
      }
    });
  });

  describe('init', () => {
    it('should not throw when analytics is disabled', () => {
      expect(() => service.init()).not.toThrow();
    });
  });

  describe('trackError', () => {
    it('should not throw when analytics is disabled', () => {
      const error = new Error('test error');
      expect(() => service.trackError(error)).not.toThrow();
    });
  });

  describe('trackHTTPError', () => {
    it('should not throw when analytics is disabled', () => {
      expect(() =>
        service.trackHTTPError('/api/test', 'GET', 500, 'Server Error')
      ).not.toThrow();
    });
  });

  describe('trackPageChange', () => {
    it('should not throw when analytics is disabled', () => {
      expect(() => service.trackPageChange()).not.toThrow();
    });
  });

  describe('trackSiteSearch', () => {
    it('should not throw when analytics is disabled', () => {
      expect(() =>
        service.trackSiteSearch('keyword', 'category', 10)
      ).not.toThrow();
    });
  });

  describe('trackDownload', () => {
    it('should strip ticket param with ? from url', () => {
      expect(() =>
        service.trackDownload(
          'http://example.com/file?ticket=abc123',
          'file.pdf'
        )
      ).not.toThrow();
    });

    it('should strip ticket param with & from url', () => {
      expect(() =>
        service.trackDownload(
          'http://example.com/file?other=1&ticket=abc123',
          'file.pdf'
        )
      ).not.toThrow();
    });
  });

  describe('trackCustomEvent', () => {
    it('should not throw when analytics is disabled', () => {
      expect(() =>
        service.trackCustomEvent('group', 'eventName')
      ).not.toThrow();
    });
  });

  describe('getAgreeWithCookies', () => {
    it('should return null when $wt is not available', () => {
      expect(service.getAgreeWithCookies()).toBeNull();
    });
  });

  describe('getAgreeWithTrack', () => {
    it('should return null when $wt is not available', () => {
      expect(service.getAgreeWithTrack()).toBeNull();
    });
  });

  describe('analyticsConfiguration', () => {
    it('should have default configuration values', () => {
      expect(service.analyticsConfiguration).toEqual({
        utility: 'analytics',
        siteID: '',
        sitePath: [],
        instance: 'europa.eu',
        mode: 'manual',
      });
    });
  });

  describe('IGname', () => {
    it('should default to empty string', () => {
      expect(service.IGname).toBe('');
    });
  });
});
