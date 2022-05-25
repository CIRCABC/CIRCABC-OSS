import { Component, Input } from '@angular/core';

import { UserNewsFeed } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-timeline-step',
  templateUrl: './timeline-step.component.html',
  styleUrls: ['./timeline-step.component.scss'],
  preserveWhitespaces: true,
})
export class TimelineStepComponent {
  @Input()
  userFeed!: UserNewsFeed;

  @Input()
  when!: string;

  isEmptyFeed(): boolean {
    if (this.userFeed && this.userFeed.groupFeeds) {
      return this.userFeed.groupFeeds.length > 0;
    }

    return false;
  }
}
