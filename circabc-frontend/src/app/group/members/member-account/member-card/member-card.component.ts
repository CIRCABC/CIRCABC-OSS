import { Component, Input } from '@angular/core';

import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-member-card',
  templateUrl: './member-card.component.html',
  styleUrls: ['./member-card.component.scss'],
  preserveWhitespaces: true,
})
export class MemberCardComponent {
  @Input()
  user!: User;

  getUserVisibility(): boolean {
    if (this.user && this.user.visibility !== undefined) {
      return this.user.visibility;
    }

    return false;
  }
}
