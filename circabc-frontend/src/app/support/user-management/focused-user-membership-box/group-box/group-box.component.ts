import { Component, Input, output, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  type InterestGroupProfile,
  MembersService,
} from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-box',
  templateUrl: './group-box.component.html',
  styleUrl: './group-box.component.scss',
  imports: [RouterLink, InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class GroupBoxComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input() membership!: InterestGroupProfile;
  readonly userId = input.required<string>();
  readonly userUninvited = output();

  public uninviting = false;

  constructor(private membersService: MembersService) {}

  public async uninviteUSer() {
    const userId = this.userId();
    if (this.membership?.interestGroup?.id && userId) {
      this.uninviting = true;
      try {
        await firstValueFrom(
          this.membersService.deleteMember(
            this.membership.interestGroup.id,
            userId
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
