import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { PagedShares, Share, SpaceService } from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { ShareSpaceComponent } from './share-space/share-space.component';

/**
 * Component that renders the management view for space sharing within a
 * document library node.
 *
 * It displays the list of interest groups a space is currently shared with,
 * as a paginated listing, and provides the actions needed to manage those
 * shares: inviting another interest group to share the space (via the
 * {@link ShareSpaceComponent} modal) and removing an existing share.
 *
 * The target space is resolved from the `nodeId` route parameter, and all
 * share data is retrieved and mutated through the generated
 * {@link SpaceService} API client.
 *
 * @remarks
 * The rendered template relies on {@link HorizontalLoaderComponent} for the
 * loading indicator, {@link PagerComponent} for pagination controls,
 * {@link InlineDeleteComponent} for per-share removal confirmation and
 * {@link ShareSpaceComponent} for the invite modal.
 */
@Component({
  selector: 'cbc-manage-space-sharing',
  templateUrl: './manage-space-sharing.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    PagerComponent,
    InlineDeleteComponent,
    ShareSpaceComponent,
    TranslocoModule,
  ],
})
export class ManageSpaceSharingComponent implements OnInit {
  /** Provides access to the current route parameters (used to read `nodeId`). */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to read, create and delete space shares. */
  private readonly spaceService = inject(SpaceService);
  private readonly loadingService = inject(LoadingService);

  /** The shares (interest groups) the current space is shared with, for the active page. */
  public readonly shares = signal<Share[] | undefined>(undefined);
  /** Identifier of the space node whose shares are being managed (from the `nodeId` route param). */
  public readonly spaceId = signal<string | undefined>(undefined);
  /** Current pagination and sorting options for the shares listing. */
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  /** Total number of shares available across all pages, used to drive the pager. */
  public readonly totalItems = signal(10);

  /** Whether a share listing request is currently in flight. */
  public readonly loading = signal(false);

  /** Whether the "share space" invite modal is currently displayed. */
  public showShareSpaceModal = false;

  /**
   * Angular lifecycle hook. Initialises the share list and subscribes to route
   * parameter changes to (re)load the shares for the resolved space.
   */
  ngOnInit() {
    this.shares.set([]);
    this.route.params.subscribe(
      async (params) => await this.listShares(params)
    );
  }

  // shares paging and listing

  /**
   * Resolves the target space from the route parameters and loads its shares.
   *
   * @param params - Route parameters map; the `nodeId` entry is used as the space identifier.
   * @returns A promise that resolves once the shares have been loaded.
   */
  private async listShares(params: { [key: string]: string }) {
    this.spaceId.set(params.nodeId);
    await this.loadShares();
  }

  /**
   * Fetches the current page of shares for the active space from the backend
   * and updates {@link shares} and {@link totalItems}. Does nothing when no
   * space is set. Toggles {@link loading} around the request.
   *
   * @returns A promise that resolves once the shares have been retrieved and stored.
   */
  private async loadShares() {
    const spaceId = this.spaceId();
    if (spaceId === undefined) {
      return;
    }
    const result: PagedShares | undefined = await this.loadingService.run(
      this.loading,
      () =>
        this.spaceService.getShareSpacesAsync({
          id: spaceId,
          limit: this.listingOptions.limit,
          page: this.listingOptions.page,
        })
    );
    if (result) {
      this.shares.set(result.data);
      this.totalItems.set(result.total);
    }
  }

  // paging

  /**
   * Navigates the shares listing to the given page and reloads the data.
   *
   * @param page - The 1-based page number to display.
   * @returns A promise that resolves once the requested page has been loaded.
   */
  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  /**
   * Applies new listing options and reloads the shares accordingly.
   *
   * @param listingOptions - The pagination and sorting options to apply.
   * @returns A promise that resolves once the shares have been reloaded.
   */
  public async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    await this.loadShares();
  }

  /**
   * Updates the page size, resets to the first page and reloads the shares.
   *
   * @param limit - The maximum number of shares to display per page.
   * @returns A promise that resolves once the shares have been reloaded.
   */
  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // actions

  /**
   * Opens the "share space" modal so another interest group can be invited to
   * share the current space.
   */
  public inviteIGToShareSpace() {
    this.showShareSpaceModal = true;
  }

  /**
   * Handles completion of the share-space invite modal: closes the modal and,
   * on success, reloads the shares to reflect the newly created share.
   *
   * @param result - The outcome emitted by the invite modal.
   * @returns A promise that resolves once any subsequent reload has completed.
   */
  public async refresh(result: ActionEmitterResult) {
    this.showShareSpaceModal = false;
    if (result.result === ActionResult.SUCCEED) {
      await this.loadShares();
    }
  }

  /**
   * Removes an existing share (interest group) from the current space and
   * reloads the listing.
   *
   * @param share - The share to remove; its `igId` identifies the interest group.
   * @returns A promise that resolves once the share has been deleted and the list reloaded.
   */
  public async removeShare(share: Share) {
    await this.spaceService.deleteShareSpaceAsync({
      id: this.spaceId() as string,
      sharedIGId: share.igId as string,
    });
    await this.loadShares();
  }
}
