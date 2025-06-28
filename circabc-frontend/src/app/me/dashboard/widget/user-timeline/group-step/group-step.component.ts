import { Component, OnChanges, SimpleChanges, input } from '@angular/core';

import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  type InterestGroupFeed,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

@Component({
  selector: 'cbc-group-step',
  templateUrl: './group-step.component.html',
  styleUrl: './group-step.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, UserCardComponent, DatePipe, TranslocoModule],
})
export class GroupStepComponent implements OnChanges {
  readonly groupFeed = input.required<InterestGroupFeed>();
  readonly type = input.required<string>();

  public showAll = false;
  public hasMoreThan15 = false;

  constructor(private i18nPipe: I18nPipe) {}

  public ngOnChanges(changes: SimpleChanges) {
    if (changes.groupFeed?.currentValue.feed) {
      this.hasMoreThan15 = changes.groupFeed.currentValue.feed.length > 15;
    }
  }

  getGroupNameOrTitle(): string {
    const groupFeed = this.groupFeed();
    if (groupFeed.title && Object.keys(groupFeed.title).length > 0) {
      return this.i18nPipe.transform(groupFeed.title);
    }
    return groupFeed.name ? groupFeed.name : '';
  }

  public getDisplayProperty(node: ModelNode | undefined) {
    if (node === undefined) {
      return '';
    }
    // check if the title is present, and if not, return the name
    let title = this.i18nPipe.transform(node.title);
    if (node?.title && Object.keys(node.title).length > 0 && node.properties) {
      if (
        node.properties.kindOfEvent !== undefined ||
        node.properties.meetingType !== undefined ||
        node.properties.eventType !== undefined
      ) {
        // case for events and meetings as the title is of type MLText, but not used as such
        const match = title.match(/=(.*?)}/); // regex to capture all character between = and } (ie. {en=text} => text)

        title = match === null ? title : match[1];
      }
      return title === '' ? node.name : title;
    }
    if (node.name?.startsWith('posted') && node.name.endsWith('.html')) {
      // case for the posts
      const postedLength = 'posted-'.length;
      const dateLength = 'dd-MM-yyyy'.length;
      const hourLength = 'HH:mm'.length;
      const firstPart = node.name.substring(
        postedLength,
        postedLength + dateLength
      );
      const secondPart = node.name
        .substring(
          postedLength + dateLength + 1,
          postedLength + dateLength + 1 + hourLength
        )
        .replace('-', ':');
      // eslint-disable-next-line
      const name = `Post ${firstPart} ${secondPart}`;
      return title === '' ? name : title;
    }
    // just return the name (default)
    return title === '' ? node.name : title;
  }

  public getDisplayableFeed() {
    const groupFeed = this.groupFeed();
    if (this.showAll && groupFeed.feed) {
      return groupFeed.feed;
    }
    if (!this.showAll && groupFeed.feed) {
      return groupFeed.feed.slice(0, 15);
    }
    return undefined;
  }

  public showAllFeed() {
    this.showAll = true;
  }
}
