import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';

import {
  AutoUploadConfiguration,
  AutoUploadService,
  PagedAutoUploadConfiguration,
} from 'app/core/generated/circabc';
import { changeSort } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-auto-upload',
  templateUrl: './auto-upload.component.html',
  styleUrls: ['./auto-upload.component.scss'],
  preserveWhitespaces: true,
})
export class AutoUploadComponent implements OnInit {
  public configurations!: AutoUploadConfiguration[];
  public igId!: string;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems = 10;
  public loading = false;
  public showAddModal = false;

  constructor(
    private route: ActivatedRoute,
    private autoUploadService: AutoUploadService,
    private translateService: TranslocoService
  ) {}

  ngOnInit() {
    this.configurations = [];
    this.route.params.subscribe(
      async (params) => await this.listConfigurations(params)
    );
  }

  // configurations paging and listing

  private async listConfigurations(params: { [key: string]: string }) {
    this.igId = params.id;
    await this.loadConfigurations();
  }

  private async loadConfigurations() {
    this.loading = true;
    if (this.igId !== undefined) {
      await this.getAutoUploadConfigurations();
    }
    this.loading = false;
  }

  private async getAutoUploadConfigurations() {
    const result: PagedAutoUploadConfiguration = await firstValueFrom(
      this.autoUploadService.getAutoUploadEntries(
        this.igId,
        this.listingOptions.limit,
        this.listingOptions.page
      )
    );
    this.configurations = result.data;
    this.totalItems = result.total;

    // pretty print dateRestriction
    for (const configuration of this.configurations) {
      const values: string[] = this.prettyPrintDateRestriction(configuration);
      if (values.length === 0) {
        configuration.dateRestriction = '-';
      } else if (values.length > 1) {
        const day: string = this.translateService.translate(
          `cron-num-day.${values[1]}`
        );
        let hour: string;
        if (values[0] === '*') {
          hour = this.translateService.translate('label.every.hour.s');
        } else {
          hour = `${values[0]}:00`;
        }
        configuration.dateRestriction = `${day} ${hour}`;
      }
    }
  }

  public async configurationAdded() {
    this.showAddModal = false;
    await this.loadConfigurations();
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  private async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    if (this.igId !== undefined) {
      await this.getAutoUploadConfigurations();
    }
  }
  public async changeSort(sort: string) {
    this.listingOptions.sort = changeSort(this.listingOptions.sort, sort);
    await this.changePage(this.listingOptions);
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // configuration deletion

  public async deleteConfiguration(configuration: AutoUploadConfiguration) {
    if (
      this.igId !== undefined &&
      configuration.idConfiguration !== undefined
    ) {
      await firstValueFrom(
        this.autoUploadService.deleteAutoUploadEntry(
          this.igId,
          configuration.idConfiguration.toString()
        )
      );
      await this.loadConfigurations();
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public async redisplayListAfterDelete() {
    await this.changePage(this.listingOptions);
  }

  public async toggleConfiguration(configuration: AutoUploadConfiguration) {
    if (this.igId !== undefined && configuration.status !== 2) {
      await firstValueFrom(
        this.autoUploadService.putAutoUploadEntry(
          this.igId,
          // eslint-disable-next-line prefer-template
          String(configuration.idConfiguration),
          configuration.status === 0
        )
      );
      await this.loadConfigurations();
    }
  }

  public prettyPrintDateRestriction(
    configuration: AutoUploadConfiguration
  ): string[] {
    if (configuration.dateRestriction === undefined) {
      return [];
    }
    const match = configuration.dateRestriction.match(
      /\* \* (\d+|.*) \? \* (\d+|.*)/
    );
    if (match !== null) {
      const hour = match[1];
      const dayNum = match[2];
      return [hour, dayNum];
    }
    return [configuration.dateRestriction];
  }

  public trackById(
    _index: number,
    item: { idConfiguration?: string | number }
  ) {
    return item.idConfiguration;
  }
}
