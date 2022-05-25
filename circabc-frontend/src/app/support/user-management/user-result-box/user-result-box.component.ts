import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-user-result-box',
  templateUrl: './user-result-box.component.html',
  styleUrls: ['./user-result-box.component.scss'],
})
export class UserResultBoxComponent {
  @Input() user!: User;
  @Input() focusedUserId = '';
  @Input() showSelect = true;
  @Input() showRemove = true;
  @Output() readonly focusedUserIdChange = new EventEmitter<string>();
  @Output() readonly selectionTriggered = new EventEmitter<User>();
  @Output() readonly removeTriggered = new EventEmitter<User>();

  public toggleFocus() {
    if (this.focusedUserId === this.user.userId) {
      this.focusedUserId = '';
    } else if (this.user.userId) {
      this.focusedUserId = this.user.userId;
    }
    this.focusedUserIdChange.emit(this.focusedUserId);
  }

  public triggerSelect(event: Event) {
    this.selectionTriggered.emit(this.user);
    event.stopPropagation();
  }

  public triggerRemove(event: Event) {
    this.removeTriggered.emit(this.user);
    event.stopPropagation();
  }
}
