import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { StartComponent } from './start.component';

@Pipe({ name: 'cbcSetTitle' })
class MockSetTitlePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Pipe({ name: 'cbcI18n' })
class MockI18nPipe implements PipeTransform {
  transform(value: unknown): string {
    return String(value);
  }
}

const mockHelpService = {
  getHelpCategoriesAsync: vi.fn().mockResolvedValue([]),
  getHelpLinksAsync: vi.fn().mockResolvedValue([]),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getUser: vi.fn().mockReturnValue({ properties: undefined }),
};

describe('StartComponent', () => {
  let component: StartComponent;
  let fixture: ComponentFixture<StartComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StartComponent],
      providers: [
        { provide: HelpService, useValue: mockHelpService },
        { provide: LoginService, useValue: mockLoginService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(StartComponent, {
      set: {
        imports: [TranslocoModule, MockSetTitlePipe, MockI18nPipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    fixture = TestBed.createComponent(StartComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('initial load', () => {
    it('should load categories and links', async () => {
      const categories = [{ id: '1', name: 'Cat1' }];
      const links = [{ id: 'l1', title: 'Link1' }];
      mockHelpService.getHelpCategoriesAsync.mockResolvedValue(categories);
      mockHelpService.getHelpLinksAsync.mockResolvedValue(links);

      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.categories()).toEqual(categories);
      expect(component.links()).toEqual(links);
      expect(component.loading()).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      mockHelpService.getHelpLinksAsync.mockRejectedValue(new Error('fail'));
      mockHelpService.getHelpCategoriesAsync.mockRejectedValue(
        new Error('fail')
      );

      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.categories()).toEqual([]);
      expect(component.links()).toEqual([]);
      expect(component.loading()).toBe(false);
    });
  });

  describe('isAdminOrSupport', () => {
    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isAdminOrSupport()).toBe(false);
    });

    it('should return true when user is admin', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'true' },
      });
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return true when user is circabc admin', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isCircabcAdmin: 'true' },
      });
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return false when user has no admin properties', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'false' },
      });
      expect(component.isAdminOrSupport()).toBe(false);
    });
  });

  describe('refresh', () => {
    it('should reload categories', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      const categories = [{ id: '2', name: 'Cat2' }];
      mockHelpService.getHelpCategoriesAsync.mockResolvedValue(categories);

      component.refresh({} as never);
      await fixture.whenStable();

      expect(component.categories()).toEqual(categories);
      expect(component.loading()).toBe(false);
    });
  });

  describe('refreshLinks', () => {
    it('should reload links and close modal', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      component.showCreateLinkModal = true;
      const links = [{ id: 'l2', title: 'Link2' }];
      mockHelpService.getHelpLinksAsync.mockResolvedValue(links);

      component.refreshLinks();
      await fixture.whenStable();

      expect(component.links()).toEqual(links);
      expect(component.showCreateLinkModal).toBe(false);
      expect(component.loading()).toBe(false);
    });
  });

  describe('openForEdit', () => {
    it('should set linkId and open modal', () => {
      component.openForEdit('abc');
      expect(component.linkId).toBe('abc');
      expect(component.showCreateLinkModal).toBe(true);
    });

    it('should not open modal for empty linkId', () => {
      component.showCreateLinkModal = false;
      component.openForEdit('');
      expect(component.showCreateLinkModal).toBe(false);
    });
  });
});
