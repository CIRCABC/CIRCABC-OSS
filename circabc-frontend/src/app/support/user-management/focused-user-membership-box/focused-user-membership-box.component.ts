import {
  Component,
  OnChanges,
  OnInit,
  SimpleChanges,
  input,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  Category,
  InterestGroupProfile,
  UserService,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { CategoryBoxComponent } from './category-box/category-box.component';
import { GroupBoxComponent } from './group-box/group-box.component';

@Component({
  selector: 'cbc-focused-user-membership-box',
  templateUrl: './focused-user-membership-box.component.html',
  styleUrl: './focused-user-membership-box.component.scss',
  imports: [
    HorizontalLoaderComponent,
    SpinnerComponent,
    CategoryBoxComponent,
    GroupBoxComponent,
    TranslocoModule,
  ],
})
export class FocusedUserMembershipBoxComponent implements OnInit, OnChanges {
  readonly userId = input.required<string>();
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
    const userId = this.userId();
    if (userId) {
      this.loading = true;
      try {
        this.memberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );

        this.categories = await firstValueFrom(
          this.userService.getUserCategories(userId)
        );
      } catch (_error) {
        this.error = true;
      }
      this.loading = false;
    }
  }
}
