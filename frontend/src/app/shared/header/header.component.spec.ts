import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    getCurrentUsername: ReturnType<typeof vi.fn>;
  };
  let mockUserService: {
    putUser: ReturnType<typeof vi.fn>;
    putUserAsync: ReturnType<typeof vi.fn>;
  };
  let mockTranslocoService: {
    getActiveLang: ReturnType<typeof vi.fn>;
    setActiveLang: ReturnType<typeof vi.fn>;
  };
  let mockRoute: { root: { firstChild: unknown } };

  beforeEach(async () => {
    mockLoginService = {
      isGuest: vi.fn().mockReturnValue(true),
      getCurrentUsername: vi.fn().mockReturnValue('guest'),
    };

    mockUserService = {
      putUser: vi.fn().mockReturnValue(of({})),
      putUserAsync: vi.fn().mockResolvedValue({}),
    };

    mockTranslocoService = {
      getActiveLang: vi.fn().mockReturnValue('en'),
      setActiveLang: vi.fn(),
    };

    mockRoute = {
      root: { firstChild: null },
    };

    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(HeaderComponent, {
        set: { imports: [], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('isGuest', () => {
    it('should return true when login service says guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });

    it('should return false when login service says not guest', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      expect(component.isGuest()).toBe(false);
    });
  });

  describe('isSearchVisible', () => {
    it('should return false when route has no firstChild', () => {
      mockRoute.root.firstChild = null;
      expect(component.isSearchVisible()).toBe(false);
    });

    it('should return true when in group context', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'group' }] },
        firstChild: null,
      };
      expect(component.isSearchVisible()).toBe(true);
    });

    it('should return true when in help context', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'help' }] },
        firstChild: null,
      };
      expect(component.isSearchVisible()).toBe(true);
    });
  });

  describe('isGroupContext', () => {
    it('should return true when path is group', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'group' }] },
        firstChild: null,
      };
      expect(component.isGroupContext()).toBe(true);
    });

    it('should return false when path is not group', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'explore' }] },
        firstChild: null,
      };
      expect(component.isGroupContext()).toBe(false);
    });
  });

  describe('isHelpContext', () => {
    it('should return true when path is help', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'help' }] },
        firstChild: null,
      };
      expect(component.isHelpContext()).toBe(true);
    });

    it('should return false when path is not help', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'group' }] },
        firstChild: null,
      };
      expect(component.isHelpContext()).toBe(false);
    });
  });

  describe('getGroupId', () => {
    it('should return undefined when not in group context', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'explore' }] },
        firstChild: null,
      };
      expect(component.getGroupId()).toBeUndefined();
    });

    it('should return group id when in group context with child', () => {
      mockRoute.root.firstChild = {
        snapshot: { url: [{ path: 'group' }] },
        firstChild: { snapshot: { url: [{ path: 'abc-123' }] } },
      };
      expect(component.getGroupId()).toBe('abc-123');
    });
  });

  describe('currentLang', () => {
    it('should return the active language', () => {
      mockTranslocoService.getActiveLang.mockReturnValue('fr');
      expect(component.currentLang).toBe('fr');
    });
  });

  describe('refreshUILang', () => {
    it('should set active lang', async () => {
      await component.refreshUILang('fr');
      expect(mockTranslocoService.setActiveLang).toHaveBeenCalledWith('fr');
    });

    it('should not call putUser when guest', async () => {
      mockLoginService.isGuest.mockReturnValue(true);
      await component.refreshUILang('fr');
      expect(mockUserService.putUserAsync).not.toHaveBeenCalled();
    });

    it('should call putUser when not guest', async () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getCurrentUsername.mockReturnValue('admin');
      await component.refreshUILang('de');
      expect(mockUserService.putUserAsync).toHaveBeenCalledWith({
        userId: 'admin',
        user: {
          uiLang: 'de',
        },
      });
    });
  });

  describe('propagateSearch', () => {
    it('should emit searchPropagated', () => {
      const spy = vi.fn();
      component.searchPropagated.subscribe(spy);
      component.propagateSearch('test query');
      expect(spy).toHaveBeenCalledWith('test query');
    });
  });

  describe('environmentServerUrl', () => {
    it('should return base URL when no UUID in location', () => {
      Object.defineProperty(globalThis, 'location', {
        value: { href: 'http://localhost:4200/explore' },
        writable: true,
        configurable: true,
      });
      expect(component.environmentServerUrl()).toContain(
        'jsp/extension/index.jsp'
      );
    });

    it('should return browse URL when UUID found in location', () => {
      Object.defineProperty(globalThis, 'location', {
        value: {
          href: 'http://localhost:4200/group/12345678-1234-1234-1234-123456789abc/library',
        },
        writable: true,
        configurable: true,
      });
      expect(component.environmentServerUrl()).toContain(
        'w/browse/12345678-1234-1234-1234-123456789abc'
      );
    });
  });
});
