import { Component, Input } from '@angular/core';
import { User } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-focused-user-box',
  templateUrl: './focused-user-box.component.html',
  styleUrls: ['./focused-user-box.component.scss'],
})
export class FocusedUserBoxComponent {
  @Input() user: User | undefined;
}
