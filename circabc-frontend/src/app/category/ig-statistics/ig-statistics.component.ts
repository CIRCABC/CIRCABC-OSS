import { DatePipe } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit, Optional } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  PagedStatisticsContents,
  StatisticsContent,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { ALF_BASE_PATH } from 'app/core/variables';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-ig-statistics',
  templateUrl: './ig-statistics.component.html',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    PagerComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class IgStatisticsComponent implements OnInit, OnDestroy {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private interval: any;

  public contents!: StatisticsContent[];
  public listingOptions: ListingOptions = { page: 1, limit: 5, sort: '' };
  public totalItems = 5;

  public categoryId!: string;
  public loading = false;
  private alfrescoServicePath!: string;

  public showAddModal = false;

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private saveAsService: SaveAsService,
    @Optional()
    @Inject(ALF_BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.alfrescoServicePath = basePath;
    }
  }

  ngOnInit() {
    this.contents = [];

    this.route.params.subscribe(
      async (params) => await this.listContents(params)
    );
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
  }

  private async listContents(params: { [key: string]: string }) {
    this.categoryId = params.id;
    await this.loadContents();
    this.interval = setInterval(async () => {
      await this.loadContents();
    }, 10000);
  }

  private async loadContents() {
    this.loading = true;
    if (this.categoryId !== undefined) {
      const result: PagedStatisticsContents = await firstValueFrom(
        this.categoryService.getCategoryStatistics(
          this.categoryId,
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );
      this.contents = result.data;
      this.totalItems = result.total;
    }
    this.loading = false;
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  public async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    if (this.categoryId !== undefined) {
      const result: PagedStatisticsContents = await firstValueFrom(
        this.categoryService.getCategoryStatistics(
          this.categoryId,
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );
      this.contents = result.data;
      this.totalItems = result.total;
    }
  }

  public async generateStatistics() {
    await firstValueFrom(
      this.categoryService.postCategoryStatistics(this.categoryId)
    );
  }

  public download(content: StatisticsContent) {
    const url = `${this.alfrescoServicePath}/node/content${(
      content.downloadURL as string
    ).substring(4, 63)}`;
    this.saveAsService.saveUrlAs(url, content.name as string);
  }
}
