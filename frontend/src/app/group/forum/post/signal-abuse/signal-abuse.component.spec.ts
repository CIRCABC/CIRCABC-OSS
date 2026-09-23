import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { Node as ModelNode, PostService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SignalAbuseComponent } from './signal-abuse.component';

describe('SignalAbuseComponent', () => {
  let component: SignalAbuseComponent;
  let componentRef: ComponentRef<SignalAbuseComponent>;
  let fixture: ComponentFixture<SignalAbuseComponent>;
  const mockPostService = {
    postAbuse: vi.fn().mockReturnValue(of(undefined)),
    postAbuseAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockPost: ModelNode = {
    id: 'post-123',
    properties: { message: 'Test message' },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignalAbuseComponent, ReactiveFormsModule],
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
    }).compileComponents();

    fixture = TestBed.createComponent(SignalAbuseComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('post', mockPost);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form on changes', () => {
    expect(component.signalPostForm).toBeDefined();
    expect(component.signalPostForm.controls['abuseText'].value).toBe('');
  });

  it('should return message from post properties', () => {
    expect(component.message).toBe('Test message');
  });

  it('should return empty string when post has no properties', () => {
    componentRef.setInput('post', { id: 'post-456' } as ModelNode);
    fixture.detectChanges();
    expect(component.message).toBe('');
  });

  it('should call postAbuse and emit success on accept', async () => {
    mockPostService.postAbuseAsync.mockResolvedValue(undefined);
    component.signalPostForm.controls['abuseText'].setValue('spam content');

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.accept();

    expect(mockPostService.postAbuseAsync).toHaveBeenCalledWith({
      id: 'post-123',
      abuseText: 'spam content',
    });
    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.SUCCEED,
      type: ActionType.SIGNAL_ABUSE_POST,
    });
    expect(component.executing()).toBe(false);
  });

  it('should emit canceled result on cancel', () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.cancel('close');

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.SIGNAL_ABUSE_POST,
    });
  });
});
