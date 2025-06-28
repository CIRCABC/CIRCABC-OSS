import { Component, OnInit, input } from '@angular/core';

import {
  InterestGroupService,
  Node as ModelNode,
  RecentDiscussion,
} from 'app/core/generated/circabc';

import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-recent-discussions',
  templateUrl: './recent-discussions.component.html',
  styleUrl: './recent-discussions.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    UserCardComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RecentDiscussionsComponent implements OnInit {
  readonly igId = input.required<string>();

  public discussions: RecentDiscussion[] = [];
  public loading = false;
  public more = false;
  public restCallError = false;

  constructor(
    private interestGroupService: InterestGroupService,
    private i18nPipe: I18nPipe
  ) {}

  async ngOnInit() {
    this.restCallError = false;
    const igId = this.igId();
    if (igId && igId !== '') {
      this.loading = true;
      try {
        this.discussions = await firstValueFrom(
          this.interestGroupService.getGroupRecentDiscussions(igId)
        );
      } catch (_error) {
        console.error('Problem retrieving the latest discussions');
        this.restCallError = true;
      }

      this.loading = false;
    }
  }

  public getTitleOrName(node: ModelNode): string | undefined {
    if (node.title && Object.keys(node.title).length > 0) {
      return this.i18nPipe.transform(node.title);
    }
    return node.name;
  }

  getRecentDiscussions(): RecentDiscussion[] {
    let result: RecentDiscussion[] = [];

    if (this.more) {
      result = this.discussions;
    } else {
      for (let i = 0; i < this.discussions.length && i < 8; i += 1) {
        result.push(this.discussions[i]);
      }
    }

    return result;
  }
}
