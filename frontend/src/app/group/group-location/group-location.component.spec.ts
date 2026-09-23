import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  GroupPath,
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupLocationComponent } from './group-location.component';

const mockGroup: InterestGroup = {
  id: 'group-123',
  name: 'Test Group',
  permissions: { library: 'LibAdmin' },
};

const mockGroupPath: GroupPath = {
  group: mockGroup,
  category: { id: 'cat-1', name: 'Category 1' } as never,
  header: { id: 'hdr-1', name: 'Header 1' } as never,
};

describe('GroupLocationComponent', () => {
  let fixture: ComponentFixture<GroupLocationComponent>;
  let component: GroupLocationComponent;
  let componentRef: ComponentRef<GroupLocationComponent>;
  const mockGroupService = {
    getGroupPathAsync: vi.fn().mockResolvedValue(mockGroupPath),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupLocationComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: InterestGroupService, useValue: mockGroupService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GroupLocationComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('group', mockGroup);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should fetch group path on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getGroupPathAsync).toHaveBeenCalledWith({
      id: 'group-123',
    });
    expect(component.groupPath()).toEqual(mockGroupPath);
  });

  it('should not fetch group path when group has no id', async () => {
    mockGroupService.getGroupPathAsync.mockClear();
    componentRef.setInput('group', { name: 'No ID', permissions: {} });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getGroupPathAsync).not.toHaveBeenCalled();
  });
});
