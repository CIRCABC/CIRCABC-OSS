import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavigationEnd, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import { InterestGroupService } from 'app/core/generated/circabc';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { NavigatorComponent } from './navigator.component';

describe('NavigatorComponent', () => {
  let component: NavigatorComponent;
  let fixture: ComponentFixture<NavigatorComponent>;

  const routerEvents$ = new Subject<NavigationEnd>();
  const groupRefresh$ = new Subject<string>();

  const mockRouter = {
    url: '/me',
    events: routerEvents$.asObservable(),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({
      userId: 'testuser',
      properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
    }),
  };

  const mockInterestGroupService = {
    getInterestGroupAsync: vi
      .fn()
      .mockResolvedValue({ name: 'TestIG', permissions: {} }),
  };

  const mockGroupReloadListenerService = {
    refreshAnnounced$: groupRefresh$.asObservable(),
  };

  const mockAnalyticsService = {
    IGname: '',
    trackCustomEvent: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigatorComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: LoginService, useValue: mockLoginService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        {
          provide: GroupReloadListenerService,
          useValue: mockGroupReloadListenerService,
        },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(NavigatorComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(NavigatorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('ngOnInit - default location', () => {
    it('should set location to default when no currentIg', () => {
      fixture.detectChanges();
      expect(component.location).toBe('default');
    });

    it('should set isGuest from loginService', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      fixture.detectChanges();
      expect(component.isGuest()).toBe(true);
      mockLoginService.isGuest.mockReturnValue(false);
    });

    it('should set isMe to true when url ends with /me', () => {
      (mockRouter as { url: string }).url = '/me';
      fixture.detectChanges();
      expect(component.isMe()).toBe(true);
    });

    it('should set isCalendar to true when url ends with /calendar', () => {
      (mockRouter as { url: string }).url = '/calendar';
      fixture.detectChanges();
      expect(component.isCalendar()).toBe(true);
    });

    it('should set isRoles to true when url ends with /roles', () => {
      (mockRouter as { url: string }).url = '/roles';
      fixture.detectChanges();
      expect(component.isRoles()).toBe(true);
    });

    it('should set isExplore to true when url ends with /explore', () => {
      (mockRouter as { url: string }).url = '/explore';
      fixture.detectChanges();
      expect(component.isExplore()).toBe(true);
    });

    it('should set isHelp to true when url includes /help', () => {
      (mockRouter as { url: string }).url = '/help/category/123';
      fixture.detectChanges();
      expect(component.isHelp()).toBe(true);
    });
  });

  describe('ngOnInit - in-group location', () => {
    const igPermissions: { [key: string]: string } = {
      library: 'Admin',
      information: 'Admin',
      event: 'Admin',
      newsgroup: 'Admin',
      directory: 'Admin',
    };

    beforeEach(() => {
      (mockRouter as { url: string }).url = '/group/123/library';
    });

    it('should set location to in-group when currentIg is set', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.location).toBe('in-group');
    });

    it('should set isGroupAdmin when all permissions are Admin', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.isGroupAdmin()).toBe(true);
    });

    it('should set canSeeInformation when information permission is not NoAccess', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.canSeeInformation()).toBe(true);
    });

    it('should set canSeeEvents when event permission is not NoAccess', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.canSeeEvents()).toBe(true);
    });

    it('should set canSeeNewsgroups when newsgroup permission is not NoAccess', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.canSeeNewsgroups()).toBe(true);
    });

    it('should set canSeeMembers when directory permission is not NoAccess', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.canSeeMembers()).toBe(true);
    });

    it('should set isInsideLibrary when url includes library', () => {
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.isInsideLibrary()).toBe(true);
    });

    it('should set isForum when url includes forum', () => {
      (mockRouter as { url: string }).url = '/group/123/forum';
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.isForum()).toBe(true);
    });

    it('should set isAgenda when url includes agenda', () => {
      (mockRouter as { url: string }).url = '/group/123/agenda';
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.isAgenda()).toBe(true);
    });

    it('should set isGroupDashboard when url has exactly 3 segments with group', () => {
      (mockRouter as { url: string }).url = '/group/123';
      component.currentIg.set({ name: 'TestIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(component.isGroupDashboard()).toBe(true);
    });

    it('should track analytics when IG name changes', () => {
      mockAnalyticsService.IGname = '';
      component.currentIg.set({ name: 'NewIG', permissions: igPermissions });
      fixture.detectChanges();
      expect(mockAnalyticsService.trackCustomEvent).toHaveBeenCalledWith(
        'IG',
        'NewIG'
      );
      expect(mockAnalyticsService.IGname).toBe('NewIG');
    });
  });

  describe('isAppAdmin', () => {
    it('should return false for guest user', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'guest',
        properties: null,
      });
      expect(component.isAppAdmin()).toBe(false);
    });

    it('should return false for user with empty userId', () => {
      mockLoginService.getUser.mockReturnValue({ userId: '', properties: {} });
      expect(component.isAppAdmin()).toBe(false);
    });

    it('should return true when user isAdmin', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'admin',
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });
      expect(component.isAppAdmin()).toBe(true);
    });

    it('should return true when user isCircabcAdmin', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'cbcadmin',
        properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
      });
      expect(component.isAppAdmin()).toBe(true);
    });

    it('should return false for regular user', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'regular',
        properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
      });
      expect(component.isAppAdmin()).toBe(false);
    });
  });

  describe('router events', () => {
    it('should update flags on NavigationEnd in default location', () => {
      (mockRouter as { url: string }).url = '/explore';
      fixture.detectChanges();

      (mockRouter as { url: string }).url = '/calendar';
      routerEvents$.next(new NavigationEnd(1, '/calendar', '/calendar'));

      expect(component.isCalendar()).toBe(true);
      expect(component.isExplore()).toBe(false);
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe from subscriptions', () => {
      fixture.detectChanges();
      expect(() => component.ngOnDestroy()).not.toThrow();
    });
  });
});
