import { TestBed } from '@angular/core/testing';
import { UiMessage } from 'app/core/message/ui-message';

import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { vi } from 'vitest';

describe('UiMessageService', () => {
  let service: UiMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UiMessageService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit an info message via messageAnnounced$', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addInfoMessage('info test');

    expect(spy).toHaveBeenCalledTimes(1);
    const msg: UiMessage = spy.mock.calls[0][0];
    expect(msg.level).toBe(UiMessageLevel.INFO);
    expect(msg.body).toBe('info test');
    expect(msg.autoclose).toBe(false);
  });

  it('should emit an error message', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addErrorMessage('error test', true);

    const msg: UiMessage = spy.mock.calls[0][0];
    expect(msg.level).toBe(UiMessageLevel.ERROR);
    expect(msg.body).toBe('error test');
    expect(msg.autoclose).toBe(true);
  });

  it('should emit a warning message', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addWarningMessage('warn test');

    const msg: UiMessage = spy.mock.calls[0][0];
    expect(msg.level).toBe(UiMessageLevel.WARNING);
  });

  it('should emit a success message with custom displayTime', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addSuccessMessage('success test', false, 10);

    const msg: UiMessage = spy.mock.calls[0][0];
    expect(msg.level).toBe(UiMessageLevel.SUCCESS);
    expect(msg.displayTime).toBe(10);
  });

  it('should emit on messageDestroyed$ when removeMessage is called', () => {
    const spy = vi.fn();
    service.messageDestroyed$.subscribe(spy);

    const msg = new UiMessage(UiMessageLevel.INFO, 'to remove');
    service.removeMessage(msg);

    expect(spy).toHaveBeenCalledWith(msg);
  });

  it('should suppress duplicate messages within 5 seconds', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addInfoMessage('duplicate');
    service.addInfoMessage('duplicate');

    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('should allow the same message after 5 seconds', () => {
    vi.useFakeTimers();
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addInfoMessage('timed');
    vi.advanceTimersByTime(5001);
    service.addInfoMessage('timed');

    expect(spy).toHaveBeenCalledTimes(2);
    vi.useRealTimers();
  });

  it('should allow different messages in quick succession', () => {
    const spy = vi.fn();
    service.messageAnnounced$.subscribe(spy);

    service.addInfoMessage('first');
    service.addInfoMessage('second');

    expect(spy).toHaveBeenCalledTimes(2);
  });
});
