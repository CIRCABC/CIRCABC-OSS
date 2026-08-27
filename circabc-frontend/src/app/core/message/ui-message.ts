import { UiMessageLevel } from 'app/core/message/ui-message-level';

export class UiMessage {
  public level: UiMessageLevel;
  public body: string;
  public active: boolean;
  public autoclose = false;
  public displayTime = 5;
  public id?: string;

  public constructor(
    level: UiMessageLevel,
    content: string,
    autoclose?: boolean,
    displayTime?: number,
    id?: string
  ) {
    this.level = level;
    this.body = content;
    this.active = true;
    if (autoclose) {
      this.autoclose = autoclose;
    }
    if (displayTime) {
      this.displayTime = displayTime;
    }
    if (id) {
      this.id = id;
    }
  }
}
