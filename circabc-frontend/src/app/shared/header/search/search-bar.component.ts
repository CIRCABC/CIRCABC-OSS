import { debounceTime } from 'rxjs/operators';

import { Component, OnInit, viewChild, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { DomSanitizer } from '@angular/platform-browser';
import { assertDefined } from 'app/core/asserts';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SearchService } from 'app/core/generated/circabc/api/search.service';
import { HelpService } from 'app/core/generated/circabc/api/help.service';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import { SearchNode } from 'app/core/generated/circabc/model/searchNode';
import { AdvancedSearchComponent } from './advanced-search/advanced-search.component';
import { UserCardComponent } from '../../user-card/user-card.component';

@Component({
  selector: 'cbc-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss',
  preserveWhitespaces: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent,
    RouterLink,
    DatePipe,
    TranslocoModule,
    MatExpansionModule,
    MatCardModule,
    I18nPipe,
    MatTooltipModule,
    AdvancedSearchComponent,
    UserCardComponent,
  ],
})
export class SearchBarComponent implements OnInit {
  readonly groupId = input<string>();

  readonly forExplorer = input(false);

  readonly searchHit = output<string>();

  readonly renewSearchHit = output<string>();

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly panelResultIG = viewChild<any>('panelResultIG');

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly advancedSearchIG = viewChild<any>('advancedSearchIG');

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly panelResultHelp = viewChild<any>('panelResultHelp');

  public fileResults: SearchNode[] = [];
  public folderResults: SearchNode[] = [];
  public postResults: SearchNode[] = [];
  public forumResults: SearchNode[] = [];
  public topicResults: SearchNode[] = [];
  public eventResults: SearchNode[] = [];
  public informationResults: SearchNode[] = [];
  public otherResults: SearchNode[] = [];
  public helpResult!: HelpSearchResult;
  public searching = false;
  public noResult = false;
  public searchForm!: FormGroup;
  public lastSearchString = '';

  public constructor(
    private fb: FormBuilder,
    private sanitizer: DomSanitizer,
    private router: Router,
    private searchService: SearchService,
    private analyticsService: AnalyticsService,
    private helpService: HelpService,
    private translateService: TranslocoService
  ) {}

  public ngOnInit(): void {
    this.buildForm();
  }

  private buildForm(): void {
    this.searchForm = this.fb.group(
      {
        searchString: [''],
      },
      {
        updateOn: 'change',
      }
    );
    const debounceTimeInMiliSecond = 750;
    // eslint-disable-next-line @typescript-eslint/promise-function-async, @typescript-eslint/no-floating-promises
    this.searchForm.valueChanges
      .pipe(debounceTime(debounceTimeInMiliSecond))
      .subscribe((data) => this.onValueChanged(data)); // eslint-disable-line
    this.clearResults();
  }

  public clearResults() {
    this.fileResults = [];
    this.folderResults = [];
    this.postResults = [];
    this.forumResults = [];
    this.topicResults = [];
    this.eventResults = [];
    this.informationResults = [];
    this.otherResults = [];

    const panelResultIG = this.panelResultIG();
    const panelResultHelp = this.panelResultHelp();
    const advancedSearchIG = this.advancedSearchIG();
    if (panelResultIG && panelResultHelp && advancedSearchIG) {
      panelResultIG.close();
      panelResultHelp.close();
      advancedSearchIG.close();
    }
  }

  public resetForm(): void {
    this.clearResults();
    this.searchForm.controls.searchString.setValue('');
    this.noResult = false;
    this.searching = false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public async onValueChanged(data?: any) {
    this.clearResults();
    this.noResult = false;
    if (this.isRouteHelp()) {
      if (data.searchString !== '' && !this.searching) {
        this.searching = true;
        this.panelResultHelp().open();
        try {
          await this.helpService
            .helpSearch(data.searchString)
            .subscribe((result) => {
              this.helpResult = result;

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
        } catch (_error) {
          this.noResult = true;
        }

        this.searching = false;
      }
    } else {
      // done especially for a IG search
      const groupId = this.groupId();
      if (data.searchString !== '' && !this.searching && groupId) {
        this.searching = true;
        this.panelResultIG().open();
        try {
          const res = await firstValueFrom(
            this.searchService.getSearch(
              data.searchString,
              groupId,
              data.language,
              undefined,
              data.searchIn,
              data.creator,
              data.creationDateFrom,
              data.creationDateTo,
              data.modifiedDateFrom,
              data.modifiedDateTo,
              data.keywords,
              data.status,
              data.securityRanking,
              data.version
            )
          );
          if (res.data) {
            this.splitResults(res.data);

            if (res.total === 0) {
              this.noResult = true;
            }
            this.analyticsService.trackSiteSearch(
              data.searchString,
              `search interest group -${groupId}`,
              res.total
            );
          }
        } catch (_error) {
          this.noResult = true;
        }

        this.searching = false;
      }

      if (this.forExplorer()) {
        this.searchHit.emit(data.searchString);
      }
    }
  }

  splitResults(data: SearchNode[]) {
    for (const sn of data) {
      switch (sn.resultType) {
        case 'folder': {
          this.folderResults.push(sn);
          break;
        }
        case 'file': {
          this.fileResults.push(sn);
          break;
        }
        case 'topic': {
          this.topicResults.push(sn);
          break;
        }
        case 'post': {
          this.postResults.push(sn);
          break;
        }
        case 'forum': {
          this.forumResults.push(sn);
          break;
        }
        case 'information': {
          this.informationResults.push(sn);
          break;
        }
        default:
          if (sn.service === 'events') {
            this.eventResults.push(sn);
          } else {
            this.otherResults.push(sn);
          }
      }
    }
  }

  hasResults(): boolean {
    return (
      this.hasFolderResults() ||
      this.hasFileResults() ||
      this.hasTopicResults() ||
      this.hasForumResults() ||
      this.hasPostResults() ||
      this.hasEventResults() ||
      this.hasInformationResults() ||
      this.hasOtherResults()
    );
  }

  hasFileResults(): boolean {
    return this.fileResults.length > 0;
  }

  hasFolderResults(): boolean {
    return this.folderResults.length > 0;
  }

  hasForumResults(): boolean {
    return this.forumResults.length > 0;
  }

  hasTopicResults(): boolean {
    return this.topicResults.length > 0;
  }

  hasPostResults(): boolean {
    return this.postResults.length > 0;
  }

  hasEventResults(): boolean {
    return this.eventResults.length > 0;
  }

  hasInformationResults(): boolean {
    return this.informationResults.length > 0;
  }

  hasOtherResults(): boolean {
    return this.otherResults.length > 0;
  }

  private getContext(node: SearchNode, item: 'forum' | 'forum/topic'): string {
    if (node.service === 'library') {
      return node.service;
    }
    return item;
  }

  getTopicContext(node: SearchNode): string {
    return this.getContext(node, 'forum');
  }

  getPostContext(node: SearchNode): string {
    return this.getContext(node, 'forum/topic');
  }

  getPostEndContext(node: SearchNode): string {
    if (node.service === 'library') {
      return '/details';
    }
    return '';
  }

  public getModified(searchNode: SearchNode): string | null {
    if (searchNode.properties) {
      return searchNode.properties.modified;
    }
    return null;
  }

  public hasText(): boolean {
    return (
      this.searchForm.value.searchString !== '' &&
      this.searchForm.value.searchString !== undefined
    );
  }

  public requestRefresh() {
    if (this.hasText()) {
      this.renewSearchHit.emit(this.searchForm.value.searchString);
    }
  }

  public hasResult() {
    if (this.helpResult) {
      if (
        (this.helpResult.articles && this.helpResult.articles.length > 0) ||
        (this.helpResult.categories && this.helpResult.categories.length > 0) ||
        (this.helpResult.links && this.helpResult.links.length > 0)
      ) {
        return true;
      }
      return false;
    }
    return false;
  }

  public sanitizeLinkRef(href: string | undefined) {
    if (href) {
      return this.sanitizer.bypassSecurityTrustUrl(href);
    }
    return undefined;
  }

  public goToCategoryLink(id: string | undefined) {
    assertDefined(id);
    this.resetForm();
    this.router.navigate(['/help/category', id]);
  }

  public goToArticleLink(
    categoryId: string | undefined,
    articleId: string | undefined
  ) {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.resetForm();
    this.router.navigate(['/help/category', categoryId, 'article', articleId]);
  }

  public openCloseCustonSearch() {
    this.lastSearchString = this.searchForm.value.searchString;
    const advancedSearchIG = this.advancedSearchIG();
    if (advancedSearchIG.expanded) {
      advancedSearchIG.close();
    } else {
      this.searchForm.controls.searchString.setValue('');
      setTimeout(() => {
        this.advancedSearchIG().open();
      }, 900);
    }
  }

  public getName(searchNode: SearchNode) {
    const lang = this.translateService.getActiveLang();

    if (searchNode.title?.[lang] && searchNode.title[lang].trim().length > 0) {
      return this.cutBigText(searchNode.title[lang].trim());
    }

    if (searchNode.title?.en && searchNode.title.en.trim().length > 0) {
      return this.cutBigText(searchNode.title.en.trim());
    }

    if (searchNode.properties?.title) {
      const title = searchNode.properties.title.slice(4, -1);
      if (title.trim().length > 0) {
        return this.cutBigText(title);
      }
    }

    if (searchNode.name?.includes('news_')) {
      return this.cutBigText(searchNode.name?.slice(5));
    }
    return this.cutBigText(searchNode.name);
  }

  cutBigText(text: string | undefined) {
    if (text) {
      if (window.screen.width < 400) {
        return text.slice(0, 20) + (text.length > 30 ? '...' : '');
      }
      if (window.screen.width < 600) {
        return text.slice(0, 40) + (text.length > 40 ? '...' : '');
      }

      if (window.screen.width < 700) {
        return text.slice(0, 50) + (text.length > 50 ? '...' : '');
      }

      return text.slice(0, 60) + (text.length > 60 ? '...' : '');
    }
    return '';
  }

  public isRouteHelp(): boolean {
    return this.router.url.includes('/help');
  }

  public isExpired(date: string) {
    return new Date(date) < new Date();
  }
}
