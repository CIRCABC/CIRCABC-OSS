import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  NodesService,
  PagedNodes,
  SpaceService,
  UserService,
} from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { CookieService } from 'ngx-cookie-service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { LibraryComponent } from './library.component';

const mockPagedNodes: PagedNodes = { data: [], total: 0 };

const mockNode: ModelNode = {
  id: 'node1',
  name: 'TestFolder',
  type: 'folder',
  parentId: 'group1',
};

describe('LibraryComponent', () => {
  let component: LibraryComponent;
  let fixture: ComponentFixture<LibraryComponent>;
  let paramsSubject: Subject<Record<string, string>>;
  let queryParamsSubject: Subject<Record<string, string>>;
  let dataSubject: Subject<Record<string, unknown>>;

  const mockSpaceService = {
    getChildrenAsync: vi.fn().mockResolvedValue(mockPagedNodes),
    getRestrictedChildrenAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
    getGroupAsync: vi.fn().mockResolvedValue({
      id: 'group1',
      name: 'TestGroup',
      permissions: { library: 'LibAdmin' },
    }),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
    addSuccessMessage: vi.fn(),
  };

  const mockTranslocoService = {
    translate: vi.fn().mockReturnValue('translated'),
    getActiveLang: vi.fn().mockReturnValue('en'),
    config: { reRenderOnLangChange: false, defaultLang: 'en' },
    langChanges$: of('en'),
    selectTranslation: vi.fn().mockReturnValue(of({})),
    selectTranslate: vi.fn().mockReturnValue(of('')),
    load: vi.fn().mockReturnValue(of({})),
    _loadDependencies: vi.fn().mockReturnValue(of([])),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({
      userId: 'user1',
      firstname: 'Test',
      lastname: 'User',
    }),
  };

  const mockPermEvalService = {
    isLibAdmin: vi.fn().mockReturnValue(false),
    isLibManageOwnOrHigher: vi.fn().mockReturnValue(false),
  };

  const mockUserService = {
    saveUserPreferencesAsync: vi.fn().mockResolvedValue({}),
  };

  const mockCookieService = {
    check: vi.fn().mockReturnValue(false),
    get: vi.fn().mockReturnValue(''),
  };

  const mockLibraryIdService = {
    updateLibraryId: vi.fn(),
  };

  const mockUserPreferencesService = {
    waitForPreferences: vi.fn().mockResolvedValue(undefined),
  };

  const mockRouter = {
    navigate: vi.fn().mockResolvedValue(true),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();
    queryParamsSubject = new Subject();
    dataSubject = new Subject();

    const mockRoute = {
      data: dataSubject.asObservable(),
      params: paramsSubject.asObservable(),
      queryParams: queryParamsSubject.asObservable(),
      snapshot: { queryParams: {} },
    };

    await TestBed.configureTestingModule({
      imports: [LibraryComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UserService, useValue: mockUserService },
        { provide: CookieService, useValue: mockCookieService },
        { provide: LibraryIdService, useValue: mockLibraryIdService },
        {
          provide: UserPreferencesService,
          useValue: mockUserPreferencesService,
        },
      ],
    })
      // The full template renders several heavy child components (add-dropdown,
      // breadcrumb, folder-tree-view, clipboard, library-browser) that each
      // pull in unrelated services (ClipboardService, SaveAsService, MatDialog,
      // ActionService, ...). This spec exercises the component's data-loading
      // logic (route -> resources -> public signals), not that downstream view
      // tree, so the template is overridden with a minimal stand-in that still
      // exercises the `ready`/`loading` gates without instantiating children.
      .overrideComponent(LibraryComponent, {
        set: { template: '<div>{{ ready() }}{{ loading() }}</div>' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(LibraryComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  /** Drives the route params/data subjects and settles all pending resources. */
  async function initComponent(
    nodeId = 'node1',
    group: Record<string, unknown> = {
      id: 'group1',
      name: 'TestGroup',
      permissions: { library: 'LibAdmin' },
    },
    queryParams: Record<string, string> = {}
  ) {
    fixture.detectChanges();
    dataSubject.next({ group });
    paramsSubject.next({ nodeId });
    queryParamsSubject.next(queryParams);
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('isFile', () => {
    it('should return true for non-folder type', () => {
      expect(component.isFile({ type: 'content' })).toBe(true);
    });

    it('should return false for folder type', () => {
      expect(component.isFile({ type: 'folder' })).toBe(false);
    });

    it('should return false when type is undefined', () => {
      expect(component.isFile({})).toBe(false);
    });
  });

  describe('isFolder', () => {
    it('should return true for folder type', () => {
      expect(component.isFolder({ type: 'folder' })).toBe(true);
    });

    it('should return false for non-folder type', () => {
      expect(component.isFolder({ type: 'content' })).toBe(false);
    });

    it('should return false when type is undefined', () => {
      expect(component.isFolder({})).toBe(false);
    });
  });

  describe('isLibraryRoot', () => {
    it('should return true when node name is Library', async () => {
      mockNodesService.getNodeAsync.mockResolvedValueOnce({ name: 'Library' });
      await initComponent();
      expect(component.isLibraryRoot()).toBe(true);
    });

    it('should return false when node name is not Library', async () => {
      await initComponent();
      expect(component.isLibraryRoot()).toBe(false);
    });
  });

  describe('isGroupAdmin', () => {
    it('should return false when group permissions are undefined', async () => {
      await initComponent('node1', { name: 'Test', permissions: {} });
      expect(component.isGroupAdmin()).toBe(false);
    });

    it('should return true when all admin permissions are set', async () => {
      await initComponent('node1', {
        name: 'Test',
        permissions: {
          library: 'LibAdmin',
          directory: 'DirAdmin',
          information: 'InfAdmin',
          newsgroup: 'NwsAdmin',
          event: 'EveAdmin',
        },
      });
      expect(component.isGroupAdmin()).toBe(true);
    });

    it('should return false when not all admin permissions are set', async () => {
      await initComponent('node1', {
        name: 'Test',
        permissions: {
          library: 'LibAdmin',
          directory: 'DirAccess',
          information: 'InfAdmin',
          newsgroup: 'NwsAdmin',
          event: 'EveAdmin',
        },
      });
      expect(component.isGroupAdmin()).toBe(false);
    });
  });

  describe('isLibAdmin', () => {
    it('should return true when group library permission is LibAdmin', async () => {
      await initComponent('node1', {
        name: 'Test',
        permissions: { library: 'LibAdmin' },
      });
      expect(component.isLibAdmin()).toBe(true);
    });

    it('should delegate to permEvalService when group permission is not LibAdmin', async () => {
      mockPermEvalService.isLibAdmin.mockReturnValue(true);
      await initComponent('node1', {
        name: 'Test',
        permissions: { library: 'LibAccess' },
      });
      expect(component.isLibAdmin()).toBe(true);
      expect(mockPermEvalService.isLibAdmin).toHaveBeenCalledWith(
        component.node()
      );
    });
  });

  describe('isLibManageOwn', () => {
    it('should delegate to permEvalService', async () => {
      mockPermEvalService.isLibManageOwnOrHigher.mockReturnValue(true);
      await initComponent('node1', {
        name: 'Test',
        permissions: { library: 'LibAccess' },
      });
      expect(component.isLibManageOwn()).toBe(true);
      expect(mockPermEvalService.isLibManageOwnOrHigher).toHaveBeenCalledWith(
        component.node()
      );
    });
  });

  describe('changePage', () => {
    it('should update listing page and navigate', async () => {
      await initComponent();
      await component.changePage(3);
      expect(component.preferences().library.listing.page).toBe(3);
      expect(mockRouter.navigate).toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    it('should show success message on upload file success', async () => {
      await initComponent();
      component.refresh({
        result: ActionResult.SUCCEED,
        type: ActionType.UPLOAD_FILE,
      });
      expect(mockTranslocoService.translate).toHaveBeenCalled();
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });

    it('should reload content even on non-success result', async () => {
      await initComponent();
      mockSpaceService.getChildrenAsync.mockClear();
      component.refresh({ result: ActionResult.CANCELED });
      await fixture.whenStable();
      expect(mockSpaceService.getChildrenAsync).toHaveBeenCalled();
    });
  });

  describe('content loading', () => {
    it('should load children via spaceService', async () => {
      await initComponent();
      expect(mockSpaceService.getChildrenAsync).toHaveBeenCalled();
      expect(component.currentContents()).toEqual(mockPagedNodes);
      expect(component.loading()).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      mockSpaceService.getChildrenAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      await initComponent();
      expect(component.currentContents()).toEqual({ data: [], total: 0 });
    });
  });

  describe('clipboardSidebarClosed', () => {
    it('should set clipboardOpen to false', () => {
      component.clipboardOpen = true;
      component.clipboardSidebarClosed();
      expect(component.clipboardOpen).toBe(false);
    });
  });

  describe('itemsInClipboard', () => {
    it('should update amountOfItemsInClipboard asynchronously', () => {
      vi.useFakeTimers();
      component.itemsInClipboard(5);
      vi.runAllTimers();
      expect(component.amountOfItemsInClipboard).toBe(5);
      vi.useRealTimers();
    });
  });
});
