import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { TopicService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteTopicComponent } from './delete-topic.component';

const mockTopicService = {
  deleteTopic: vi.fn(),
  deleteTopicAsync: vi.fn(),
};

describe('DeleteTopicComponent', () => {
  let component: DeleteTopicComponent;
  let componentRef: ComponentRef<DeleteTopicComponent>;
  let fixture: ComponentFixture<DeleteTopicComponent>;

  beforeEach(async () => {
    mockTopicService.deleteTopic.mockReset();
    mockTopicService.deleteTopicAsync.mockReset();

    await TestBed.configureTestingModule({
      imports: [DeleteTopicComponent],
      providers: [
        { provide: TopicService, useValue: mockTopicService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteTopicComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('topic', { id: 'topic-123' });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('delete', () => {
    it('should delete topic successfully', async () => {
      mockTopicService.deleteTopicAsync.mockResolvedValue(undefined);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(mockTopicService.deleteTopicAsync).toHaveBeenCalledWith({
        id: 'topic-123',
      });
      expect(component.deleting()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_TOPIC,
        result: ActionResult.SUCCEED,
      });
    });

    it('should emit FAILED result on error', async () => {
      mockTopicService.deleteTopicAsync.mockRejectedValue(new Error('fail'));
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(component.deleting()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_TOPIC,
        result: ActionResult.FAILED,
      });
    });

    it('should not call service if topic has no id', async () => {
      componentRef.setInput('topic', {});
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(mockTopicService.deleteTopicAsync).not.toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_TOPIC,
      });
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result and hide modal', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_TOPIC,
        result: ActionResult.CANCELED,
      });
    });
  });
});
