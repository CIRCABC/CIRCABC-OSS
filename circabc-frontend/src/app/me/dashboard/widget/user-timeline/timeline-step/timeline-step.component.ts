import { Component, Input, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { UserNewsFeed } from 'app/core/generated/circabc';
import { GroupStepComponent } from 'app/me/dashboard/widget/user-timeline/group-step/group-step.component';
import { SummaryBoxComponent } from 'app/me/dashboard/widget/user-timeline/summary-box/summary-box.component';

@Component({
  selector: 'cbc-timeline-step',
  templateUrl: './timeline-step.component.html',
  styleUrl: './timeline-step.component.scss',
  preserveWhitespaces: true,
  imports: [SummaryBoxComponent, GroupStepComponent, TranslocoModule],
})
export class TimelineStepComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  userFeed!: UserNewsFeed;

  readonly when = input.required<string>();

  isEmptyFeed(): boolean {
    if (this.userFeed?.groupFeeds) {
      return this.userFeed.groupFeeds.length > 0;
    }

    return false;
  }
}
