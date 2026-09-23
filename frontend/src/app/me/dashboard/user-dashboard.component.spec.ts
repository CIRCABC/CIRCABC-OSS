import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { InterestGroupProfile, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserDashboardComponent } from './user-dashboard.component';

@Pipe({ name: 'cbcSetTitle' })
class MockSetTitlePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

const mockMemberships: InterestGroupProfile[] = [
  {
    profile: { name: 'ADMIN' },
    interestGroup: { name: 'Group1', permissions: {} },
  },
];

const mockLoginService = {
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockUserService = {
  getUserMembershipAsync: vi.fn().mockResolvedValue(mockMemberships),
};

describe('UserDashboardComponent', () => {
  let component: UserDashboardComponent;
  let fixture: ComponentFixture<UserDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserDashboardComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(UserDashboardComponent, {
        set: {
          imports: [TranslocoModule, MockSetTitlePipe],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    mockLoginService.getCurrentUsername.mockReturnValue('testuser');
    mockUserService.getUserMembershipAsync.mockResolvedValue(mockMemberships);

    fixture = TestBed.createComponent(UserDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load memberships on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockLoginService.getCurrentUsername).toHaveBeenCalled();
    expect(mockUserService.getUserMembershipAsync).toHaveBeenCalledWith({
      userId: 'testuser',
    });
    expect(component.memberships()).toEqual(mockMemberships);
    expect(component.loadingMemberships()).toBe(false);
  });

  it('should handle empty memberships', async () => {
    mockUserService.getUserMembershipAsync.mockResolvedValue([]);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.memberships()).toEqual([]);
    expect(component.loadingMemberships()).toBe(false);
  });
});
