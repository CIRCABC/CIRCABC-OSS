import { Component, Input, output, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

@Component({
  selector: 'cbc-leader-card',
  templateUrl: './leader-card.component.html',
  styleUrl: './leader-card.component.scss',
  preserveWhitespaces: true,
  imports: [DownloadPipe, SecurePipe, TranslocoModule],
})
export class LeaderCardComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  user!: User;
  readonly showDelete = input(true);
  readonly removeClicked = output();

  onRemoveClick() {
    this.removeClicked.emit();
  }
}
