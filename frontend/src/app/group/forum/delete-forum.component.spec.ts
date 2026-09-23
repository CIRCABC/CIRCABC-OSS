import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteForumComponent } from './delete-forum.component';

const mockForumService = {
  deleteForum: vi.fn().mockReturnValue(of(undefined)),
  deleteForumAsync: vi.fn().mockResolvedValue(undefined),
};

const mockActionService = {
  propagateActionFinished: vi.fn(),
};

describe('DeleteForumComponent', () => {
  let component: DeleteForumComponent;
  let componentRef: ComponentRef<DeleteForumComponent>;
  let fixture: ComponentFixture<DeleteForumComponent>;

  const forum: ModelNode = { id: 'forum-123', name: 'Test Forum' };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DeleteForumComponent],
      providers: [
        { provide: ForumService, useValue: mockForumService },
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

    fixture = TestBed.createComponent(DeleteForumComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('forum', forum);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('delete', () => {
    it('should delete forum successfully', async () => {
      await component.delete();

      expect(mockForumService.deleteForumAsync).toHaveBeenCalledWith({
        id: 'forum-123',
      });
      expect(component.deleting()).toBe(false);
      expect(mockActionService.propagateActionFinished).toHaveBeenCalledWith({
        type: ActionType.DELETE_FORUM,
        result: ActionResult.SUCCEED,
      });
    });

    it('should handle delete failure', async () => {
      mockForumService.deleteForumAsync.mockRejectedValue(new Error('fail'));

      await component.delete();

      expect(component.deleting()).toBe(false);
      expect(mockActionService.propagateActionFinished).toHaveBeenCalledWith({
        type: ActionType.DELETE_FORUM,
        result: ActionResult.FAILED,
      });
    });

    it('should not call service when forum has no id', async () => {
      componentRef.setInput('forum', { name: 'No ID' } as ModelNode);
      fixture.detectChanges();

      await component.delete();

      expect(mockForumService.deleteForumAsync).not.toHaveBeenCalled();
      expect(mockActionService.propagateActionFinished).toHaveBeenCalled();
    });

    it('should set showModal to false after delete', async () => {
      componentRef.setInput('showModal', true);
      fixture.detectChanges();

      await component.delete();

      expect(component.showModal()).toBe(false);
    });
  });

  describe('cancelWizard', () => {
    it('should emit canceled result and close modal', () => {
      componentRef.setInput('showModal', true);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.DELETE_FORUM,
      });
    });
  });
});
