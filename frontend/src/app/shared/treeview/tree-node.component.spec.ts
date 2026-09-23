import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  ForumService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { I18nService } from 'app/shared/services/i18n.service';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeNodeComponent } from 'app/shared/treeview/tree-node.component';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

describe('TreeNodeComponent', () => {
  let component: TreeNodeComponent;
  let componentRef: ComponentRef<TreeNodeComponent>;
  let fixture: ComponentFixture<TreeNodeComponent>;

  const langChanges$ = new Subject<string>();

  const mockSpaceService = {
    getSubspacesAsync: vi.fn().mockResolvedValue([]),
  };

  const mockForumService = {
    getSubforumsAsync: vi.fn().mockResolvedValue([]),
  };

  const mockI18nService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
    getDefaultLang: vi.fn().mockReturnValue('en'),
    getLangChanges$: vi.fn().mockReturnValue(langChanges$.asObservable()),
  };

  const mockI18nPipe = {
    transform: vi.fn((mltext: { [key: string]: string } | undefined) => {
      if (!mltext) return '';
      return mltext['en'] ?? Object.values(mltext)[0] ?? '';
    }),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TreeNodeComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ForumService, useValue: mockForumService },
        { provide: I18nService, useValue: mockI18nService },
        { provide: I18nPipe, useValue: mockI18nPipe },
      ],
    })
      .overrideComponent(TreeNodeComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(TreeNodeComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    const rootNode = new TreeNode('Root', 'root-id');
    componentRef.setInput('node', rootNode);
    componentRef.setInput('rootId', 'root-id');

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('canBeExpanded', () => {
    it('should return false when node has no children and no subfolders', () => {
      expect(component.canBeExpanded()).toBe(false);
    });

    it('should return true when node has children', () => {
      component.node().children = [new TreeNode('Child', 'child-id')];
      expect(component.canBeExpanded()).toBe(true);
    });

    it('should return true when node has subfolders flag', () => {
      component.node().hasSubFolders = true;
      expect(component.canBeExpanded()).toBe(true);
    });
  });

  describe('markSelected', () => {
    it('should return false when searchedNodeId is undefined', () => {
      expect(component.markSelected()).toBe(false);
    });

    it('should return true when searchedNodeId matches node id', () => {
      componentRef.setInput('searchedNodeId', 'root-id');
      fixture.detectChanges();
      expect(component.markSelected()).toBe(true);
    });

    it('should return false when searchedNodeId does not match', () => {
      componentRef.setInput('searchedNodeId', 'other-id');
      fixture.detectChanges();
      expect(component.markSelected()).toBe(false);
    });
  });

  describe('isNew', () => {
    it('should return false when flagNewDays is -1', () => {
      expect(component.isNew()).toBe(false);
    });

    it('should return true when node was created within flagNewDays', () => {
      component.node().created = new Date();
      componentRef.setInput('flagNewDays', 7);
      fixture.detectChanges();
      expect(component.isNew()).toBe(true);
    });

    it('should return false when node was created before flagNewDays', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 30);
      component.node().created = oldDate;
      componentRef.setInput('flagNewDays', 7);
      fixture.detectChanges();
      expect(component.isNew()).toBe(false);
    });
  });

  describe('selectNode', () => {
    it('should emit selectedNodeEmitter', () => {
      const emitSpy = vi.spyOn(component.selectedNodeEmitter, 'emit');
      const node = new TreeNode('Test', 'test-id');
      component.selectNode(node);
      expect(emitSpy).toHaveBeenCalledWith(node);
    });
  });

  describe('clickNode', () => {
    it('should emit selectedNodeEmitter when showSelector is true', async () => {
      const emitSpy = vi.spyOn(component.selectedNodeEmitter, 'emit');
      const node = new TreeNode('Test', 'test-id');
      await component.clickNode(node);
      expect(emitSpy).toHaveBeenCalledWith(node);
    });

    it('should emit clickedNodeEmitter when showSelector is false', async () => {
      componentRef.setInput('showSelector', false);
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.clickedNodeEmitter, 'emit');
      const node = new TreeNode('Test', 'test-id');
      await component.clickNode(node);
      expect(emitSpy).toHaveBeenCalledWith(node);
    });
  });

  describe('loadChildren', () => {
    it('should call spaceService.getSubspaces for library service', async () => {
      const spaces: ModelNode[] = [
        { id: 'space-1', name: 'Space 1', hasSubFolders: false },
        { id: 'space-2', name: 'Space 2', hasSubFolders: true },
      ];
      mockSpaceService.getSubspacesAsync.mockResolvedValue(spaces);

      await component.loadChildren();

      expect(mockSpaceService.getSubspacesAsync).toHaveBeenCalledWith({
        id: 'root-id',
        language: '',
        sort: 'title',
        order: 'ASC',
        skipExpiredItems: true,
      });
      expect(component.node().children).toHaveLength(2);
      expect(component.loading()).toBe(false);
    });

    it('should call forumService.getSubforums for newsgroups service', async () => {
      componentRef.setInput('service', 'newsgroups');
      fixture.detectChanges();

      const forums: ModelNode[] = [
        { id: 'forum-1', name: 'Forum 1', hasSubFolders: false },
      ];
      mockForumService.getSubforumsAsync.mockResolvedValue(forums);

      await component.loadChildren();

      expect(mockForumService.getSubforumsAsync).toHaveBeenCalledWith({
        id: 'root-id',
        sort: 'title',
        order: 'ASC',
      });
      expect(component.node().children).toHaveLength(1);
    });

    it('should use title from i18nPipe when available', async () => {
      const spaces: ModelNode[] = [
        { id: 'space-1', name: 'FallbackName', title: { en: 'English Title' } },
      ];
      mockSpaceService.getSubspacesAsync.mockResolvedValue(spaces);

      await component.loadChildren();

      expect(component.node().children[0].name).toBe('English Title');
    });

    it('should fall back to name when title is undefined', async () => {
      const spaces: ModelNode[] = [{ id: 'space-1', name: 'FallbackName' }];
      mockSpaceService.getSubspacesAsync.mockResolvedValue(spaces);

      await component.loadChildren();

      expect(component.node().children[0].name).toBe('FallbackName');
    });

    it('should deduplicate children by id', async () => {
      const spaces: ModelNode[] = [
        { id: 'space-1', name: 'Space 1' },
        { id: 'space-1', name: 'Space 1 Duplicate' },
      ];
      mockSpaceService.getSubspacesAsync.mockResolvedValue(spaces);

      await component.loadChildren();

      expect(component.node().children).toHaveLength(1);
    });
  });

  describe('toggleExpand', () => {
    it('should load children and toggle expanded state', async () => {
      mockSpaceService.getSubspacesAsync.mockResolvedValue([]);
      expect(component.node().expanded).toBe(false);

      await component.toggleExpand();

      expect(component.node().expanded).toBe(true);
    });
  });

  describe('reload', () => {
    it('should load children when rootId matches node id', async () => {
      mockSpaceService.getSubspacesAsync.mockResolvedValue([]);

      await component.reload();

      expect(mockSpaceService.getSubspacesAsync).toHaveBeenCalled();
    });

    it('should load and expand when displayedPath contains node id', async () => {
      const path: ModelNode[] = [{ id: 'root-id', name: 'Root' }];
      componentRef.setInput('displayedPath', path);
      fixture.detectChanges();
      mockSpaceService.getSubspacesAsync.mockResolvedValue([]);

      await component.reload();

      expect(component.node().expanded).toBe(true);
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe from language changes', () => {
      component.ngOnDestroy();
      expect(() => langChanges$.next('fr')).not.toThrow();
    });
  });
});
