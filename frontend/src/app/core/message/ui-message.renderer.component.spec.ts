import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageRendererComponent } from 'app/core/message/ui-message.renderer.component';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { of } from 'rxjs';
import { vi } from 'vitest';

describe('UiMessageRendererComponent', () => {
  let fixture: ComponentFixture<UiMessageRendererComponent>;
  let component: UiMessageRendererComponent;
  let componentRef: ComponentRef<UiMessageRendererComponent>;
  let mockService: { removeMessage: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockService = { removeMessage: vi.fn() };

    TestBed.configureTestingModule({
      imports: [UiMessageRendererComponent],
      providers: [
        { provide: UiMessageService, useValue: mockService },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    fixture = TestBed.createComponent(UiMessageRendererComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  function setMessage(msg: UiMessage): void {
    componentRef.setInput('message', msg);
    fixture.detectChanges();
  }

  it('should create', () => {
    setMessage(new UiMessage(UiMessageLevel.INFO, 'test'));
    expect(component).toBeDefined();
  });

  it('should close message and mark inactive', () => {
    const msg = new UiMessage(UiMessageLevel.INFO, 'hello');
    setMessage(msg);

    component.closeMessage();

    expect(msg.active).toBe(false);
    expect(mockService.removeMessage).toHaveBeenCalledWith(msg);
  });

  it('should auto-remove message after default timeout when autoclose is true', () => {
    vi.useFakeTimers();
    const msg = new UiMessage(UiMessageLevel.WARNING, 'auto', true);
    setMessage(msg);

    expect(mockService.removeMessage).not.toHaveBeenCalled();

    vi.advanceTimersByTime(5000);

    expect(mockService.removeMessage).toHaveBeenCalledWith(msg);
    vi.useRealTimers();
  });

  it('should use custom displayTime for autoclose', () => {
    vi.useFakeTimers();
    const msg = new UiMessage(UiMessageLevel.SUCCESS, 'custom', true, 7);
    setMessage(msg);

    vi.advanceTimersByTime(6999);
    expect(mockService.removeMessage).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(mockService.removeMessage).toHaveBeenCalledWith(msg);
    vi.useRealTimers();
  });

  it('should not auto-remove when autoclose is false', () => {
    vi.useFakeTimers();
    const msg = new UiMessage(UiMessageLevel.ERROR, 'stay');
    setMessage(msg);

    vi.advanceTimersByTime(10000);
    expect(mockService.removeMessage).not.toHaveBeenCalled();
    vi.useRealTimers();
  });

  it('should return correct image links', () => {
    setMessage(new UiMessage(UiMessageLevel.INFO, 'x'));
    expect(component.imageInfoLink).toBe('img/info-signs.png');
    expect(component.imageExclamationLink).toBe('img/exclamation.png');
    expect(component.imageErrorLink).toBe('img/error-sign.png');
    expect(component.imageCheckMarkLink).toBe('img/check-mark.png');
  });
});
