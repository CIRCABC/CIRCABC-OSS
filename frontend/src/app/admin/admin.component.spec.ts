import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AdminComponent } from 'app/admin/admin.component';
import { User } from 'app/core/generated/circabc';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

describe('AdminComponent', () => {
  let component: AdminComponent;
  let router: Router;
  const mockLoginService = { getUser: vi.fn() };
  const mockHeaderReloadListenerService = { propagateHeaderRefresh: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminComponent],
      providers: [
        provideRouter([]),
        { provide: LoginService, useValue: mockLoginService },
        {
          provide: HeaderReloadListenerService,
          useValue: mockHeaderReloadListenerService,
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    router = TestBed.inject(Router);
    const fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('checkCurrentRouteActive', () => {
    it('should return true when route is in the URL', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/headers');
      expect(component.checkCurrentRouteActive('headers')).toBe(true);
    });

    it('should return false when route is not in the URL', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/headers');
      expect(component.checkCurrentRouteActive('circabc')).toBe(false);
    });
  });

  describe('isHeadersRoute', () => {
    it('should return true when on headers route', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/headers');
      expect(component.isHeadersRoute()).toBe(true);
    });

    it('should return false when not on headers route', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/circabc');
      expect(component.isHeadersRoute()).toBe(false);
    });
  });

  describe('isCircabcRoute', () => {
    it('should return true when on circabc route', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/circabc');
      expect(component.isCircabcRoute()).toBe(true);
    });

    it('should return false when not on circabc route', () => {
      vi.spyOn(router, 'url', 'get').mockReturnValue('/admin/headers');
      expect(component.isCircabcRoute()).toBe(false);
    });
  });

  describe('isAdmin', () => {
    it('should return true when user is admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'true' },
      } as User);
      expect(component.isAdmin()).toBe(true);
    });

    it('should return false when user is not admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'false' },
      } as User);
      expect(component.isAdmin()).toBe(false);
    });

    it('should return false when properties are undefined', () => {
      mockLoginService.getUser.mockReturnValue({} as User);
      expect(component.isAdmin()).toBe(false);
    });
  });

  describe('isCircabcAdmin', () => {
    it('should return true when user is circabc admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isCircabcAdmin: 'true' },
      } as User);
      expect(component.isCircabcAdmin()).toBe(true);
    });

    it('should return false when user is not circabc admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isCircabcAdmin: 'false' },
      } as User);
      expect(component.isCircabcAdmin()).toBe(false);
    });
  });

  describe('addHeader', () => {
    it('should set showAddHeaderModal to true', () => {
      component.addHeader();
      expect(component.showAddHeaderModal()).toBe(true);
    });
  });

  describe('loadHeaders', () => {
    it('should call propagateHeaderRefresh', () => {
      component.loadHeaders();
      expect(
        mockHeaderReloadListenerService.propagateHeaderRefresh
      ).toHaveBeenCalled();
    });
  });
});
