import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import {
  InterestGroupFeed,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-group-step',
  templateUrl: './group-step.component.html',
  styleUrls: ['./group-step.component.scss'],
  preserveWhitespaces: true,
})
export class GroupStepComponent implements OnChanges {
  @Input()
  groupFeed!: InterestGroupFeed;
  @Input()
  type!: string;

  public showAll = false;
  public hasMoreThan15 = false;

  constructor(private i18nPipe: I18nPipe) {}

  public ngOnChanges(changes: SimpleChanges) {
    if (changes.groupFeed && changes.groupFeed.currentValue.feed) {
      this.hasMoreThan15 = changes.groupFeed.currentValue.feed.length > 15;
    }
  }

  getGroupNameOrTitle(): string {
    if (this.groupFeed.title && Object.keys(this.groupFeed.title).length > 0) {
      return this.i18nPipe.transform(this.groupFeed.title);
    } else {
      return this.groupFeed.name ? this.groupFeed.name : '';
    }
  }

  public getDisplayProperty(node: ModelNode | undefined) {
    if (node === undefined) {
      return '';
    }
    // check if the title is present, and if not, return the name
    let title = this.i18nPipe.transform(node.title);
    if (
      node &&
      node.title &&
      Object.keys(node.title).length > 0 &&
      node.properties
    ) {
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
    } else if (
      node.name !== undefined &&
      node.name.startsWith('posted') &&
      node.name.endsWith('.html')
    ) {
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
    } else {
      // just return the name (default)
      return title === '' ? node.name : title;
    }
  }

  public getDisplayableFeed() {
    if (this.showAll && this.groupFeed.feed) {
      return this.groupFeed.feed;
    } else if (!this.showAll && this.groupFeed.feed) {
      return this.groupFeed.feed.slice(0, 15);
    } else {
      return undefined;
    }
  }

  public showAllFeed() {
    this.showAll = true;
  }
}
