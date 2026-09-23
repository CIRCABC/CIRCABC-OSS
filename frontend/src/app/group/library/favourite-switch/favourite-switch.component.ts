import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnInit,
} from '@angular/core';

import {
  FavouritesService,
  Node as ModelNode,
  SimpleId,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Renders a toggle control (a "favourite" star/switch) for a library node,
 * allowing the current user to add or remove that node from their personal
 * favourites.
 *
 * The control is only shown for authenticated (non-guest) users and is hidden
 * for link-type nodes (`filelink` / `folderlink`). Toggling delegates to the
 * generated {@link FavouritesService} to create or delete the favourite entry
 * for the current user, and mutates the bound node's `favourite` flag in place
 * to reflect the new state.
 *
 * Key collaborators:
 * - {@link FavouritesService} — backend calls to add/remove favourites.
 * - {@link LoginService} — resolves the current username and guest status.
 */
@Component({
  selector: 'cbc-favourite-switch',
  templateUrl: './favourite-switch.component.html',
  styleUrl: './favourite-switch.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class FavouriteSwitchComponent implements OnInit {
  /** Generated API service used to add or remove favourite entries. */
  private readonly favouritesService = inject(FavouritesService);
  /** Provides the current user's username and guest status. */
  private readonly loginService = inject(LoginService);

  /**
   * Required input: the library node whose favourite state this switch
   * reflects and toggles. Its `favourite` flag is updated in place when the
   * user toggles the control.
   */
  readonly node = input.required<ModelNode>();
  /**
   * Re-entrancy guard that prevents concurrent toggle requests while a
   * previous add/remove call is still in flight.
   */
  private isWorking = false;
  /**
   * Whether the switch should be displayed. `true` only for authenticated
   * users and non-link node types; otherwise the control is hidden.
   */
  public shaw = false;
  /**
   * OnPush-safe mirror of the bound node's `favourite` flag that drives the
   * template. It tracks the current node input, and is written explicitly in
   * {@link toggleFav} so the view re-renders after the async add/remove call
   * completes (a plain field would not mark the OnPush view dirty from a
   * promise continuation).
   */
  readonly favourite = linkedSignal(() => this.node().favourite === true);

  /**
   * Lifecycle hook that computes the initial visibility ({@link shaw}) of the
   * switch. The control is shown only when the user is not a guest and the
   * node type is neither `filelink` nor `folderlink`.
   */
  ngOnInit(): void {
    this.shaw = false;
    const node = this.node();
    if (!this.loginService.isGuest() && node.type) {
      this.shaw = !(
        node.type.includes('filelink') || node.type.includes('folderlink')
      );
    }
  }

  /**
   * Toggles the favourite state of the bound node for the current user.
   *
   * If the node is currently a favourite it is removed via
   * {@link FavouritesService.deleteFavourite}; otherwise it is added via
   * {@link FavouritesService.postFavourite}. The node's `favourite` flag is
   * updated in place on success. Calls are guarded by {@link isWorking} so
   * that overlapping invocations are ignored.
   *
   * @returns A promise that resolves once the add/remove operation completes.
   */
  async toggleFav() {
    if (this.isWorking) {
      return;
    }
    try {
      this.isWorking = true;
      const node = this.node();
      if (node) {
        if (node.favourite && node.id) {
          await this.favouritesService.deleteFavouriteAsync({
            userId: this.loginService.getCurrentUsername(),
            nodeId: node.id,
          });
          node.favourite = false;
          this.favourite.set(false);
        } else {
          const body: SimpleId = { id: node.id as string };
          await this.favouritesService.postFavouriteAsync({
            userId: this.loginService.getCurrentUsername(),
            simpleId: body,
          });
          node.favourite = true;
          this.favourite.set(true);
        }
      }
    } finally {
      this.isWorking = false;
    }
  }

  /**
   * Reports whether the bound node is currently marked as a favourite.
   *
   * @returns `true` if the node's `favourite` flag is set; otherwise `false`.
   */
  isFavourite(): boolean {
    return this.favourite();
  }
}
