import { Injectable } from '@angular/core';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UiMessageService {
  private messageSource: Subject<UiMessage> = new Subject<UiMessage>();
  private messageDestroySource: Subject<UiMessage> = new Subject<UiMessage>();
  public messageAnnounced$: Observable<UiMessage> =
    this.messageSource.asObservable();
  public messageDestroyed$: Observable<UiMessage> =
    this.messageDestroySource.asObservable();

  private lastMessage = { message: '', time: new Date().getTime() };

  public addInfoMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.INFO, autoclose, displayTime);
  }

  public addErrorMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.ERROR, autoclose, displayTime);
  }

  public addWarningMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.WARNING, autoclose, displayTime);
  }

  public addSuccessMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.SUCCESS, autoclose, displayTime);
  }

  public removeMessage(message: UiMessage): void {
    this.messageDestroySource.next(message);
  }

  private isSameAsLastMessage(message: string): boolean {
    let result = false;
    const time = new Date().getTime();
    if (
      message === this.lastMessage.message &&
      time - this.lastMessage.time < 5000
    ) {
      result = true;
    }
    this.lastMessage.message = message;
    this.lastMessage.time = time;
    return result;
  }

  private addMessage(
    message: string,
    level: UiMessageLevel,
    autoclose = false,
    displayTime?: number
  ): void {
    let finalDisplayTime;
    if (displayTime !== undefined) {
      finalDisplayTime = displayTime;
    }

    if (!this.isSameAsLastMessage(message)) {
      const uiMessage = new UiMessage(
        level,
        message,
        autoclose,
        finalDisplayTime
      );
      this.messageSource.next(uiMessage);
    }
  }
}
