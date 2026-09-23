import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { DashboardService, UserNewsFeed } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserTimelineComponent } from './user-timeline.component';

const mockFeed: UserNewsFeed = {
  when: 'today',
  uploads: 3,
  updates: 5,
  comments: 2,
  groupFeeds: [],
};

describe('UserTimelineComponent', () => {
  let fixture: ComponentFixture<UserTimelineComponent>;
  let component: UserTimelineComponent;
  let componentRef: ComponentRef<UserTimelineComponent>;

  const mockDashboardService = {
    getUserNewsFeedAsync: vi.fn().mockResolvedValue(mockFeed),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserTimelineComponent],
      providers: [
        { provide: DashboardService, useValue: mockDashboardService },
        { provide: LoginService, useValue: mockLoginService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserTimelineComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('when', 'today');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load user feed on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockLoginService.getCurrentUsername).toHaveBeenCalled();
    expect(mockDashboardService.getUserNewsFeedAsync).toHaveBeenCalledWith({
      userId: 'testuser',
      when: 'today',
    });
    expect(component.userFeed()).toEqual(mockFeed);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe(false);
  });

  it('should set error to true when feed loading fails', async () => {
    mockDashboardService.getUserNewsFeedAsync.mockRejectedValueOnce(
      new Error('fail')
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });
});
