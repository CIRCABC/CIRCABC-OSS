import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { TopicService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EditTopicComponent } from './edit-topic.component';

describe('EditTopicComponent', () => {
  let component: EditTopicComponent;
  let fixture: ComponentFixture<EditTopicComponent>;

  const mockTopicService = {
    putTopic: vi.fn().mockReturnValue(of(undefined)),
    putTopicAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    mockTopicService.putTopic.mockReturnValue(of(undefined));
    mockTopicService.putTopicAsync.mockResolvedValue(undefined);

    await TestBed.configureTestingModule({
      imports: [EditTopicComponent, ReactiveFormsModule],
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
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(EditTopicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form with empty name', () => {
    expect(component.editTopicForm.controls.name.value).toBe('');
  });

  it('should mark form invalid when name is empty', () => {
    component.editTopicForm.controls.name.setValue('');
    expect(component.editTopicForm.valid).toBe(false);
  });

  it('should mark form valid with a proper name', () => {
    component.editTopicForm.controls.name.setValue('ValidName');
    expect(component.editTopicForm.valid).toBe(true);
  });

  it('should emit CANCELED result on cancel', () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.cancel();
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.EDIT_TOPIC,
    });
  });

  it('should call putTopic and emit SUCCEED on save', async () => {
    fixture.componentRef.setInput('topic', { id: '123', name: 'Old' });
    fixture.detectChanges();
    component.editTopicForm.controls.name.setValue('NewName');

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.save();

    expect(mockTopicService.putTopicAsync).toHaveBeenCalledWith({
      id: '123',
      node: {
        id: '123',
        name: 'NewName',
      },
    });
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.SUCCEED,
      type: ActionType.EDIT_TOPIC,
    });
    expect(component.updating()).toBe(false);
  });

  it('should emit FAILED on save error', async () => {
    fixture.componentRef.setInput('topic', { id: '456', name: 'Old' });
    fixture.detectChanges();
    component.editTopicForm.controls.name.setValue('NewName');
    mockTopicService.putTopicAsync.mockRejectedValue(new Error('fail'));

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.save();

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.FAILED,
      type: ActionType.EDIT_TOPIC,
    });
    expect(component.updating()).toBe(false);
  });
});
