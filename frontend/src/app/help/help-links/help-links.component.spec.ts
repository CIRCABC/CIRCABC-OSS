import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { HelpLinksComponent } from './help-links.component';

describe('HelpLinksComponent', () => {
  let component: HelpLinksComponent;
  let fixture: ComponentFixture<HelpLinksComponent>;
  let mockHelpService: {
    removeHelpLink: ReturnType<typeof vi.fn>;
    removeHelpLinkAsync: ReturnType<typeof vi.fn>;
  };
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    getUser: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockHelpService = {
      removeHelpLink: vi.fn().mockReturnValue(of(null)),
      removeHelpLinkAsync: vi.fn().mockResolvedValue(null),
    };
    mockLoginService = {
      isGuest: vi.fn().mockReturnValue(false),
      getUser: vi.fn().mockReturnValue({ properties: { isAdmin: 'true' } }),
    };

    await TestBed.configureTestingModule({
      imports: [HelpLinksComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: HelpService, useValue: mockHelpService },
        { provide: LoginService, useValue: mockLoginService },
      ],
    })
      .overrideComponent(HelpLinksComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(HelpLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('sanitizeLinkRef', () => {
    it('should return a sanitized URL', () => {
      const result = component.sanitizeLinkRef('https://example.com');
      expect(result).toBeDefined();
    });
  });

  describe('deleteLink', () => {
    it('should call removeHelpLink and emit linkDeleted', async () => {
      const link: HelpLink = { id: '123', href: 'https://example.com' };
      const emitSpy = vi.spyOn(component.linkDeleted, 'emit');

      await component.deleteLink(link);

      expect(mockHelpService.removeHelpLinkAsync).toHaveBeenCalledWith({
        id: '123',
      });
      expect(emitSpy).toHaveBeenCalled();
    });

    it('should not call removeHelpLink if link has no id', async () => {
      const link: HelpLink = { href: 'https://example.com' };

      await component.deleteLink(link);

      expect(mockHelpService.removeHelpLinkAsync).not.toHaveBeenCalled();
    });

    it('should log error on failure', async () => {
      const link: HelpLink = { id: '123' };
      const error = new Error('fail');
      mockHelpService.removeHelpLinkAsync.mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {});

      await component.deleteLink(link);

      expect(consoleSpy).toHaveBeenCalledWith(error);
      consoleSpy.mockRestore();
    });
  });

  describe('isAdminOrSupport', () => {
    it('should return true when user is admin', () => {
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return true when user is circabc admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isCircabcAdmin: 'true' },
      });
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isAdminOrSupport()).toBe(false);
    });

    it('should return false when user has no properties', () => {
      mockLoginService.getUser.mockReturnValue({ properties: undefined });
      expect(component.isAdminOrSupport()).toBe(false);
    });

    it('should return false when user is not admin', () => {
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
      });
      expect(component.isAdminOrSupport()).toBe(false);
    });
  });

  describe('edit', () => {
    it('should emit the link id for edition', () => {
      const emitSpy = vi.spyOn(component.clickedForEdition, 'emit');
      const link: HelpLink = { id: 'abc' };

      component.edit(link);

      expect(emitSpy).toHaveBeenCalledWith('abc');
    });
  });

  describe('getTarget', () => {
    it('should return _self when link contains current hostname', () => {
      const link = `https://${globalThis.location.hostname}/page`;
      expect(component.getTarget(link)).toBe('_self');
    });

    it('should return _blank for external links', () => {
      expect(component.getTarget('https://external.com/page')).toBe('_blank');
    });

    it('should return _blank for undefined', () => {
      expect(component.getTarget(undefined)).toBe('_blank');
    });
  });
});
