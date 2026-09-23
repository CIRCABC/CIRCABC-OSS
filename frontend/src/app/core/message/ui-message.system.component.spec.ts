import { Component, input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageRendererComponent } from 'app/core/message/ui-message.renderer.component';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageSystemComponent } from 'app/core/message/ui-message.system.component';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { Subject } from 'rxjs';

@Component({
  selector: 'cbc-ui-message-rendered',
  template: '',
})
class MockRendererComponent {
  message = input<UiMessage>();
}

describe('UiMessageSystemComponent', () => {
  let component: UiMessageSystemComponent;
  let fixture: ComponentFixture<UiMessageSystemComponent>;
  let messageAnnounced$: Subject<UiMessage>;
  let messageDestroyed$: Subject<UiMessage>;

  beforeEach(() => {
    messageAnnounced$ = new Subject<UiMessage>();
    messageDestroyed$ = new Subject<UiMessage>();

    const mockService = {
      messageAnnounced$: messageAnnounced$.asObservable(),
      messageDestroyed$: messageDestroyed$.asObservable(),
    };

    TestBed.configureTestingModule({
      imports: [UiMessageSystemComponent],
      providers: [{ provide: UiMessageService, useValue: mockService }],
    }).overrideComponent(UiMessageSystemComponent, {
      remove: { imports: [UiMessageRendererComponent] },
      add: { imports: [MockRendererComponent] },
    });

    fixture = TestBed.createComponent(UiMessageSystemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should add message when announced', () => {
    const msg = new UiMessage(UiMessageLevel.INFO, 'test');
    messageAnnounced$.next(msg);
    expect(component.messages()).toEqual([msg]);
  });

  it('should remove message when destroyed', () => {
    const msg = new UiMessage(UiMessageLevel.ERROR, 'error');
    messageAnnounced$.next(msg);
    messageDestroyed$.next(msg);
    expect(component.messages()).toEqual([]);
  });

  it('should accumulate multiple messages', () => {
    const msg1 = new UiMessage(UiMessageLevel.INFO, 'first');
    const msg2 = new UiMessage(UiMessageLevel.WARNING, 'second');
    messageAnnounced$.next(msg1);
    messageAnnounced$.next(msg2);
    expect(component.messages()).toHaveLength(2);
  });

  it('should unsubscribe on destroy', () => {
    const msg = new UiMessage(UiMessageLevel.SUCCESS, 'msg');
    component.ngOnDestroy();
    messageAnnounced$.next(msg);
    expect(component.messages()).toEqual([]);
  });
});
