import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { InterestGroup } from 'app/core/generated/circabc/model/interestGroup';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupAdminComponent } from './group-admin.component';

const mockGroup: InterestGroup = {
  name: 'Test Group',
  permissions: { IgDelete: 'true' },
};

const mockRoute = {
  data: of({ group: mockGroup }),
};

const mockRouter = {
  url: '/group/admin/general',
};

const mockLoginService = {
  getUser: vi.fn().mockReturnValue({ userId: 'testuser' }),
};

const mockPermissionEvaluatorService = {
  isGroupAdmin: vi.fn().mockReturnValue(true),
};

const mockDialog = {
  open: vi.fn().mockReturnValue({ afterClosed: () => of(false) }),
};

describe('GroupAdminComponent', () => {
  let component: GroupAdminComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupAdminComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: LoginService, useValue: mockLoginService },
        {
          provide: PermissionEvaluatorService,
          useValue: mockPermissionEvaluatorService,
        },
        { provide: MatDialog, useValue: mockDialog },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(GroupAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set group from route data on init', () => {
    expect(component.group()).toEqual(mockGroup);
  });

  it('should set displayRequestDeleteIg based on isGroupAdmin', () => {
    expect(component.displayRequestDeleteIg()).toBe(true);
  });

  describe('checkCurrentRouteActive', () => {
    it('should return true when router url includes the route name', () => {
      expect(component.checkCurrentRouteActive('general')).toBe(true);
    });

    it('should return false when router url does not include the route name', () => {
      expect(component.checkCurrentRouteActive('logos')).toBe(false);
    });
  });

  describe('route check methods', () => {
    it('isGeneralRoute should return true for general url', () => {
      expect(component.isGeneralRoute()).toBe(true);
    });

    it('isDeleteRoute should return false for general url', () => {
      expect(component.isDeleteRoute()).toBe(false);
    });

    it('isLogRoute should return false when url contains logos', () => {
      (mockRouter as { url: string }).url = '/group/admin/logos';
      expect(component.isLogRoute()).toBe(false);
    });
  });

  describe('canDeleteGroup', () => {
    it('should return true when IgDelete permission is true', () => {
      expect(component.canDeleteGroup()).toBe(true);
    });

    it('should return false when IgDelete permission is not true', () => {
      component.group.set({ name: 'No Delete', permissions: {} });
      expect(component.canDeleteGroup()).toBe(false);
    });

    it('should return false when group is undefined', () => {
      component.group.set(undefined);
      expect(component.canDeleteGroup()).toBe(false);
    });
  });

  describe('toggleDropdown', () => {
    it('should toggle showActionsDropdown', () => {
      const event = { stopPropagation: vi.fn() } as unknown as Event;
      expect(component.showActionsDropdown).toBe(false);
      component.toggleDropdown(event);
      expect(component.showActionsDropdown).toBe(true);
      component.toggleDropdown(event);
      expect(component.showActionsDropdown).toBe(false);
    });

    it('should call stopPropagation on the event', () => {
      const event = { stopPropagation: vi.fn() } as unknown as Event;
      component.toggleDropdown(event);
      expect(event.stopPropagation).toHaveBeenCalled();
    });
  });
});
