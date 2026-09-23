import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { Node, NodesService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ClipboardComponent } from './clipboard.component';

describe('ClipboardComponent', () => {
  let component: ClipboardComponent;
  let componentRef: ComponentRef<ClipboardComponent>;
  let fixture: ComponentFixture<ClipboardComponent>;

  const itemsAdded$ = new Subject<Node>();
  const itemsRemoved$ = new Subject<Node>();

  const mockClipboardService = {
    itemsAdded$: itemsAdded$.asObservable(),
    itemsRemoved$: itemsRemoved$.asObservable(),
  };

  const mockNodesService = {
    postPaste: vi.fn().mockReturnValue(of(undefined)),
    putPaste: vi.fn().mockReturnValue(of(undefined)),
    postLink: vi.fn().mockReturnValue(of(undefined)),
    postPasteAsync: vi.fn().mockResolvedValue(undefined),
    putPasteAsync: vi.fn().mockResolvedValue(undefined),
    postLinkAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockPermEvalService = {
    isLibAdmin: vi.fn().mockReturnValue(false),
    isLibFullEdit: vi.fn().mockReturnValue(false),
    isLibManageOwnOrHigher: vi.fn().mockReturnValue(false),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ userId: 'testUser' }),
  };

  const mockBulkDownloadPipe = {
    transform: vi.fn().mockReturnValue('http://example.com/bulk'),
  };

  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  const mockDialog = {
    open: vi.fn().mockReturnValue({
      afterClosed: () => of(true),
      close: vi.fn(),
    }),
    openDialogs: [] as unknown[],
    afterOpened: new Subject<unknown>(),
    getDialogById: vi.fn(),
    closeAll: vi.fn(),
  };

  const routeParams$ = new Subject<{ [key: string]: string }>();
  const mockRoute = {
    params: routeParams$.asObservable(),
  };

  beforeEach(async () => {
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [ClipboardComponent],
      providers: [
        { provide: ClipboardService, useValue: mockClipboardService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: BulkDownloadPipe, useValue: mockBulkDownloadPipe },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: MatDialog, useValue: mockDialog },
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

    fixture = TestBed.createComponent(ClipboardComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('currentStandingNode', { id: 'folder1' });
    componentRef.setInput('contents', []);
    componentRef.setInput('currentStandingNodeIsFolder', true);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should load nodes from sessionStorage', () => {
      const nodes: Node[] = [{ id: 'node1', name: 'Test' }];
      sessionStorage.setItem('cbc-clipboardig1', JSON.stringify(nodes));

      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      expect(component.nodes()).toEqual(nodes);
    });

    it('should emit itemsAmount', () => {
      const spy = vi.spyOn(component.itemsAmount, 'emit');
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      expect(spy).toHaveBeenCalledWith(0);
    });
  });

  describe('clipboard subscription', () => {
    it('should add node when itemsAdded$ emits', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const node: Node = { id: 'node1', name: 'Added Node' };
      itemsAdded$.next(node);

      expect(component.nodes()).toContainEqual(node);
    });

    it('should not add duplicate node', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const node: Node = { id: 'node1', name: 'Added Node' };
      itemsAdded$.next(node);
      itemsAdded$.next(node);

      expect(component.nodes()).toHaveLength(1);
    });

    it('should remove node when itemsRemoved$ emits', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const node: Node = { id: 'node1', name: 'Node' };
      itemsAdded$.next(node);
      itemsRemoved$.next(node);

      expect(component.nodes()).toHaveLength(0);
    });
  });

  describe('isAuthorized', () => {
    beforeEach(() => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();
    });

    it('should return false if currentStandingNodeIsFolder is false', () => {
      componentRef.setInput('currentStandingNodeIsFolder', false);
      const node: Node = { id: 'n1' };
      expect(component.isAuthorized(node, 'Copy')).toBe(false);
    });

    it('should return false if user has no paste permission', () => {
      mockPermEvalService.isLibAdmin.mockReturnValue(false);
      mockPermEvalService.isLibFullEdit.mockReturnValue(false);
      mockPermEvalService.isLibManageOwnOrHigher.mockReturnValue(false);

      const node: Node = { id: 'n1' };
      expect(component.isAuthorized(node, 'Copy')).toBe(false);
    });

    it('should return true for Copy when user is LibAdmin', () => {
      mockPermEvalService.isLibAdmin.mockReturnValue(true);
      const node: Node = { id: 'n1' };
      expect(component.isAuthorized(node, 'Copy')).toBe(true);
    });

    it('should return false for Move when user cannot delete', () => {
      mockPermEvalService.isLibAdmin.mockReturnValueOnce(true); // paste permission
      mockPermEvalService.isLibAdmin.mockReturnValueOnce(false); // delete permission
      mockPermEvalService.isLibFullEdit.mockReturnValue(false);
      mockPermEvalService.isLibManageOwnOrHigher.mockReturnValue(false);

      const node: Node = { id: 'n1' };
      expect(component.isAuthorized(node, 'Move')).toBe(false);
    });
  });

  describe('removeItem', () => {
    it('should remove the node from the list', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const node: Node = { id: 'node1', name: 'Node' };
      itemsAdded$.next(node);
      component.removeItem(node);

      expect(component.nodes()).toHaveLength(0);
    });
  });

  describe('removeAll', () => {
    it('should clear all nodes', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      itemsAdded$.next({ id: 'n1' });
      itemsAdded$.next({ id: 'n2' });
      component.removeAll();

      expect(component.nodes()).toHaveLength(0);
    });
  });

  describe('copyPasteItem', () => {
    it('should call nodesService.postPaste and emit success', async () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const spy = vi.spyOn(component.actionFinished, 'emit');
      const node: Node = { id: 'node1' };

      await component.copyPasteItem(node);

      expect(mockNodesService.postPasteAsync).toHaveBeenCalledWith({
        id: 'folder1',
        nodeIds: ['node1'],
      });
      expect(spy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.CLIPBOARD_COPY_NODE,
      });
    });
  });

  describe('cutPasteItem', () => {
    it('should call nodesService.putPaste, remove item, and emit success', async () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      vi.spyOn(component, 'showConfirmationDialog').mockResolvedValue();
      const spy = vi.spyOn(component.actionFinished, 'emit');
      const node: Node = { id: 'node1' };
      itemsAdded$.next(node);

      await component.cutPasteItem(node);

      expect(mockNodesService.putPasteAsync).toHaveBeenCalledWith({
        id: 'folder1',
        nodeIds: ['node1'],
        notify: true,
      });
      expect(component.nodes()).toHaveLength(0);
      expect(spy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.CLIPBOARD_MOVE_NODE,
      });
    });
  });

  describe('linkPasteItem', () => {
    it('should call nodesService.postLink, remove item, and emit success', async () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      const spy = vi.spyOn(component.actionFinished, 'emit');
      const node: Node = { id: 'node1' };
      itemsAdded$.next(node);

      await component.linkPasteItem(node);

      expect(mockNodesService.postLinkAsync).toHaveBeenCalledWith({
        id: 'folder1',
        nodeIds: ['node1'],
      });
      expect(component.nodes()).toHaveLength(0);
      expect(spy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.CLIPBOARD_LINK_NODE,
      });
    });
  });

  describe('bulkDownload', () => {
    it('should call bulkDownloadPipe and saveAsService', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      itemsAdded$.next({ id: 'n1' });
      component.bulkDownload();

      expect(mockBulkDownloadPipe.transform).toHaveBeenCalledWith(['n1']);
      expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
        'http://example.com/bulk',
        'bulk.zip'
      );
    });
  });

  describe('close', () => {
    it('should set visible to false and emit closeEmitter', () => {
      fixture.detectChanges();
      component.visible = true;
      const spy = vi.spyOn(component.closeEmitter, 'emit');

      component.close();

      expect(component.visible).toBe(false);
      expect(spy).toHaveBeenCalled();
    });
  });

  describe('getNodeIds', () => {
    it('should return array of node ids', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      itemsAdded$.next({ id: 'a' });
      itemsAdded$.next({ id: 'b' });

      expect(component.getNodeIds()).toEqual(['a', 'b']);
    });
  });

  describe('allAuthorized', () => {
    it('should return true when all nodes are authorized', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      mockPermEvalService.isLibAdmin.mockReturnValue(true);
      itemsAdded$.next({ id: 'n1' });
      itemsAdded$.next({ id: 'n2' });

      expect(component.allAuthorized('Copy')).toBe(true);
    });

    it('should return false when any node is not authorized', () => {
      fixture.detectChanges();
      routeParams$.next({ id: 'ig1' });
      component.ngOnChanges();

      mockPermEvalService.isLibAdmin.mockReturnValue(false);
      mockPermEvalService.isLibFullEdit.mockReturnValue(false);
      mockPermEvalService.isLibManageOwnOrHigher.mockReturnValue(false);
      itemsAdded$.next({ id: 'n1' });

      expect(component.allAuthorized('Copy')).toBe(false);
    });
  });
});
