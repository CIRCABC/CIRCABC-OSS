import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { Node as ModelNode, PostService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RejectPostComponent } from './reject-post.component';

const mockPostService = {
  putVerify: vi.fn().mockReturnValue(of(undefined)),
  putVerifyAsync: vi.fn().mockResolvedValue(undefined),
};

const mockNode: ModelNode = { id: 'node-1', name: 'test-post' };

describe('RejectPostComponent', () => {
  let component: RejectPostComponent;
  let fixture: ComponentFixture<RejectPostComponent>;

  beforeEach(() => {
    mockPostService.putVerify.mockReturnValue(of(undefined));
    mockPostService.putVerifyAsync.mockResolvedValue(undefined);

    TestBed.configureTestingModule({
      imports: [RejectPostComponent],
      providers: [
        { provide: PostService, useValue: mockPostService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(RejectPostComponent, {
      set: {
        imports: [ReactiveFormsModule, TranslocoModule],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    fixture = TestBed.createComponent(RejectPostComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('post', mockNode);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize rejectPostForm on changes', () => {
    expect(component.rejectPostForm).toBeDefined();
    expect(component.rejectPostForm.controls['rejectReason']).toBeDefined();
  });

  describe('accept', () => {
    it('should call putVerify and emit SUCCEED result', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.rejectPostForm.controls['rejectReason'].setValue('bad content');

      await component.accept();

      expect(mockPostService.putVerifyAsync).toHaveBeenCalledWith({
        id: 'node-1',
        approve: false,
        rejectReason: 'bad content',
      });
      expect(component.showModal()).toBe(false);
      expect(component.executing()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.REJECT_POST,
      });
    });
  });

  describe('cancel', () => {
    it('should set showModal to false and emit CANCELED result', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancel('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.REJECT_POST,
      });
    });
  });
});
