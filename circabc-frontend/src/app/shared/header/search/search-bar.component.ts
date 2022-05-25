import { debounceTime } from 'rxjs/operators';

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import { SearchNode, SearchService } from 'app/core/generated/circabc';
import { AnalyticsService } from 'app/core//analytics.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss'],
  preserveWhitespaces: true,
})
export class SearchBarComponent implements OnInit {
  @Input()
  groupId: string | undefined;
  @Input()
  forExplorer = false;
  @Output()
  readonly searchHit = new EventEmitter<string>();
  @Output()
  readonly renewSearchHit = new EventEmitter<string>();

  public fileResults: SearchNode[] = [];
  public folderResults: SearchNode[] = [];
  public postResults: SearchNode[] = [];
  public forumResults: SearchNode[] = [];
  public topicResults: SearchNode[] = [];
  public eventResults: SearchNode[] = [];
  public otherResults: SearchNode[] = [];
  public searching = false;
  public noResult = false;
  public searchForm!: FormGroup;

  public constructor(
    private fb: FormBuilder,
    private searchService: SearchService,
    private analyticsService: AnalyticsService
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

  private clearResults() {
    this.fileResults = [];
    this.folderResults = [];
    this.postResults = [];
    this.forumResults = [];
    this.topicResults = [];
    this.eventResults = [];
    this.otherResults = [];
  }

  public resetForm(): void {
    this.clearResults();
    this.searchForm.controls.searchString.setValue('');
    this.noResult = false;
    this.searching = false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async onValueChanged(data?: any) {
    this.clearResults();
    this.noResult = false;

    // done especially for a IG search
    if (data.searchString !== '' && !this.searching && this.groupId) {
      this.searching = true;
      try {
        const res = await firstValueFrom(
          this.searchService.getSearch(data.searchString, this.groupId)
        );
        if (res.data) {
          this.splitResults(res.data);

          if (res.total === 0) {
            this.noResult = true;
          }
          this.analyticsService.trackSiteSearch(
            data.searchString,
            `search interest group -${this.groupId}`,
            res.total
          );
        }
      } catch (error) {
        this.noResult = true;
      }

      this.searching = false;
    }

    if (this.forExplorer) {
      this.searchHit.emit(data.searchString);
    }
  }

  splitResults(data: SearchNode[]) {
    for (const sn of data) {
      switch (sn.resultType) {
        case 'folder':
          this.folderResults.push(sn);
          break;
        case 'file':
          this.fileResults.push(sn);
          break;
        case 'topic':
          this.topicResults.push(sn);
          break;
        case 'post':
          this.postResults.push(sn);
          break;
        case 'forum':
          this.forumResults.push(sn);
          break;
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

  hasOtherResults(): boolean {
    return this.otherResults.length > 0;
  }

  private getContext(node: SearchNode, item: 'forum' | 'forum/topic'): string {
    if (node.service === 'library') {
      return node.service;
    } else {
      return item;
    }
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
    } else {
      return '';
    }
  }

  public getModified(searchNode: SearchNode): string | null {
    if (searchNode.properties) {
      return searchNode.properties.modified;
    } else {
      return null;
    }
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
}
