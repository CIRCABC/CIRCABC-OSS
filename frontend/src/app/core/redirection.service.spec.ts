import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RedirectionService } from 'app/core/redirection.service';
import { vi } from 'vitest';

describe('RedirectionService', () => {
  let service: RedirectionService;
  let router: {
    navigateByUrl: ReturnType<typeof vi.fn>;
    navigate: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    router = {
      navigateByUrl: vi.fn().mockResolvedValue(true),
      navigate: vi.fn().mockResolvedValue(true),
    };

    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: router }],
    });

    service = TestBed.inject(RedirectionService);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  describe('mustRedirect', () => {
    it('should store current location href in sessionStorage', () => {
      service.mustRedirect();
      expect(sessionStorage.getItem('mustRedirect')).toBe(
        globalThis.location.href
      );
    });
  });

  describe('redirect', () => {
    it('should navigate to /me when sessionStorage is empty', async () => {
      await service.redirect();
      expect(router.navigate).toHaveBeenCalledWith(['/me']);
    });

    it('should navigate to /me when sessionStorage value is empty string', async () => {
      sessionStorage.setItem('mustRedirect', '');
      await service.redirect();
      expect(router.navigate).toHaveBeenCalledWith(['/me']);
    });

    it('should parse full URL and navigate to extracted path', async () => {
      sessionStorage.setItem(
        'mustRedirect',
        'https://example.com/circabc-caas/ui/group/123'
      );
      await service.redirect();
      expect(router.navigateByUrl).toHaveBeenCalledWith('group/123');
      expect(sessionStorage.getItem('mustRedirect')).toBeNull();
    });

    it('should navigate to raw value when no protocol is present', async () => {
      sessionStorage.setItem('mustRedirect', '/some/path');
      await service.redirect();
      expect(router.navigateByUrl).toHaveBeenCalledWith('/some/path');
      expect(sessionStorage.getItem('mustRedirect')).toBeNull();
    });
  });
});
