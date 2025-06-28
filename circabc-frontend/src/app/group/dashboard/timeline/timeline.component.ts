import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  input,
} from '@angular/core';

import {
  EntryEvent,
  GroupDashboardEntry,
  type InterestGroup,
  Node as ModelNode,
} from 'app/core/generated/circabc';

import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SimpleNewsCardComponent } from './simple-news-card/simple-news-card.component';

@Component({
  selector: 'cbc-timeline',
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, SimpleNewsCardComponent, DatePipe, TranslocoModule],
})
export class TimelineComponent implements OnChanges {
  readonly entries = input<GroupDashboardEntry[]>();
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  group!: InterestGroup;

  public libraryEntries: EntryEvent[] = [];
  public informationEntries: EntryEvent[] = [];

  public showNewsPanel = false;
  public showLibraryPanel = true;
  public hasMoreThan15 = false;
  public showAll = false;

  constructor(private i18nPipe: I18nPipe) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.entries) {
      if (changes.entries.currentValue !== undefined) {
        this.getValidNews();
      }
    }
  }

  public showNews() {
    this.showNewsPanel = true;
    this.showLibraryPanel = false;
  }

  public showLibrary() {
    this.showNewsPanel = false;
    this.showLibraryPanel = true;
  }

  public getDisplayProperty(node: ModelNode | undefined) {
    // check if the title is present, and if not, return the name

    if (node === undefined) {
      return;
    }
    let title = this.i18nPipe.transform(node.title);
    if (node.title && Object.keys(node.title).length > 0 && node.properties) {
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

  public getDestination(node: ModelNode) {
    if (node.properties === undefined) {
      return 'UNDEFINED';
    }

    if (
      node.properties.kindOfEvent !== undefined ||
      node.properties.meetingType !== undefined ||
      node.properties.eventType !== undefined
    ) {
      // for events/meetings
      return 'agenda';
    }
    if (node.name?.startsWith('posted') && node.name.endsWith('.html')) {
      // for posts
      return 'forum/topic';
    }
    // for library
    return 'library';
  }

  public isFile(type: string) {
    return type.indexOf('folder') === -1 && type.indexOf('content') !== -1;
  }

  public isTopic(type: string) {
    return type.indexOf('topic') !== -1;
  }

  public isForum(type: string) {
    return (
      type.indexOf('forum') !== -1 && !this.isPost(type) && !this.isTopic(type)
    );
  }

  public isPost(type: string) {
    return type.indexOf('post') !== -1;
  }

  public isNews(type: string) {
    return type.indexOf('news') !== -1;
  }

  public isFolder(type: string | undefined) {
    return type?.indexOf('folder') !== -1;
  }

  private getValidNews() {
    const entries = this.entries();
    if (entries && entries.length > 0) {
      const news = entries[0].news;
      if (news) {
        for (const entry of news) {
          // necessary to remove comments on documents
          if (
            entry.node?.type &&
            !(
              entry.node.service === 'library' &&
              (this.isPost(entry.node.type) || this.isTopic(entry.node.type))
            )
          ) {
            if (
              entry.node.service === 'library' &&
              entry.node.name !== 'Library'
            ) {
              this.libraryEntries.push(entry);
            } else if (
              entry.node.service === 'information' &&
              entry.node.name !== 'Information' &&
              this.isNews(entry.node.type) &&
              this.informationEntries.length < 3
            ) {
              this.informationEntries.push(entry);
            }
          }
        }
      }

      this.hasMoreThan15 = this.libraryEntries.length > 15;

      if (
        this.libraryEntries.length === 0 &&
        this.informationEntries.length > 0
      ) {
        this.showNewsPanel = true;
      }
    }
  }

  // eslint-disable-next-line complexity
  getRouterLinkParts(groupId: string, node: ModelNode | undefined): string[] {
    if (groupId && node && node.type && node.id && node.parentId) {
      if (node.service === 'library' && this.isFile(node.type)) {
        return ['/group', groupId, 'library', node.id, 'details'];
      }

      if (node.service === 'library' && this.isFolder(node.type)) {
        return ['/group', groupId, 'library', node.id];
      }

      if (node.service === 'library' && this.isForum(node.type)) {
        return ['/group', groupId, 'library', node.parentId, 'details'];
      }

      if (node.service === 'information') {
        return ['/group', groupId, 'information'];
      }

      if (node.service === 'newsgroups' && this.isForum(node.type)) {
        return ['/group', groupId, 'forum', node.id];
      }

      if (node.service === 'newsgroups' && this.isTopic(node.type)) {
        return ['/group', groupId, 'forum', 'topic', node.id];
      }

      if (node.service === 'newsgroups' && this.isPost(node.type)) {
        return ['/group', groupId, 'forum', 'topic', node.parentId];
      }
    }
    return [];
  }

  public getLibraryEntries() {
    if (this.showAll) {
      return this.libraryEntries;
    }
    return this.libraryEntries.slice(0, 15);
  }

  public showAllFeed() {
    this.showAll = true;
  }
}
