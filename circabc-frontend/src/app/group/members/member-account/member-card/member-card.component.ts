import { Component, Input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

@Component({
  selector: 'cbc-member-card',
  templateUrl: './member-card.component.html',
  styleUrl: './member-card.component.scss',
  preserveWhitespaces: true,
  imports: [DownloadPipe, SecurePipe, TranslocoModule],
})
export class MemberCardComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  user!: User;

  getUserVisibility(): boolean {
    if (this.user?.visibility !== undefined) {
      return this.user.visibility;
    }

    return false;
  }
}
