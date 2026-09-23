import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { FolderTreeViewComponent } from './folder-tree-view.component';

describe('FolderTreeViewComponent', () => {
  let component: FolderTreeViewComponent;
  let fixture: ComponentFixture<FolderTreeViewComponent>;
  let actionFinishedSubject: Subject<ActionEmitterResult>;

  const mockNodesService = {
    getPathAsync: vi.fn().mockResolvedValue([]),
  };

  const mockRouter = {
    navigate: vi.fn().mockResolvedValue(true),
  };

  beforeEach(async () => {
    actionFinishedSubject = new Subject<ActionEmitterResult>();

    const mockActionService = {
      actionFinished$: actionFinishedSubject.asObservable(),
    };

    await TestBed.configureTestingModule({
      imports: [FolderTreeViewComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: ActionService, useValue: mockActionService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {} },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(FolderTreeViewComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(FolderTreeViewComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('currentNode', { id: 'node1' } as ModelNode);
  });

  afterEach(() => {
    vi.clearAllMocks();
    localStorage.removeItem('showTreeView');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize root from group libraryId on init', async () => {
    fixture.componentRef.setInput('group', { libraryId: 'lib123' });
    localStorage.setItem('showTreeView', 'true');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.root).toBeDefined();
    expect(component.root.nodeId).toBe('lib123');
    expect(component.root.name).toBe('Library');
    expect(component.shown()).toBe(true);
  });

  it('should set shown to false when localStorage is not true', async () => {
    fixture.componentRef.setInput('group', { libraryId: 'lib123' });
    localStorage.setItem('showTreeView', 'false');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.shown()).toBe(false);
  });

  it('should close and update localStorage', () => {
    component.close();

    expect(component.shown()).toBe(false);
    expect(localStorage.getItem('showTreeView')).toBe('false');
  });

  it('should navigate on propagateNavigation', async () => {
    const { TreeNode } = await import('app/shared/treeview/tree-node');
    const node = new TreeNode('Test', 'node-id');

    component.propagateNavigation(node);

    expect(component.searchedNodeId()).toBe('node-id');
    expect(mockRouter.navigate).toHaveBeenCalled();
  });

  it('should return searchedNodeId from getSearchedNodeId when set', () => {
    component.searchedNodeId.set('abc');

    expect(component.getSearchedNodeId()).toBe('abc');
  });

  it('should return last path id from getSearchedNodeId when searchedNodeId is not set', async () => {
    component.searchedNodeId.set('');
    mockNodesService.getPathAsync.mockResolvedValue([
      { id: 'first' } as ModelNode,
      { id: 'last' } as ModelNode,
    ]);
    fixture.componentRef.setInput('currentNode', { id: 'node1' } as ModelNode);

    fixture.detectChanges();
    await fixture.whenStable();
    component.searchedNodeId.set('');

    expect(component.getSearchedNodeId()).toBe('last');
  });

  it('should return undefined from getSearchedNodeId when nothing is set', () => {
    component.searchedNodeId.set('');

    expect(component.getSearchedNodeId()).toBeUndefined();
  });

  it('should reload path when currentNode input changes', async () => {
    const pathNodes = [{ id: 'p1' } as ModelNode, { id: 'p2' } as ModelNode];
    mockNodesService.getPathAsync.mockResolvedValue(pathNodes);

    fixture.componentRef.setInput('currentNode', {
      id: 'new-node',
    } as ModelNode);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockNodesService.getPathAsync).toHaveBeenCalledWith({
      id: 'new-node',
    });
    expect(component.path()).toEqual(pathNodes);
    expect(component.searchedNodeId()).toBe('new-node');
  });
});
