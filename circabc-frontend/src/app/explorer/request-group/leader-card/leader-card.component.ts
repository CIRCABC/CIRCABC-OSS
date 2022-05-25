import { Component, EventEmitter, Input, Output } from '@angular/core';

import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-leader-card',
  templateUrl: './leader-card.component.html',
  styleUrls: ['./leader-card.component.scss'],
  preserveWhitespaces: true,
})
export class LeaderCardComponent {
  @Input()
  user!: User;
  @Input()
  showDelete = true;
  @Output()
  readonly removeClicked = new EventEmitter();

  onRemoveClick() {
    this.removeClicked.emit();
  }
}
