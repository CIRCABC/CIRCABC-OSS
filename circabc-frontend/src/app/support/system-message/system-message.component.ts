import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AppMessageService,
  PagedAppMessages,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { firstValueFrom } from 'rxjs';
import { OldUiConfigurationComponent } from './old-ui-configuration/old-ui-configuration.component';
import { TemplateRendererComponent } from './template-renderer/template-renderer.component';

@Component({
  selector: 'cbc-system-message',
  templateUrl: './system-message.component.html',
  styleUrl: './system-message.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    PagerComponent,
    OldUiConfigurationComponent,
    TemplateRendererComponent,
    TranslocoModule,
  ],
})
export class SystemMessageComponent implements OnInit {
  public templates!: PagedAppMessages;
  public loading = false;
  public listingOptions: ListingOptions = { page: 1, limit: 5, sort: '' };
  public totalItems = 5;
  public showDeleteModal = false;

  // add in  order to compile
  public showModalCreate = false;

  constructor(private appMessageService: AppMessageService) {}

  async ngOnInit() {
    await this.loadTemplates();
  }

  private async loadTemplates() {
    this.loading = true;

    try {
      this.templates = await firstValueFrom(
        this.appMessageService.getPagedAppMessagesTemplate(
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );
      this.totalItems = this.templates.total;
    } catch (_error) {
      console.error('problem getting the list of templates');
    }

    this.loading = false;
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  private async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    await this.loadTemplates();
  }

  public async refresh() {
    this.showDeleteModal = false;
    await this.loadTemplates();
  }
}
