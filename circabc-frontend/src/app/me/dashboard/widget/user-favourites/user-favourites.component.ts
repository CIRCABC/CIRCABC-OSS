import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  FavouritesService,
  Node as ModelNode,
  NodesService,
  PagedNodes,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { firstValueFrom } from 'rxjs';

// Node as ModelNode,

@Component({
  selector: 'cbc-user-favourites',
  templateUrl: './user-favourites.component.html',
  styleUrl: './user-favourites.component.scss',
  preserveWhitespaces: true,
  imports: [HorizontalLoaderComponent, PagerComponent, TranslocoModule],
})
export class UserFavouritesComponent implements OnInit {
  public loading = false;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems = 0;
  public pagedFavourites!: PagedNodes;

  constructor(
    private favouritesService: FavouritesService,
    private loginService: LoginService,
    private nodesService: NodesService,
    private router: Router
  ) {}

  async ngOnInit() {
    await this.loadFavourites();
  }

  async loadFavourites() {
    this.loading = true;
    try {
      this.pagedFavourites = await firstValueFrom(
        this.favouritesService.getFavourites(
          this.loginService.getCurrentUsername(),
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );
      this.totalItems = this.pagedFavourites.total;
    } catch (e) {
      this.pagedFavourites = { data: [], total: 0 };
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  isFile(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') === -1;
    }
    return false;
  }

  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    }
    return false;
  }

  async openLink(node: ModelNode) {
    if (node.id) {
      const group = await firstValueFrom(this.nodesService.getGroup(node.id));

      if (node.type && node.type.indexOf('folder') > -1) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['/group', group.id, 'library', node.id]);
      } else {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
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

  async changePage(p: number) {
    this.listingOptions.page = p;
    await this.loadFavourites();
  }

  public isPagerVisible(): boolean {
    return this.totalItems > this.listingOptions.limit;
  }
}
