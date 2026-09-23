import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteActionComponent } from './delete-action.component';

describe('DeleteActionComponent', () => {
  let component: DeleteActionComponent;
  let componentRef: ComponentRef<DeleteActionComponent>;
  let fixture: ComponentFixture<DeleteActionComponent>;

  const mockSpaceService = { deleteSpace: vi.fn(), deleteSpaceAsync: vi.fn() };
  const mockContentService = {
    deleteContent: vi.fn(),
    deleteContentAsync: vi.fn(),
  };
  const mockClipboardService = { removeItem: vi.fn() };
  const mockActionService = { propagateActionFinished: vi.fn() };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteActionComponent],
      providers: [
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ContentService, useValue: mockContentService },
        { provide: ClipboardService, useValue: mockClipboardService },
        { provide: ActionService, useValue: mockActionService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteActionComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  function setNode(node: ModelNode): void {
    componentRef.setInput('node', node);
    fixture.detectChanges();
  }

  describe('confirmationMessage', () => {
    it('should return empty string when type is undefined', () => {
      setNode({ id: '1' });
      expect(component.confirmationMessage).toBe('');
    });

    it('should return folderlink message for folderlink type', () => {
      setNode({ id: '1', type: 'folderlink' });
      expect(component.confirmationMessage).toBe(
        'text.delete-link.confirmation'
      );
    });

    it('should return folder message for folder type', () => {
      setNode({ id: '1', type: 'folder' });
      expect(component.confirmationMessage).toBe(
        'text.delete-space.confirmation'
      );
    });

    it('should return content message for content type', () => {
      setNode({ id: '1', type: 'content' });
      expect(component.confirmationMessage).toBe(
        'text.delete-content.confirmation'
      );
    });
  });

  describe('delete', () => {
    it('should delete a folder and emit SUCCEED', async () => {
      mockSpaceService.deleteSpaceAsync.mockResolvedValue(undefined);
      setNode({ id: '123', type: 'folder' });
      component.showModal.set(true);

      await component.delete();

      expect(mockSpaceService.deleteSpaceAsync).toHaveBeenCalledWith({
        id: '123',
        notify: true,
      });
      expect(mockClipboardService.removeItem).toHaveBeenCalled();
      expect(mockActionService.propagateActionFinished).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.DELETE_SPACE,
          result: ActionResult.SUCCEED,
        })
      );
      expect(component.showModal()).toBe(false);
      expect(component.deleting()).toBe(false);
    });

    it('should emit FAILED when folder deletion throws', async () => {
      mockSpaceService.deleteSpaceAsync.mockRejectedValue(new Error('fail'));
      setNode({ id: '123', type: 'folder' });

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.delete();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.DELETE_SPACE,
          result: ActionResult.FAILED,
        })
      );
      expect(component.deleting()).toBe(false);
    });

    it('should delete content and emit SUCCEED', async () => {
      mockContentService.deleteContentAsync.mockResolvedValue(undefined);
      setNode({ id: '456', type: 'content' });
      component.showModal.set(true);

      await component.delete();

      expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
        id: '456',
        notify: true,
      });
      expect(mockClipboardService.removeItem).toHaveBeenCalled();
      expect(component.showModal()).toBe(false);
      expect(component.deleting()).toBe(false);
    });

    it('should emit FAILED when content deletion throws', async () => {
      mockContentService.deleteContentAsync.mockRejectedValue(
        new Error('fail')
      );
      setNode({ id: '456', type: 'content' });

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.delete();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.DELETE_CONTENT,
          result: ActionResult.FAILED,
        })
      );
    });

    it('should pass notify=false when toggle is unchecked', async () => {
      mockContentService.deleteContentAsync.mockResolvedValue(undefined);
      setNode({ id: '789', type: 'content' });
      component.notifyFormGroup.controls.notify.setValue(false);

      await component.delete();

      expect(mockContentService.deleteContentAsync).toHaveBeenCalledWith({
        id: '789',
        notify: false,
      });
    });

    it('should do nothing when node has no id', async () => {
      setNode({ type: 'folder' });

      await component.delete();

      expect(mockSpaceService.deleteSpace).not.toHaveBeenCalled();
      expect(mockContentService.deleteContent).not.toHaveBeenCalled();
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result and hide modal', () => {
      setNode({ id: '1', type: 'folder' });
      component.showModal.set(true);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.CANCELED,
          type: ActionType.DELETE_SPACE,
        })
      );
    });
  });
});
