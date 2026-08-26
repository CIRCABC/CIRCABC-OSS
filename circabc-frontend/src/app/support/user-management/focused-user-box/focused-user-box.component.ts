import { Component, Input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-focused-user-box',
  templateUrl: './focused-user-box.component.html',
  styleUrl: './focused-user-box.component.scss',
  imports: [TranslocoModule],
})
export class FocusedUserBoxComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input() user: User | undefined;
}
