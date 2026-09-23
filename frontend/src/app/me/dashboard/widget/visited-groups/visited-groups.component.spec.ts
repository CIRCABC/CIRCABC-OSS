import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { VisitedGroupsComponent } from './visited-groups.component';

const mockGroups: InterestGroup[] = [
  { name: 'Group A', title: { en: 'English Title' }, permissions: {} },
  { name: 'Group B', title: {}, permissions: {} },
];

const mockInterestGroupService = {
  getVisitedInterestGroupsAsync: vi.fn().mockResolvedValue(mockGroups),
};

const mockI18nPipe = {
  transform: vi.fn((title: { [key: string]: string }) => title['en'] ?? ''),
};

describe('VisitedGroupsComponent', () => {
  let component: VisitedGroupsComponent;
  let fixture: ComponentFixture<VisitedGroupsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisitedGroupsComponent],
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

    fixture = TestBed.createComponent(VisitedGroupsComponent);
    component = fixture.componentInstance;
  });

  it('should load visited interest groups on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(
      mockInterestGroupService.getVisitedInterestGroupsAsync
    ).toHaveBeenCalledWith({ amount: 10 });
    expect(component.visitedIgs()).toEqual(mockGroups);
  });

  it('should return translated title when title exists', () => {
    const result = component.getGroupNameOrTitle(mockGroups[0]);
    expect(result).toBe('English Title');
  });

  it('should return name when title is empty', () => {
    const result = component.getGroupNameOrTitle(mockGroups[1]);
    expect(result).toBe('Group B');
  });

  it('should return empty string when no title and no name', () => {
    const group: InterestGroup = { name: '', title: {}, permissions: {} };
    const result = component.getGroupNameOrTitle(group);
    expect(result).toBe('');
  });
});
