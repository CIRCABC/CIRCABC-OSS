import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroupService,
  Node as ModelNode,
  RecentDiscussion,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RecentDiscussionsComponent } from './recent-discussions.component';

const mockDiscussions: RecentDiscussion[] = Array.from(
  { length: 10 },
  (_, i) => ({
    post: { id: `post-${i}`, name: `Post ${i}` },
    topic: { id: `topic-${i}`, name: `Topic ${i}` },
  })
);

describe('RecentDiscussionsComponent', () => {
  let component: RecentDiscussionsComponent;
  let componentRef: ComponentRef<RecentDiscussionsComponent>;
  let fixture: ComponentFixture<RecentDiscussionsComponent>;

  const mockInterestGroupService = {
    getGroupRecentDiscussionsAsync: vi.fn().mockResolvedValue(mockDiscussions),
  };

  const mockI18nPipe = {
    transform: vi.fn(
      (mltext: { [key: string]: string }) => Object.values(mltext)[0] ?? ''
    ),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecentDiscussionsComponent],
      providers: [
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecentDiscussionsComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('igId', 'group-123');
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load discussions on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(
      mockInterestGroupService.getGroupRecentDiscussionsAsync
    ).toHaveBeenCalledWith({ id: 'group-123' });
    expect(component.discussions()).toEqual(mockDiscussions);
    expect(component.loading()).toBe(false);
    expect(component.restCallError()).toBe(false);
  });

  it('should set restCallError on API failure', async () => {
    mockInterestGroupService.getGroupRecentDiscussionsAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.restCallError()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should not call API if igId is empty', async () => {
    componentRef.setInput('igId', '');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(
      mockInterestGroupService.getGroupRecentDiscussionsAsync
    ).not.toHaveBeenCalled();
  });

  it('getRecentDiscussions should return max 8 items when more is false', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.more = false;
    const result = component.getRecentDiscussions();
    expect(result).toHaveLength(8);
  });

  it('getRecentDiscussions should return all items when more is true', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.more = true;
    const result = component.getRecentDiscussions();
    expect(result).toHaveLength(10);
  });

  it('getTitleOrName should return transformed title when title exists', () => {
    const node: ModelNode = {
      title: { en: 'English Title' },
      name: 'fallback',
    };
    const result = component.getTitleOrName(node);
    expect(result).toBe('English Title');
    expect(mockI18nPipe.transform).toHaveBeenCalledWith({
      en: 'English Title',
    });
  });

  it('getTitleOrName should return name when title is empty', () => {
    const node: ModelNode = { title: {}, name: 'nodeName' };
    const result = component.getTitleOrName(node);
    expect(result).toBe('nodeName');
  });

  it('getTitleOrName should return name when title is undefined', () => {
    const node: ModelNode = { name: 'nodeName' };
    const result = component.getTitleOrName(node);
    expect(result).toBe('nodeName');
  });
});
