import { I18nSelectPipe, Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AlfrescoService } from 'app/core/alfresco.service';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ContentService,
  DynamicPropertiesService,
  GuardsService,
  KeywordsService,
  NodesService,
  NotificationService,
  SpaceService,
  TopicService,
} from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DetailsComponent } from './details.component';
import { SummarizeService } from './summarize/summarize.service';

describe('DetailsComponent', () => {
  let component: DetailsComponent;
  let fixture: ComponentFixture<DetailsComponent>;

  const mockNodesService = {
    getNode: vi.fn().mockReturnValue(of({})),
    getGroupAsync: vi.fn().mockResolvedValue({ id: 'g1', libraryId: 'lib1' }),
    putOwnershipAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockContentService = {
    getFirstVersionsAsync: vi.fn().mockResolvedValue([]),
    getVersionsAsync: vi.fn().mockResolvedValue([]),
    getTranslationsAsync: vi.fn().mockResolvedValue({}),
    getTopicsAsync: vi.fn().mockResolvedValue([]),
    postCheckoutAsync: vi.fn().mockResolvedValue(undefined),
    deleteCheckoutAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockKeywordsService = {
    getKeywordsAsync: vi.fn().mockResolvedValue([]),
  };
  const mockDynamicPropertiesService = {
    getDynamicPropertyDefinitionsAsync: vi.fn().mockResolvedValue([]),
  };
  const mockTopicService = {
    getRepliesAsync: vi.fn().mockResolvedValue({ data: [], total: 0 }),
  };
  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({ userId: 'user1' }),
    getTicket: vi.fn().mockReturnValue('ticket123'),
    getCurrentUsername: vi.fn().mockReturnValue('user1'),
  };
  const mockPermEvalService = {
    isLibManageOwnOrHigher: vi.fn().mockReturnValue(false),
    isLibAdmin: vi.fn().mockReturnValue(false),
    isLibAccess: vi.fn().mockReturnValue(false),
    isLibFullEdit: vi.fn().mockReturnValue(false),
  };
  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
    addInfoMessage: vi.fn(),
  };
  const mockClipboardService = {
    addItem: vi.fn(),
    removeItem: vi.fn(),
  };
  const mockSaveAsService = { saveAs: vi.fn() };
  const mockAresBridgeHelperService = {
    isAresBridgeEnabled: vi.fn().mockResolvedValue(false),
    nodeLog: vi.fn().mockResolvedValue([]),
    sendToAresBridge: vi.fn().mockResolvedValue(undefined),
  };
  const mockGuardsService = {
    getGuardAccessAsync: vi.fn().mockResolvedValue({ granted: true }),
  };
  const mockNotificationService = {
    putNotificationAuthorityAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockSpaceService = {
    getFolderSizeAsync: vi.fn().mockResolvedValue({ code: 1024 }),
  };
  const mockLibraryIdService = { updateLibraryId: vi.fn() };
  const mockAlfrescoService = {
    getRendition: vi.fn().mockResolvedValue({ entry: { status: 'CREATED' } }),
    createRendition: vi.fn().mockResolvedValue(undefined),
  };
  const mockSummarizeService = {
    isEnabled: false,
    summarize: vi.fn().mockResolvedValue({ summary: 'test' }),
  };
  const mockRouter = {
    navigate: vi.fn().mockResolvedValue(true),
    url: '/group/g1/library/n1/details',
  };
  const mockLocation = { back: vi.fn() };
  const mockDialog = { open: vi.fn() };
  const mockI18nPipe = { transform: vi.fn().mockReturnValue('') };
  const mockI18nSelectPipe = { transform: vi.fn().mockReturnValue('') };
  const mockRoute = {
    data: of({ group: { id: 'g1', name: 'Group1', libraryId: 'lib1' } }),
    params: of({ nodeId: 'node1' }),
    queryParams: of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailsComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: ContentService, useValue: mockContentService },
        { provide: KeywordsService, useValue: mockKeywordsService },
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        { provide: TopicService, useValue: mockTopicService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: ClipboardService, useValue: mockClipboardService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        {
          provide: AresBridgeHelperService,
          useValue: mockAresBridgeHelperService,
        },
        { provide: GuardsService, useValue: mockGuardsService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: LibraryIdService, useValue: mockLibraryIdService },
        { provide: AlfrescoService, useValue: mockAlfrescoService },
        { provide: SummarizeService, useValue: mockSummarizeService },
        { provide: Router, useValue: mockRouter },
        { provide: Location, useValue: mockLocation },
        { provide: MatDialog, useValue: mockDialog },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: I18nSelectPipe, useValue: mockI18nSelectPipe },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  describe('isFile', () => {
    it('should return true when node type does not include folder', () => {
      component.node = { type: 'content' };
      expect(component.isFile()).toBe(true);
    });

    it('should return false when node type includes folder', () => {
      component.node = { type: 'folder' };
      expect(component.isFile()).toBe(false);
    });

    it('should return false when node type is undefined', () => {
      component.node = {};
      expect(component.isFile()).toBe(false);
    });
  });

  describe('isLink', () => {
    it('should return true when node name ends with .url', () => {
      component.node = { type: 'content', name: 'link.url', properties: {} };
      expect(component.isLink()).toBe(true);
    });

    it('should return true when properties.isUrl is true', () => {
      component.node = {
        type: 'content',
        name: 'file.txt',
        properties: { isUrl: 'true' },
      };
      expect(component.isLink()).toBe(true);
    });

    it('should return false when not a url', () => {
      component.node = {
        type: 'content',
        name: 'file.txt',
        properties: { isUrl: 'false' },
      };
      expect(component.isLink()).toBe(false);
    });

    it('should return false when node has no properties', () => {
      component.node = { type: 'content', name: 'file.txt' };
      expect(component.isLink()).toBe(false);
    });
  });

  describe('isLastVersionOfContent', () => {
    it('should return true when versionLabel is undefined', () => {
      component.versionLabel = undefined as unknown as string;
      expect(component.isLastVersionOfContent()).toBe(true);
    });

    it('should return true when versionLabel matches first version', () => {
      component.versionLabel = '2.0';
      component.versions = [{ versionLabel: '2.0' }, { versionLabel: '1.0' }];
      expect(component.isLastVersionOfContent()).toBe(true);
    });

    it('should return false when versionLabel does not match first version', () => {
      component.versionLabel = '1.0';
      component.versions = [{ versionLabel: '2.0' }, { versionLabel: '1.0' }];
      expect(component.isLastVersionOfContent()).toBe(false);
    });
  });

  describe('isLocked', () => {
    it('should return true when locked property is true', () => {
      component.node = { properties: { locked: 'true' } };
      expect(component.isLocked()).toBe(true);
    });

    it('should return false when locked property is not true', () => {
      component.node = { properties: { locked: 'false' } };
      expect(component.isLocked()).toBe(false);
    });

    it('should return false when node has no properties', () => {
      component.node = {};
      expect(component.isLocked()).toBe(false);
    });
  });

  describe('isWorkingCopy', () => {
    it('should return true when workingCopy property is true', () => {
      component.node = { properties: { workingCopy: 'true' } };
      expect(component.isWorkingCopy()).toBe(true);
    });

    it('should return false when workingCopy property is not true', () => {
      component.node = { properties: { workingCopy: 'false' } };
      expect(component.isWorkingCopy()).toBe(false);
    });
  });

  describe('isPublic', () => {
    it('should return true when security_ranking is PUBLIC', () => {
      component.node = { properties: { security_ranking: 'PUBLIC' } };
      expect(component.isPublic()).toBe(true);
    });

    it('should return true when security_ranking is undefined', () => {
      component.node = { properties: {} };
      expect(component.isPublic()).toBe(true);
    });

    it('should return false when security_ranking is SENSITIVE', () => {
      component.node = { properties: { security_ranking: 'SENSITIVE' } };
      expect(component.isPublic()).toBe(false);
    });

    it('should return false when node has no properties', () => {
      component.node = {};
      expect(component.isPublic()).toBe(false);
    });
  });

  describe('isSensitive', () => {
    it('should return true for SENSITIVE ranking', () => {
      component.node = { properties: { security_ranking: 'SENSITIVE' } };
      expect(component.isSensitive()).toBe(true);
    });

    it('should return true for SPECIAL_HANDLING ranking', () => {
      component.node = { properties: { security_ranking: 'SPECIAL_HANDLING' } };
      expect(component.isSensitive()).toBe(true);
    });

    it('should return false for PUBLIC ranking', () => {
      component.node = { properties: { security_ranking: 'PUBLIC' } };
      expect(component.isSensitive()).toBe(false);
    });
  });

  describe('getDestinationId', () => {
    it('should extract id from 4-part destination path', () => {
      expect(
        component.getDestinationId('workspace/SpacesStore/abc/dest123')
      ).toBe('dest123');
    });

    it('should return empty string for non-4-part path', () => {
      expect(component.getDestinationId('workspace/SpacesStore')).toBe('');
    });
  });

  describe('extractDestinationId', () => {
    it('should set destinationId for filelink node', () => {
      component.node = {
        type: 'filelink',
        properties: { destination: 'workspace/SpacesStore/abc/dest1' },
      };
      component.extractDestinationId();
      expect(component.destinationId).toBe('dest1');
    });

    it('should set empty destinationId for non-filelink node', () => {
      component.node = { type: 'content', properties: {} };
      component.extractDestinationId();
      expect(component.destinationId).toBe('');
    });
  });

  describe('getPageTitle', () => {
    it('should return url label for link', () => {
      component.node = { type: 'content', name: 'link.url', properties: {} };
      expect(component.getPageTitle()).toBe('label.library.details.of.url');
    });

    it('should return document label for file', () => {
      component.node = {
        type: 'content',
        name: 'doc.pdf',
        properties: { isUrl: 'false' },
      };
      expect(component.getPageTitle()).toBe(
        'label.library.details.of.document'
      );
    });

    it('should return folder label for folder', () => {
      component.node = { type: 'folder' };
      expect(component.getPageTitle()).toBe('label.library.details.of.folder');
    });
  });

  describe('hasComments', () => {
    it('should return true when comments total > 0', () => {
      component.comments = { data: [{ id: '1' }], total: 1 };
      expect(component.hasComments()).toBe(true);
    });

    it('should return false when comments total is 0', () => {
      component.comments = { data: [], total: 0 };
      expect(component.hasComments()).toBe(false);
    });
  });

  describe('hasTopics', () => {
    it('should return true when topics exist', () => {
      component.topics = [{ id: '1' }];
      expect(component.hasTopics()).toBe(true);
    });

    it('should return false when topics is empty', () => {
      component.topics = [];
      expect(component.hasTopics()).toBe(false);
    });
  });

  describe('addToClipboard', () => {
    it('should add node to clipboard service', () => {
      component.node = { id: 'n1', name: 'file.txt' };
      component.group = { id: 'g1' };
      component.addToClipboard();
      expect(mockClipboardService.addItem).toHaveBeenCalledWith(
        { id: 'n1', name: 'file.txt' },
        true,
        'g1'
      );
      expect(component.itemToClipboard).toBe('file.txt');
    });
  });

  describe('isGuest', () => {
    it('should delegate to loginService.isGuest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });
  });

  describe('isLibAdmin', () => {
    it('should delegate to permEvalService', () => {
      component.node = { id: 'n1' };
      mockPermEvalService.isLibAdmin.mockReturnValue(true);
      expect(component.isLibAdmin()).toBe(true);
      expect(mockPermEvalService.isLibAdmin).toHaveBeenCalledWith({ id: 'n1' });
    });
  });

  describe('property getters', () => {
    it('should return securityRanking from properties', () => {
      component.node = { properties: { security_ranking: 'PUBLIC' } };
      expect(component.securityRanking).toBe('PUBLIC');
    });

    it('should return empty string when no properties for securityRanking', () => {
      component.node = {};
      expect(component.securityRanking).toBe('');
    });

    it('should return size from properties', () => {
      component.node = { properties: { size: '1024' } };
      expect(component.size).toBe('1024');
    });

    it('should return null when no properties for size', () => {
      component.node = {};
      expect(component.size).toBeNull();
    });

    it('should return created from properties', () => {
      component.node = { properties: { created: '2024-01-01' } };
      expect(component.created).toBe('2024-01-01');
    });

    it('should return modified from properties', () => {
      component.node = { properties: { modified: '2024-02-01' } };
      expect(component.modified).toBe('2024-02-01');
    });

    it('should return modifier from properties', () => {
      component.node = { properties: { modifier: 'admin' } };
      expect(component.modifier).toBe('admin');
    });

    it('should return workingCopyId from properties', () => {
      component.node = { properties: { workingCopyId: 'wc1' } };
      expect(component.workingCopyId).toBe('wc1');
    });

    it('should return empty string for workingCopyId when no properties', () => {
      component.node = {};
      expect(component.workingCopyId).toBe('');
    });
  });

  describe('humanReadableSelectionDynProp', () => {
    it('should parse array format', () => {
      expect(component.humanReadableSelectionDynProp('["a", "b"]')).toBe(
        '"b, a'
      );
    });

    it('should return value as-is when not array format', () => {
      expect(component.humanReadableSelectionDynProp('simple')).toBe('simple');
    });

    it('should return empty string for falsy value', () => {
      expect(component.humanReadableSelectionDynProp('')).toBe('');
    });
  });

  describe('isDateField', () => {
    it('should return true for DATE_FIELD type', () => {
      expect(
        component.isDateField({ propertyType: 'DATE_FIELD' } as never)
      ).toBe(true);
    });

    it('should return false for other types', () => {
      expect(
        component.isDateField({ propertyType: 'TEXT_FIELD' } as never)
      ).toBe(false);
    });
  });

  describe('isSelectionField', () => {
    it('should return true for SELECTION type', () => {
      expect(
        component.isSelectionField({ propertyType: 'SELECTION' } as never)
      ).toBe(true);
    });

    it('should return true for MULTI_SELECTION type', () => {
      expect(
        component.isSelectionField({ propertyType: 'MULTI_SELECTION' } as never)
      ).toBe(true);
    });

    it('should return false for other types', () => {
      expect(
        component.isSelectionField({ propertyType: 'TEXT_FIELD' } as never)
      ).toBe(false);
    });
  });

  describe('canPreviousCommentPage', () => {
    it('should return true when page > 1', () => {
      component.listingCommentOptions.page = 2;
      expect(component.canPreviousCommentPage()).toBe(true);
    });

    it('should return false when page is 1', () => {
      component.listingCommentOptions.page = 1;
      expect(component.canPreviousCommentPage()).toBe(false);
    });
  });

  describe('canNextCommentPage', () => {
    it('should return true when more pages available', () => {
      component.listingCommentOptions.page = 1;
      component.listingCommentOptions.limit = 10;
      component.totalCommentItems = 25;
      expect(component.canNextCommentPage()).toBe(true);
    });

    it('should return false when on last page', () => {
      component.listingCommentOptions.page = 3;
      component.listingCommentOptions.limit = 10;
      component.totalCommentItems = 25;
      expect(component.canNextCommentPage()).toBe(false);
    });
  });

  describe('goBack', () => {
    it('should call location.back', () => {
      component.goBack();
      expect(mockLocation.back).toHaveBeenCalled();
    });
  });

  describe('isAiEnabled', () => {
    it('should return summarizeService.isEnabled', () => {
      expect(component.isAiEnabled()).toBe(false);
    });
  });

  describe('isCurrentOwner', () => {
    it('should return true when owner matches current user', () => {
      component.node = { properties: { owner: 'user1' } };
      component.user = { userId: 'user1' };
      expect(component.isCurrentOwner()).toBe(true);
    });

    it('should return false when owner does not match', () => {
      component.node = { properties: { owner: 'other' } };
      component.user = { userId: 'user1' };
      expect(component.isCurrentOwner()).toBe(false);
    });

    it('should return false when no properties', () => {
      component.node = {};
      component.user = { userId: 'user1' };
      expect(component.isCurrentOwner()).toBe(false);
    });
  });

  describe('isSubscribedToNotifications', () => {
    it('should return true when notifications is ALLOWED', () => {
      component.node = { notifications: 'ALLOWED' };
      expect(component.isSubscribedToNotifications()).toBe(true);
    });

    it('should return false when notifications is not ALLOWED', () => {
      component.node = { notifications: 'INHIBITED' };
      expect(component.isSubscribedToNotifications()).toBe(false);
    });
  });

  describe('getCommentPages', () => {
    it('should return correct page numbers', () => {
      component.totalCommentItems = 25;
      component.listingCommentOptions.limit = 10;
      expect(component.getCommentPages()).toEqual([1, 2, 3]);
    });

    it('should return single page for small total', () => {
      component.totalCommentItems = 5;
      component.listingCommentOptions.limit = 10;
      expect(component.getCommentPages()).toEqual([1]);
    });
  });
});
