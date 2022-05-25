import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import {
  Category,
  InterestGroupProfile,
  UserService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-focused-user-membership-box',
  templateUrl: './focused-user-membership-box.component.html',
  styleUrls: ['./focused-user-membership-box.component.scss'],
})
export class FocusedUserMembershipBoxComponent implements OnInit, OnChanges {
  @Input() userId!: string;
  public loading = false;
  public error = false;
  public memberships: InterestGroupProfile[] = [];
  public categories: Category[] = [];

  constructor(private userService: UserService) {}

  async ngOnInit() {
    await this.mustRefresh();
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.userId) {
      this.categories = [];
      this.memberships = [];

      await this.mustRefresh();
    }
  }

  public async mustRefresh() {
    if (this.userId) {
      this.loading = true;
      try {
        this.memberships = await firstValueFrom(
          this.userService.getUserMembership(this.userId)
        );

        this.categories = await firstValueFrom(
          this.userService.getUserCategories(this.userId)
        );
      } catch (error) {
        this.error = true;
      }
      this.loading = false;
    }
  }
}
