import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-user-result-box',
  templateUrl: './user-result-box.component.html',
  styleUrl: './user-result-box.component.scss',
  imports: [TranslocoModule],
})
export class UserResultBoxComponent {
  readonly user = input.required<User>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input() focusedUserId = '';
  readonly showSelect = input(true);
  readonly showRemove = input(true);
  readonly focusedUserIdChange = output<string>();
  readonly selectionTriggered = output<User>();
  readonly removeTriggered = output<User>();

  public toggleFocus() {
    const user = this.user();
    if (this.focusedUserId === user.userId) {
      this.focusedUserId = '';
    } else if (user.userId) {
      this.focusedUserId = user.userId;
    }
    this.focusedUserIdChange.emit(this.focusedUserId);
  }

  public triggerSelect(event: Event) {
    this.selectionTriggered.emit(this.user());
    event.stopPropagation();
  }

  public triggerRemove(event: Event) {
    this.removeTriggered.emit(this.user());
    event.stopPropagation();
  }
}
