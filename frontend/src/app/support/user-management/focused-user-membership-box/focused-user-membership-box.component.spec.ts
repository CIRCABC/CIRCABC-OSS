import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  Category,
  InterestGroupProfile,
  UserService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FocusedUserMembershipBoxComponent } from './focused-user-membership-box.component';

const mockMemberships: InterestGroupProfile[] = [
  { interestGroup: { id: '1' } as InterestGroupProfile['interestGroup'] },
];

const mockCategories: Category[] = [{ name: 'Test Category' }];

describe('FocusedUserMembershipBoxComponent', () => {
  let component: FocusedUserMembershipBoxComponent;
  let componentRef: ComponentRef<FocusedUserMembershipBoxComponent>;
  let fixture: ComponentFixture<FocusedUserMembershipBoxComponent>;

  const mockUserService = {
    getUserMembershipAsync: vi.fn().mockResolvedValue(mockMemberships),
    getUserCategoriesAsync: vi.fn().mockResolvedValue(mockCategories),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FocusedUserMembershipBoxComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
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

    mockUserService.getUserMembershipAsync.mockResolvedValue(mockMemberships);
    mockUserService.getUserCategoriesAsync.mockResolvedValue(mockCategories);

    fixture = TestBed.createComponent(FocusedUserMembershipBoxComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('userId', 'user1');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load memberships and categories on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.memberships()).toEqual(mockMemberships);
    expect(component.categories()).toEqual(mockCategories);
    expect(component.loading()).toBe(false);
    expect(component.error()).toBe(false);
  });

  it('should set error to true when service call fails', async () => {
    mockUserService.getUserMembershipAsync.mockRejectedValue(new Error('fail'));

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.error()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should reload data when userId input changes', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    mockUserService.getUserMembershipAsync.mockClear();
    mockUserService.getUserCategoriesAsync.mockClear();

    componentRef.setInput('userId', 'user2');
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUserService.getUserMembershipAsync).toHaveBeenCalledWith({
      userId: 'user2',
    });
    expect(mockUserService.getUserCategoriesAsync).toHaveBeenCalledWith({
      userId: 'user2',
    });
  });

  it('should not call services if userId is empty', async () => {
    componentRef.setInput('userId', '');
    mockUserService.getUserMembershipAsync.mockClear();
    mockUserService.getUserCategoriesAsync.mockClear();

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockUserService.getUserMembershipAsync).not.toHaveBeenCalled();
    expect(mockUserService.getUserCategoriesAsync).not.toHaveBeenCalled();
  });
});
