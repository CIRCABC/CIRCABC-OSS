import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { User, UserService } from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { MemberCardComponent } from './member-card/member-card.component';

/**
 * Standalone component that renders the account details page for a single
 * member (user).
 *
 * The component reads the `userid` route parameter from the current
 * {@link ActivatedRoute}, fetches the corresponding {@link User} through the
 * generated {@link UserService}, and displays the result via the embedded
 * {@link MemberCardComponent}. While the user is being loaded a
 * {@link HorizontalLoaderComponent} is shown. It also exposes a "go back"
 * action that returns the browser to the previous location.
 *
 * @remarks
 * Selector: `cbc-member-account`. Uses OnPush change detection and relies on
 * Transloco for its translations.
 */
@Component({
  selector: 'cbc-member-account',
  templateUrl: './member-account.component.html',
  styleUrl: './member-account.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HorizontalLoaderComponent, MemberCardComponent, TranslocoModule],
})
export class MemberAccountComponent implements OnInit {
  /** Provides access to the current route, used to read the `userid` param. */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to retrieve the member's user details. */
  private readonly userService = inject(UserService);
  /** Angular {@link Location} service used to navigate back in history. */
  private readonly location = inject(Location);

  /**
   * The member currently being displayed. Populated asynchronously once the
   * user has been fetched; use {@link loading} to know when it is ready.
   */
  public readonly user = signal<User | undefined>(undefined);
  /** Indicates whether a member fetch is currently in progress. */
  public readonly loading = signal(false);

  /**
   * Angular lifecycle hook. Subscribes to the route parameters and triggers
   * loading of the member identified by the `userid` route parameter whenever
   * those parameters emit.
   */
  ngOnInit() {
    if (this.route.params) {
      this.route.params.subscribe(async (params) =>
        this.loadMember(params.userid)
      );
    }
  }

  /**
   * Fetches the details of the given member and stores them in {@link user}.
   *
   * Toggles {@link loading} to `true` while the request is in flight and back
   * to `false` once it completes. If no `userid` is provided the method does
   * nothing.
   *
   * @param userid - Identifier of the user to load.
   * @returns A promise that resolves once the user has been fetched (or
   * immediately when `userid` is falsy).
   */
  async loadMember(userid: string) {
    if (userid) {
      this.loading.set(true);
      this.user.set(await this.userService.getUserAsync({ userId: userid }));
      this.loading.set(false);
    }
  }

  /**
   * Navigates the browser back to the previous location in the history stack.
   */
  public goBack() {
    this.location.back();
  }
}
