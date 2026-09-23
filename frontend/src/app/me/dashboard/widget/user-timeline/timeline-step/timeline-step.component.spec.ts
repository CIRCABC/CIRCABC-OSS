import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { UserNewsFeed } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { TimelineStepComponent } from './timeline-step.component';

describe('TimelineStepComponent', () => {
  function createComponent(userFeed: UserNewsFeed, when: string) {
    TestBed.configureTestingModule({
      imports: [TimelineStepComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(TimelineStepComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(TimelineStepComponent);
    fixture.componentRef.setInput('userFeed', userFeed);
    fixture.componentRef.setInput('when', when);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent({ when: 'today' }, 'today');
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should return true when groupFeeds has items', () => {
    const feed: UserNewsFeed = { when: 'today', groupFeeds: [{} as never] };
    const fixture = createComponent(feed, 'today');
    expect(fixture.componentInstance.isEmptyFeed()).toBe(true);
  });

  it('should return false when groupFeeds is empty', () => {
    const feed: UserNewsFeed = { when: 'today', groupFeeds: [] };
    const fixture = createComponent(feed, 'today');
    expect(fixture.componentInstance.isEmptyFeed()).toBe(false);
  });

  it('should return false when groupFeeds is undefined', () => {
    const feed: UserNewsFeed = { when: 'week' };
    const fixture = createComponent(feed, 'week');
    expect(fixture.componentInstance.isEmptyFeed()).toBe(false);
  });
});
