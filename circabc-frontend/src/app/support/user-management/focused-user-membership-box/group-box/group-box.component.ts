import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  InterestGroupProfile,
  MembersService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-box',
  templateUrl: './group-box.component.html',
  styleUrls: ['./group-box.component.scss'],
})
export class GroupBoxComponent {
  @Input() membership!: InterestGroupProfile;
  @Input() userId!: string;
  @Output() readonly userUninvited = new EventEmitter();

  public uninviting = false;

  constructor(private membersService: MembersService) {}

  public async uninviteUSer() {
    if (
      this.membership &&
      this.membership.interestGroup &&
      this.membership.interestGroup.id &&
      this.userId
    ) {
      this.uninviting = true;
      try {
        await firstValueFrom(
          this.membersService.deleteMember(
            this.membership.interestGroup.id,
            this.userId
          )
        );
        this.userUninvited.emit();
      } catch (error) {
        console.error(error);
      }
      this.uninviting = false;
    }
  }
}
