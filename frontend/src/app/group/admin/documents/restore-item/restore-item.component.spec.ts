import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ArchiveNode, ArchiveService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RestoreItemComponent } from './restore-item.component';

const mockArchiveService = {
  restoreDocumentAsync: vi.fn().mockResolvedValue({}),
};

describe('RestoreItemComponent', () => {
  let component: RestoreItemComponent;
  let componentRef: ComponentRef<RestoreItemComponent>;
  let fixture: ComponentFixture<RestoreItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RestoreItemComponent],
      providers: [
        { provide: ArchiveService, useValue: mockArchiveService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RestoreItemComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('currentIg', { id: 'ig1' });
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('isFolder', () => {
    it('should return true when type includes folder', () => {
      const node: ArchiveNode = { type: 'cm:folder' };
      expect(component.isFolder(node)).toBe(true);
    });

    it('should return false when type does not include folder', () => {
      const node: ArchiveNode = { type: 'cm:content' };
      expect(component.isFolder(node)).toBe(false);
    });

    it('should return false when type is undefined', () => {
      const node: ArchiveNode = {};
      expect(component.isFolder(node)).toBe(false);
    });
  });

  describe('isLink', () => {
    it('should return true when mimetype is text/html and url is non-empty', () => {
      const node: ArchiveNode = {
        properties: { mimetype: 'text/html', url: 'http://example.com' },
      };
      expect(component.isLink(node)).toBe(true);
    });

    it('should return false when url is empty', () => {
      const node: ArchiveNode = {
        properties: { mimetype: 'text/html', url: '' },
      };
      expect(component.isLink(node)).toBe(false);
    });

    it('should return false when mimetype is not text/html', () => {
      const node: ArchiveNode = {
        properties: { mimetype: 'application/pdf', url: 'http://example.com' },
      };
      expect(component.isLink(node)).toBe(false);
    });

    it('should return false when properties is undefined', () => {
      const node: ArchiveNode = {};
      expect(component.isLink(node)).toBe(false);
    });
  });

  describe('restore', () => {
    it('should restore nodes and emit finishRestore with SUCCEED', async () => {
      componentRef.setInput('restorableNodes', [{ id: 'node1' }]);
      component.selectedNodes = ['folder1'];
      const emitSpy = vi.spyOn(component.finishRestore, 'emit');

      await component.restore();

      expect(mockArchiveService.restoreDocumentAsync).toHaveBeenCalledWith({
        id: 'ig1',
        restoreNodeMetadata: {
          archiveNodeId: 'node1',
          targetFolderId: 'folder1',
        },
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.RESTORE_CONTENT,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should emit finishRestore with FAILED on error', async () => {
      mockArchiveService.restoreDocumentAsync.mockRejectedValue(
        new Error('fail')
      );
      componentRef.setInput('restorableNodes', [{ id: 'node1' }]);
      const emitSpy = vi.spyOn(component.finishRestore, 'emit');

      await component.restore();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.RESTORE_CONTENT,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('onCancel', () => {
    it('should emit cancelRestore with CANCELED result', () => {
      const emitSpy = vi.spyOn(component.cancelRestore, 'emit');

      component.onCancel();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.RESTORE_CONTENT,
      });
    });
  });

  describe('showFolderPicker', () => {
    it('should set folderPicker to true', () => {
      component.showFolderPicker();
      expect(component.folderPicker).toBe(true);
    });
  });

  describe('hideFolderPicker', () => {
    it('should set folderPicker to false and clear selectedNodes', () => {
      component.folderPicker = true;
      component.selectedNodes = ['node1'];

      component.hideFolderPicker();

      expect(component.folderPicker).toBe(false);
      expect(component.selectedNodes).toEqual([]);
    });
  });
});
