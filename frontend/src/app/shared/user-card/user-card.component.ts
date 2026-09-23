import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  resource,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { UserCacheService } from 'app/core/user-cache.service';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

/**
 * Renders a compact, clickable user card that displays a user's avatar and
 * resolved full name. When toggled, it reveals a popover with additional user
 * details (as defined in the component template).
 *
 * The component resolves user information from the {@link UserCacheService}
 * based on the provided {@link UserCardComponent.userId | userId} input. It
 * also listens for document-level clicks in order to emit a
 * {@link UserCardComponent.clickOutside | clickOutside} event and hide the
 * popover when the user clicks outside of the card.
 *
 * @remarks
 * Special user identifiers are handled explicitly: a `null`/`undefined` id
 * resolves to the label `"unknown"`, and the id `"System"` is displayed
 * verbatim without a lookup.
 */
@Component({
  selector: 'cbc-user-card',
  templateUrl: './user-card.component.html',
  styleUrl: './user-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DownloadPipe, SecurePipe, TranslocoModule],
})
export class UserCardComponent {
  /** Service used to resolve and cache {@link User} details by id. */
  private readonly userCacheService = inject(UserCacheService);

  /**
   * Identifier of the user to display. May be `null`/`undefined` (rendered as
   * `"unknown"`) or the special value `"System"`.
   * @remarks Angular signal input.
   */
  readonly userId = input<string | null>();
  /**
   * Whether the popover should be aligned to the right side of the card.
   * @remarks Angular signal input; defaults to `false`.
   */
  readonly rightSide = input(false);
  /**
   * Whether the card is disabled, preventing the popover from toggling.
   * @remarks Angular signal input; defaults to `false`.
   */
  readonly disabled = input(false);
  /**
   * Emits when a click is detected outside the boundaries of this component,
   * carrying the originating {@link MouseEvent}.
   * @remarks Angular signal output.
   */
  public readonly clickOutside = output<MouseEvent>();

  /** The resolved user, populated via {@link UserCardComponent.userResource}. */
  public readonly user = computed(() => this.userResource.value()?.user);
  /** Whether the details popover is currently visible. */
  public visible = false;
  /** Host element reference, used for outside-click detection. */
  private readonly elementRef: ElementRef;
  /** Display name shown on the card; derived from the resolved user. */
  public readonly fullName = computed(
    () => this.userResource.value()?.fullName ?? ''
  );

  /**
   * Resolves the display name (and, when applicable, the {@link User}) for
   * the configured {@link UserCardComponent.userId | userId}:
   * - `null`/`undefined` id resolves to the name `"unknown"`.
   * - The special id `"System"` is used verbatim.
   * - Otherwise the user is fetched from {@link UserCacheService}; the full
   *   name is built from first/last name, falling back to `[userId]` when
   *   those are empty, or to the raw id when the lookup fails.
   *
   * Errors are caught in the loader (rather than left to the resource's error
   * state) since this component has no dedicated error UI; on failure the
   * card falls back to displaying the raw id.
   */
  private readonly userResource = resource({
    params: () => ({ userId: this.userId() }),
    loader: async ({ params: { userId } }) => {
      if (userId === undefined || userId === null) {
        return { user: undefined, fullName: 'unknown' };
      }
      if (userId === 'System') {
        return { user: undefined, fullName: userId };
      }
      try {
        const user = await this.userCacheService.getUser(userId);
        const fullName =
          user.firstname !== '' && user.lastname !== ''
            ? `${user.firstname} ${user.lastname}`
            : `[${userId}]`;
        return { user, fullName };
      } catch (error) {
        console.error(
          `Error getting info about user '${userId}'. User card will not be displayed.`
        );
        console.error(error);
        return { user: undefined, fullName: userId };
      }
    },
  });

  constructor() {
    const myElement = inject(ElementRef);

    this.elementRef = myElement;
  }

  /**
   * Document-level click handler that closes the card and notifies listeners
   * when a click occurs outside this component's host element.
   *
   * @param event - The originating mouse event, forwarded via
   * {@link UserCardComponent.clickOutside} when the click is outside.
   * @param targetElement - The element that was clicked; when `null` the
   * handler is a no-op.
   */
  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: EventTarget | null): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.visible = false;
    }
  }

  /**
   * Toggles the visibility of the details popover, unless the card is
   * {@link UserCardComponent.disabled | disabled}.
   */
  public toggleVisible() {
    if (!this.disabled()) {
      this.visible = !this.visible;
    }
  }
}
