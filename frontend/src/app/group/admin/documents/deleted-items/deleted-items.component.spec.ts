import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  ArchiveNode,
  ArchiveService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  PagedArchiveNodes,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeletedItemsComponent } from './deleted-items.component';

const mockIg: InterestGroup = {
  id: 'ig1',
  name: 'Test IG',
  permissions: {},
};

const mockArchiveNode: ArchiveNode = {
  id: 'node1',
  name: 'deleted-file.pdf',
  type: 'content',
  deletedBy: 'user1',
  deletedDate: '2026-01-01',
};

const mockPagedNodes: PagedArchiveNodes = {
  data: [mockArchiveNode],
  total: 1,
};

const mockArchiveService = {
  getDeletedDocumentsAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  deleteDeletedDocumentAsync: vi.fn().mockResolvedValue(undefined),
};

const mockGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
};

const mockUiMessageService = {
  addSuccessMessage: vi.fn(),
  addErrorMessage: vi.fn(),
};

describe('DeletedItemsComponent', () => {
  let component: DeletedItemsComponent;
  let componentRef: ComponentRef<DeletedItemsComponent>;
  let fixture: ComponentFixture<DeletedItemsComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DeletedItemsComponent],
      providers: [
        { provide: ArchiveService, useValue: mockArchiveService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeletedItemsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'ig1');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load interest group and deleted nodes on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: 'ig1',
    });
    expect(mockArchiveService.getDeletedDocumentsAsync).toHaveBeenCalledWith({
      id: 'ig1',
      limit: 10,
      page: 1,
      order: 'archivedDate_DESC',
    });
    expect(component.deletedNodes()).toEqual(mockPagedNodes);
    expect(component.totalItems()).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should change page', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.goToPage(2);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.page()).toBe(2);
    expect(mockArchiveService.getDeletedDocumentsAsync).toHaveBeenCalledWith({
      id: 'ig1',
      limit: 10,
      page: 2,
      order: 'archivedDate_DESC',
    });
  });

  it('should change limit and reset page to 1', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.changeLimit(25);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.limit()).toBe(25);
    expect(component.page()).toBe(1);
  });

  it('should identify file nodes', () => {
    const fileNode: ModelNode = { name: 'file.pdf', type: 'content' };
    const folderNode: ModelNode = { name: 'folder', type: 'folder' };

    expect(component.isFile(fileNode)).toBe(true);
    expect(component.isFile(folderNode)).toBe(false);
  });

  it('should identify folder nodes', () => {
    const folderNode: ModelNode = { name: 'folder', type: 'folder' };
    const fileNode: ModelNode = { name: 'file.pdf', type: 'content' };

    expect(component.isFolder(folderNode)).toBe(true);
    expect(component.isFolder(fileNode)).toBe(false);
  });

  it('should identify link nodes', () => {
    const linkNode: ModelNode = {
      name: 'link',
      properties: { mimetype: 'text/html', url: 'http://example.com' },
    };
    const nonLinkNode: ModelNode = {
      name: 'file',
      properties: { mimetype: 'application/pdf' },
    };

    expect(component.isLink(linkNode)).toBe(true);
    expect(component.isLink(nonLinkNode)).toBe(false);
  });

  it('should prepare and unprepare nodes for restore', () => {
    component.prepareRestoreNode(mockArchiveNode);
    expect(component.isPrepared(mockArchiveNode)).toBe(true);

    component.prepareRestoreNode(mockArchiveNode);
    expect(component.isPrepared(mockArchiveNode)).toBe(false);
  });

  it('should toggle all prepared nodes', () => {
    component.deletedNodes.set(mockPagedNodes);

    component.toggleAllPrepared();
    expect(component.allSelected).toBe(true);
    expect(component.restorableNodes()).toContain(mockArchiveNode);

    component.toggleAllPrepared();
    expect(component.allSelected).toBe(false);
    expect(component.restorableNodes()).toHaveLength(0);
  });

  it('should handle restoreFinish with success', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.restoreFinish({
      type: ActionType.RESTORE_CONTENT,
      result: ActionResult.SUCCEED,
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    expect(component.showModal()).toBe(false);
  });

  it('should handle purgeFinish with success', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.purgeFinish({
      type: ActionType.PURGE_CONTENT,
      result: ActionResult.SUCCEED,
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    expect(component.showPurgeModal()).toBe(false);
  });

  it('should handle purgeFinish with failure', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.purgeFinish({
      type: ActionType.PURGE_CONTENT,
      result: ActionResult.FAILED,
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
  });

  it('should purge a single node', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    await component.purgeNode(mockArchiveNode);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockArchiveService.deleteDeletedDocumentAsync).toHaveBeenCalledWith({
      id: 'ig1',
      nodeId: 'node1',
    });
    expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
  });

  it('should set restoreNode and show modal', () => {
    component.restoreNode(mockArchiveNode);

    expect(component.restorableNodes()).toEqual([mockArchiveNode]);
    expect(component.showModal()).toBe(true);
  });

  it('should cancel and reset state', () => {
    component.showModal.set(true);
    component.showPurgeModal.set(true);
    component.restorableNodes.set([mockArchiveNode]);

    component.canceled();

    expect(component.restorableNodes()).toHaveLength(0);
    expect(component.showModal()).toBe(false);
    expect(component.showPurgeModal()).toBe(false);
  });
});
