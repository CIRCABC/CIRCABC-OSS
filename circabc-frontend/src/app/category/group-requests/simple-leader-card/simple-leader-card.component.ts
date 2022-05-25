import { Component, EventEmitter, Input, Output } from '@angular/core';

import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-simple-leader-card',
  templateUrl: './simple-leader-card.component.html',
  styleUrls: ['./simple-leader-card.component.scss'],
  preserveWhitespaces: true,
})
export class SimpleLeaderCardComponent {
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
