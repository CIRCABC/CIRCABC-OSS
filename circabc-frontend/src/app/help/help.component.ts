import { Component } from '@angular/core';
import { HelpSearchResult, HelpService } from 'app/core/generated/circabc';
import { AnalyticsService } from 'app/core//analytics.service';
import { TranslocoService } from '@ngneat/transloco';

@Component({
  selector: 'cbc-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss'],
  preserveWhitespaces: true,
})
export class HelpComponent {
  public showSearch = false;
  public searching = false;
  public resultSearch: HelpSearchResult | undefined;

  constructor(
    private helpService: HelpService,
    private analyticsService: AnalyticsService,
    public translateService: TranslocoService
  ) {}

  public async searchInHelp(data: string) {
    if (data === '') {
      this.showSearch = false;
      this.resultSearch = undefined;
    } else {
      this.showSearch = true;
      this.searching = true;
      this.helpService.helpSearch(data).subscribe((result) => {
        this.resultSearch = result;
        this.searching = false;

        this.analyticsService.trackSiteSearch(
          data,
          'help-articles',
          result.articles?.length ?? 0
        );
        this.analyticsService.trackSiteSearch(
          data,
          'help-categories',
          result.categories?.length ?? 0
        );
        this.analyticsService.trackSiteSearch(
          data,
          'help-links',
          result.links?.length ?? 0
        );
      });
    }
  }

  public closeSearch() {
    this.showSearch = false;
    this.resultSearch = undefined;
    this.searching = false;
  }

  public linkClick() {
    this.closeSearch();
  }
}
