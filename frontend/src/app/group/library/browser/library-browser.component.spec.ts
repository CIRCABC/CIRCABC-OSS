import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { NavigationEnd, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { AlfrescoService } from 'app/core/alfresco.service';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { NotificationService, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableNode } from 'app/core/ui-model/index';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { CookieService } from 'ngx-cookie-service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { LibraryBrowserComponent } from './library-browser.component';

describe('LibraryBrowserComponent', () => {
  let component: LibraryBrowserComponent;
  let fixture: ComponentFixture<LibraryBrowserComponent>;

  const routerEvents$ = new Subject<NavigationEnd>();

  const mockRouter = {
    events: routerEvents$.asObservable(),
    navigate: vi.fn().mockResolvedValue(true),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ userId: 'testUser' }),
    isGuest: vi.fn().mockReturnValue(false),
    getCurrentUsername: vi.fn().mockReturnValue('testUser'),
    getTicket: vi.fn().mockReturnValue('ticket123'),
  };

  const mockPermEvalService = {
    isLibAdmin: vi.fn().mockReturnValue(false),
    isOwner: vi.fn().mockReturnValue(false),
  };

  const mockClipboardService = {
    addItem: vi.fn(),
  };

  const mockNotificationService = {
    putNotificationAuthorityAsync: vi.fn().mockResolvedValue({}),
    isUserSubscribedAsync: vi.fn().mockResolvedValue({ subscribed: false }),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  const mockBulkDownloadPipe = {
    transform: vi.fn().mockReturnValue('http://bulk-url'),
  };

  const mockSaveAsService = {
    saveUrlAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockPermissionEvaluator = {
    hasAnyOfPermissions: vi.fn().mockReturnValue(true),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue(''),
  };

  const mockCookieService = {
    set: vi.fn(),
  };

  const mockUserService = {
    saveUserPreferences: vi.fn().mockReturnValue(of({})),
  };

  const mockAresBridgeHelperService = {
    isAresBridgeEnabled: vi.fn().mockResolvedValue(false),
    getAlreadySentToAresBridge: vi.fn().mockResolvedValue([]),
    sendToAresBridge: vi.fn().mockResolvedValue(undefined),
  };

  const mockDialog = {
    open: vi.fn().mockReturnValue({ afterClosed: () => of(true) }),
  };

  const mockAlfrescoService = {
    getRendition: vi.fn().mockResolvedValue({ entry: { status: 'CREATED' } }),
    createRendition: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LibraryBrowserComponent],
      providers: [
        FormBuilder,
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: Router, useValue: mockRouter },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: ClipboardService, useValue: mockClipboardService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: BulkDownloadPipe, useValue: mockBulkDownloadPipe },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: PermissionEvaluator, useValue: mockPermissionEvaluator },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: CookieService, useValue: mockCookieService },
        { provide: UserService, useValue: mockUserService },
        {
          provide: AresBridgeHelperService,
          useValue: mockAresBridgeHelperService,
        },
        { provide: MatDialog, useValue: mockDialog },
        { provide: AlfrescoService, useValue: mockAlfrescoService },
      ],
    }).overrideComponent(LibraryBrowserComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(LibraryBrowserComponent);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('contents', []);
    fixture.componentRef.setInput('parent', { id: 'parent-1' });
    fixture.componentRef.setInput('resetPage', false);
    fixture.componentRef.setInput('totalItems', 25);
    fixture.componentRef.setInput('preferences', {
      library: {
        column: {
          name: true,
          title: true,
          version: true,
          modification: true,
          creation: false,
          size: true,
          expiration: true,
          status: false,
          description: true,
          author: false,
          securityRanking: false,
        },
        listing: { page: 1, limit: 10, sort: 'modified_DESC' },
      },
      search: [],
    });
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('isFile', () => {
    it('should return true for content type', () => {
      const node: SelectableNode = { type: 'content' };
      expect(component.isFile(node)).toBe(true);
    });

    it('should return false for folder type', () => {
      const node: SelectableNode = { type: 'folder' };
      expect(component.isFile(node)).toBe(false);
    });

    it('should return false for filelink type', () => {
      const node: SelectableNode = { type: 'filelink' };
      expect(component.isFile(node)).toBe(false);
    });

    it('should return false when type is undefined', () => {
      const node: SelectableNode = {};
      expect(component.isFile(node)).toBe(false);
    });
  });

  describe('isFolder', () => {
    it('should return true for folder type', () => {
      const node: SelectableNode = { type: 'folder' };
      expect(component.isFolder(node)).toBe(true);
    });

    it('should return false for content type', () => {
      const node: SelectableNode = { type: 'content' };
      expect(component.isFolder(node)).toBe(false);
    });

    it('should return true for folderlink type (contains folder, not filelink)', () => {
      const node: SelectableNode = { type: 'folderlink' };
      expect(component.isFolder(node)).toBe(true);
    });
  });

  describe('isLink', () => {
    it('should return true when node has mimetype, url, and isUrl property', () => {
      const node: SelectableNode = {
        properties: { mimetype: 'text/html', url: 'http://x', isUrl: 'true' },
      };
      expect(component.isLink(node)).toBe(true);
    });

    it('should return false when isUrl is not true', () => {
      const node: SelectableNode = {
        properties: { mimetype: 'text/html', url: 'http://x', isUrl: 'false' },
      };
      expect(component.isLink(node)).toBe(false);
    });

    it('should return false when properties are missing', () => {
      const node: SelectableNode = {};
      expect(component.isLink(node)).toBe(false);
    });
  });

  describe('toggleSelect', () => {
    it('should select all unlocked nodes when none are selected', () => {
      fixture.detectChanges();
      const nodes: SelectableNode[] = [
        { id: '1', selected: false, properties: {} },
        { id: '2', selected: false, properties: { locked: 'true' } },
      ];
      fixture.componentRef.setInput('contents', nodes);

      component.toggleSelect();

      expect(nodes[0].selected).toBe(true);
      expect(nodes[1].selected).toBe(false);
      expect(component.allSelected()).toBe(true);
    });

    it('should deselect all when all are selected', () => {
      fixture.detectChanges();
      const nodes: SelectableNode[] = [
        { id: '1', selected: true, properties: {} },
      ];
      fixture.componentRef.setInput('contents', nodes);
      component.allSelected.set(true);

      component.toggleSelect();

      expect(nodes[0].selected).toBe(false);
      expect(component.allSelected()).toBe(false);
    });
  });

  describe('toggleSelected', () => {
    it('should toggle selection of a node', () => {
      fixture.detectChanges();
      const node: SelectableNode = { id: '1', selected: false, properties: {} };
      fixture.componentRef.setInput('contents', [node]);

      const result = component.toggleSelected(node);

      expect(result).toBe(true);
      expect(node.selected).toBe(true);
    });

    it('should return false for locked nodes', () => {
      fixture.detectChanges();
      const node: SelectableNode = {
        id: '1',
        selected: false,
        properties: { locked: 'true' },
      };
      fixture.componentRef.setInput('contents', [node]);

      const result = component.toggleSelected(node);

      expect(result).toBe(false);
    });

    it('should return false for working copy nodes', () => {
      fixture.detectChanges();
      const node: SelectableNode = {
        id: '1',
        selected: false,
        properties: { workingCopy: 'true' },
      };
      fixture.componentRef.setInput('contents', [node]);

      const result = component.toggleSelected(node);

      expect(result).toBe(false);
    });
  });

  describe('changePage', () => {
    it('should emit changedPage with new page number', () => {
      fixture.detectChanges();
      const spy = vi.spyOn(component.changedPage, 'emit');

      component.changePage(3);

      expect(spy).toHaveBeenCalledWith(3);
    });
  });

  describe('changeLimit', () => {
    it('should emit changedListing with updated limit and reset page to 1', () => {
      fixture.detectChanges();
      const spy = vi.spyOn(component.changedListing, 'emit');

      component.changeLimit(25);

      expect(component.localListingOptions.limit).toBe(25);
      expect(component.localListingOptions.page).toBe(1);
      expect(spy).toHaveBeenCalled();
    });
  });

  describe('addToClipboard', () => {
    it('should call clipboardService.addItem and set itemToClipboard', () => {
      fixture.detectChanges();
      const node: SelectableNode = { id: '1', name: 'test.pdf' };

      component.addToClipboard(node);

      expect(mockClipboardService.addItem).toHaveBeenCalledWith(node);
      expect(component.itemToClipboard).toBe('test.pdf');
    });
  });

  describe('getSelectedNodes', () => {
    it('should return ids of selected nodes', () => {
      fixture.detectChanges();
      const nodes: SelectableNode[] = [
        { id: 'a', selected: true },
        { id: 'b', selected: false },
        { id: 'c', selected: true },
      ];
      fixture.componentRef.setInput('contents', nodes);

      expect(component.getSelectedNodes()).toEqual(['a', 'c']);
    });
  });

  describe('isPublic', () => {
    it('should return true when security_ranking is undefined', () => {
      const node: SelectableNode = { properties: {} };
      expect(component.isPublic(node)).toBe(true);
    });

    it('should return true when security_ranking is PUBLIC', () => {
      const node: SelectableNode = {
        properties: { security_ranking: 'PUBLIC' },
      };
      expect(component.isPublic(node)).toBe(true);
    });

    it('should return false when security_ranking is SENSITIVE', () => {
      const node: SelectableNode = {
        properties: { security_ranking: 'SENSITIVE' },
      };
      expect(component.isPublic(node)).toBe(false);
    });
  });

  describe('isGuest', () => {
    it('should delegate to loginService.isGuest', () => {
      fixture.detectChanges();
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
      mockLoginService.isGuest.mockReturnValue(false);
    });
  });

  describe('isSubscribedToNotifications (resource)', () => {
    it('should load the subscription state for the parent node', async () => {
      mockNotificationService.isUserSubscribedAsync.mockResolvedValue({
        subscribed: true,
      });

      fixture.detectChanges();
      await fixture.whenStable();

      expect(
        mockNotificationService.isUserSubscribedAsync
      ).toHaveBeenCalledWith({ id: 'parent-1', userId: 'testUser' });
      expect(component.isSubscribedToNotifications()).toBe(true);
    });

    it('should not query the service for a guest user', async () => {
      mockNotificationService.isUserSubscribedAsync.mockClear();
      mockLoginService.isGuest.mockReturnValue(true);

      fixture.detectChanges();
      await fixture.whenStable();

      expect(
        mockNotificationService.isUserSubscribedAsync
      ).not.toHaveBeenCalled();
      expect(component.isSubscribedToNotifications()).toBe(false);

      mockLoginService.isGuest.mockReturnValue(false);
    });

    it('should reload the subscription state after changing it', async () => {
      mockNotificationService.isUserSubscribedAsync.mockResolvedValue({
        subscribed: false,
      });
      fixture.detectChanges();
      await fixture.whenStable();

      mockNotificationService.isUserSubscribedAsync.mockResolvedValue({
        subscribed: true,
      });
      await component.changeNotificationSubscription('on');
      await fixture.whenStable();
      fixture.detectChanges();
      await fixture.whenStable();

      expect(
        mockNotificationService.putNotificationAuthorityAsync
      ).toHaveBeenCalledWith({
        id: 'parent-1',
        authority: 'testUser',
        body: 'on',
      });
      expect(component.isSubscribedToNotifications()).toBe(true);
    });
  });

  describe('locked', () => {
    it('should return true when locked property is true', () => {
      const node: SelectableNode = { properties: { locked: 'true' } };
      expect(component.locked(node)).toBe(true);
    });

    it('should return false when locked property is not set', () => {
      const node: SelectableNode = { properties: {} };
      expect(component.locked(node)).toBe(false);
    });
  });

  describe('workingCopy', () => {
    it('should return true when workingCopy property is true', () => {
      const node: SelectableNode = { properties: { workingCopy: 'true' } };
      expect(component.workingCopy(node)).toBe(true);
    });

    it('should return false when workingCopy property is not set', () => {
      const node: SelectableNode = { properties: {} };
      expect(component.workingCopy(node)).toBe(false);
    });
  });

  describe('hasExpirationDate', () => {
    it('should return true when expiration_date is set', () => {
      const node: SelectableNode = {
        properties: { expiration_date: '2026-12-31' },
      };
      expect(component.hasExpirationDate(node)).toBe(true);
    });

    it('should return false when expiration_date is null string', () => {
      const node: SelectableNode = { properties: { expiration_date: 'null' } };
      expect(component.hasExpirationDate(node)).toBe(false);
    });

    it('should return false when expiration_date is undefined', () => {
      const node: SelectableNode = { properties: {} };
      expect(component.hasExpirationDate(node)).toBe(false);
    });
  });

  describe('getDestinationId', () => {
    it('should return the 4th segment of a slash-separated string', () => {
      expect(component.getDestinationId('a/b/c/dest-id')).toBe('dest-id');
    });

    it('should return empty string for invalid format', () => {
      expect(component.getDestinationId('a/b')).toBe('');
    });
  });

  describe('getColumnCount', () => {
    it('should count enabled columns plus base count of 3', () => {
      fixture.detectChanges();
      const count = component.getColumnCount();
      // Based on default preferences: name, title, version, modification, size, expiration, description = 7 true + 3 base = 10
      expect(count).toBe(10);
    });
  });

  describe('isSensitive', () => {
    it('should return true for SENSITIVE ranking', () => {
      const node: SelectableNode = {
        properties: { security_ranking: 'SENSITIVE' },
      };
      expect(component.isSensitive(node)).toBe(true);
    });

    it('should return true for SPECIAL_HANDLING ranking', () => {
      const node: SelectableNode = {
        properties: { security_ranking: 'SPECIAL_HANDLING' },
      };
      expect(component.isSensitive(node)).toBe(true);
    });

    it('should return false for PUBLIC ranking', () => {
      const node: SelectableNode = {
        properties: { security_ranking: 'PUBLIC' },
      };
      expect(component.isSensitive(node)).toBe(false);
    });
  });

  describe('getTranslationsCount', () => {
    it('should return translations minus 1', () => {
      const node: SelectableNode = { properties: { translations: '3' } };
      expect(component.getTranslationsCount(node)).toBe('2');
    });

    it('should return 0 when translations is undefined', () => {
      const node: SelectableNode = { properties: {} };
      expect(component.getTranslationsCount(node)).toBe('0');
    });
  });

  describe('versionLabel', () => {
    it('should return versionLabel from properties', () => {
      const node: SelectableNode = { properties: { versionLabel: '1.2' } };
      expect(component.versionLabel(node)).toBe('1.2');
    });

    it('should return empty string when no properties', () => {
      const node: SelectableNode = {};
      expect(component.versionLabel(node)).toBe('');
    });
  });

  describe('toggleTreeView', () => {
    it('should toggle treeView and emit change', () => {
      fixture.detectChanges();
      const spy = vi.spyOn(component.treeViewChange, 'emit');

      component.toggleTreeView();

      expect(component.treeView()).toBe(true);
      expect(spy).toHaveBeenCalledWith(true);
    });
  });

  describe('areNodesDeletable', () => {
    it('should return true when all selected nodes have delete permissions', () => {
      fixture.detectChanges();
      const nodes: SelectableNode[] = [{ id: '1', selected: true }];
      fixture.componentRef.setInput('contents', nodes);
      mockPermissionEvaluator.hasAnyOfPermissions.mockReturnValue(true);

      expect(component.areNodesDeletable()).toBe(true);
    });

    it('should return false when a selected node lacks permissions', () => {
      fixture.detectChanges();
      const nodes: SelectableNode[] = [{ id: '1', selected: true }];
      fixture.componentRef.setInput('contents', nodes);
      mockPermissionEvaluator.hasAnyOfPermissions.mockReturnValue(false);

      expect(component.areNodesDeletable()).toBe(false);
    });
  });
});
