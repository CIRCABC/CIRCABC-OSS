import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  FavouritesService,
  Node as ModelNode,
  NodesService,
  PagedNodes,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';

// Node as ModelNode,

/**
 * Dashboard widget that renders the current user's favourite nodes
 * (files and folders) in a paginated list.
 *
 * The component loads the favourites for the currently logged-in user via
 * the {@link FavouritesService}, displays them with pagination support, and
 * lets the user navigate to the corresponding item inside its owning group's
 * library. A horizontal loader is shown while data is being fetched and a
 * pager is displayed when the number of favourites exceeds the page limit.
 *
 * Key collaborators:
 * - {@link FavouritesService} — retrieves the paged list of favourites.
 * - {@link LoginService} — provides the current username.
 * - {@link NodesService} — resolves the owning group of a favourite node.
 * - {@link Router} — navigates to the library view of a selected node.
 */
@Component({
  selector: 'cbc-user-favourites',
  templateUrl: './user-favourites.component.html',
  styleUrl: './user-favourites.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HorizontalLoaderComponent, PagerComponent, TranslocoModule],
})
export class UserFavouritesComponent {
  private readonly favouritesService = inject(FavouritesService);
  private readonly loginService = inject(LoginService);
  private readonly nodesService = inject(NodesService);
  private readonly router = inject(Router);

  /** Number of favourites requested per page. */
  public readonly limit = 10;
  /** The 1-based page currently displayed. */
  public readonly page = signal(1);

  /**
   * The current page of the user's favourites, loaded reactively. The request
   * re-runs automatically whenever {@link page} changes.
   */
  private readonly favouritesResource = resource({
    defaultValue: { data: [], total: 0 } as PagedNodes,
    params: () => ({
      userId: this.loginService.getCurrentUsername(),
      limit: this.limit,
      page: this.page(),
    }),
    loader: async ({ params }) => {
      try {
        return await this.favouritesService.getFavouritesAsync(params);
      } catch (error) {
        console.error(error);
        return { data: [], total: 0 };
      }
    },
  });

  /** Whether a favourites request is currently in flight. */
  public readonly loading = this.favouritesResource.isLoading;
  /** The current page of favourite nodes returned by the backend. */
  public readonly pagedFavourites = this.favouritesResource.value;
  /** Total number of favourites available for the user (across all pages). */
  public readonly totalItems = computed(() => this.pagedFavourites().total);

  /**
   * Determines whether a node represents a file (i.e. not a folder).
   *
   * @param node The node to inspect.
   * @returns `true` if the node has a type that does not include `folder`;
   *          `false` when it is a folder or has no type.
   */
  isFile(node: ModelNode): boolean {
    if (node.type) {
      return !node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether a node represents a folder.
   *
   * @param node The node to inspect.
   * @returns `true` if the node has a type that includes `folder`;
   *          `false` otherwise.
   */
  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Resolves the group owning the given node and navigates to its location in
   * the group's library. Folders navigate to the library listing, while files
   * navigate to the node's details view.
   *
   * @param node The favourite node to open. No navigation occurs when the
   *             node has no id.
   * @returns A promise that resolves once the owning group has been resolved
   *          and navigation has been triggered.
   */
  async openLink(node: ModelNode) {
    if (node.id) {
      const group = await this.nodesService.getGroupAsync({ id: node.id });

      if (node.type?.includes('folder')) {
        this.router.navigate(['/group', group.id, 'library', node.id]);
      } else {
        this.router.navigate([
          '/group',
          group.id,
          'library',
          node.id,
          'details',
        ]);
      }
    }
  }

  /**
   * Changes the active page, which triggers a reload of the favourites for
   * that page via the reactive resource.
   *
   * @param p The 1-based page number to load.
   */
  changePage(p: number) {
    this.page.set(p);
  }

  /**
   * Indicates whether the pager should be displayed.
   *
   * @returns `true` when the total number of favourites exceeds the page
   *          limit and pagination controls are needed; `false` otherwise.
   */
  public isPagerVisible(): boolean {
    return this.totalItems() > this.limit;
  }
}
