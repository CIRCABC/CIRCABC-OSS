import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, Data, RouterOutlet } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { InterestGroup } from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { FlatMessageComponent } from 'app/shared/flat-message/flat-message.component';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { Subscription } from 'rxjs';

/**
 * Root shell component for an interest group workspace (selector `cbc-group`).
 *
 * Rendered when the user navigates into a specific interest group, this
 * component provides the common chrome around the group's service views: it
 * displays the group {@link HeaderComponent}, the {@link NavigatorComponent}
 * for switching between group services, a {@link FlatMessageComponent} for
 * inline notifications, and a `<router-outlet>` (`RouterOutlet`) into which the
 * active group sub-feature (library, forum, agenda, information, etc.) is
 * rendered.
 *
 * Responsibilities:
 * - Resolves the current {@link InterestGroup} from the activated route data
 *   and records the visit via {@link VisitedGroupService}.
 * - Keeps the group's `libraryId` in sync with values emitted by
 *   {@link LibraryIdService}.
 * - Exposes whether the current session is a guest/visitor via
 *   {@link LoginService}.
 *
 * Key collaborators: {@link ActivatedRoute}, {@link LoginService},
 * {@link VisitedGroupService} and {@link LibraryIdService}.
 */
@Component({
  selector: 'cbc-group',
  templateUrl: './group.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    FlatMessageComponent,
    RouterOutlet,
    TranslocoModule,
  ],
})
export class GroupComponent implements OnInit, OnDestroy {
  /** Provides access to the route data that carries the resolved group. */
  private readonly route = inject(ActivatedRoute);
  /** Used to determine the authentication state of the current session. */
  private readonly loginService = inject(LoginService);
  /** Records that the current group has been visited by the user. */
  private readonly visitedGroupService = inject(VisitedGroupService);
  /** Emits updates to the group's library node identifier. */
  private readonly libraryIdService = inject(LibraryIdService);

  /**
   * The interest group currently being displayed, resolved from the route data
   * during {@link GroupComponent.ngOnInit}.
   */
  public readonly group = signal<InterestGroup | undefined>(undefined);

  /**
   * Subscription to {@link LibraryIdService.libraryIdSubject$}, retained so it
   * can be released in {@link GroupComponent.ngOnDestroy}.
   */
  private libraryIdSubscription$!: Subscription;

  /**
   * Angular lifecycle hook. Subscribes to the route data to obtain the current
   * {@link InterestGroup}, marks it as visited, and starts listening for
   * library id updates.
   */
  public ngOnInit() {
    this.route.data.subscribe((value: Data) => {
      this.group.set(value.group);
      this.visitedGroupService.visitGroup(value.group);
    });
    this.subscribe();
  }

  /**
   * Angular lifecycle hook. Releases the library id subscription to avoid
   * memory leaks when the component is destroyed.
   */
  public ngOnDestroy(): void {
    this.unsubscribe();
  }

  /**
   * Subscribes to library id updates and applies each emitted value to the
   * current group's `libraryId`.
   */
  private subscribe() {
    this.libraryIdSubscription$ =
      this.libraryIdService.libraryIdSubject$.subscribe((libraryId: string) => {
        // Reassign immutably so OnPush observes a new group reference.
        this.group.update((group) => (group ? { ...group, libraryId } : group));
      });
  }

  /**
   * Unsubscribes from the library id updates stream.
   */
  private unsubscribe() {
    this.libraryIdSubscription$.unsubscribe();
  }
  /**
   * Indicates whether the current user is browsing the group as an
   * unauthenticated guest/visitor.
   *
   * @returns `true` if the session is a guest session, otherwise `false`.
   */
  public isAccessingAsVisitor(): boolean {
    return this.loginService.isGuest();
  }
}
