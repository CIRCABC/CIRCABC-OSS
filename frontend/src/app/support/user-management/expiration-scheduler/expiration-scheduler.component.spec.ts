import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { HistoryService } from 'app/core/generated/circabc';
import { InterestGroupProfileSelectable } from 'app/support/user-management/interest-group-profile-selectable';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ExpirationSchedulerComponent } from './expiration-scheduler.component';

const mockHistoryService = {
  setMembershipsExpiration: vi.fn().mockReturnValue(of(undefined)),
  setMembershipsExpirationAsync: vi.fn().mockResolvedValue(undefined),
};

function makeMembership(selected: boolean): InterestGroupProfileSelectable {
  return {
    selected,
    profile: { id: 'p1', name: 'ACCESS' },
    interestGroup: { name: 'ig1', permissions: {} },
  };
}

describe('ExpirationSchedulerComponent', () => {
  let component: ExpirationSchedulerComponent;
  let componentRef: ComponentRef<ExpirationSchedulerComponent>;
  let fixture: ComponentFixture<ExpirationSchedulerComponent>;

  beforeEach(async () => {
    mockHistoryService.setMembershipsExpiration.mockReturnValue(of(undefined));
    mockHistoryService.setMembershipsExpirationAsync.mockResolvedValue(
      undefined
    );

    await TestBed.configureTestingModule({
      imports: [ExpirationSchedulerComponent],
      providers: [
        provideNativeDateAdapter(),
        { provide: HistoryService, useValue: mockHistoryService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ExpirationSchedulerComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize scheduleForm on init', () => {
    expect(component.scheduleForm).toBeDefined();
    expect(component.scheduleForm.controls['scheduleDate']).toBeDefined();
  });

  it('should emit canceled on cancel()', () => {
    const cancelSpy = vi.fn();
    componentRef.instance.canceled.subscribe(cancelSpy);

    component.cancel();

    expect(cancelSpy).toHaveBeenCalled();
  });

  it('should call historyService and emit scheduled on schedule()', async () => {
    const scheduledSpy = vi.fn();
    componentRef.instance.scheduled.subscribe(scheduledSpy);

    const selectedMembership = makeMembership(true);
    const requests: UsersMembershipsModel[] = [
      {
        userid: 'user1',
        user: { userId: 'user1' },
        memberships: [selectedMembership, makeMembership(false)],
        loadingMemberships: false,
      },
    ];

    componentRef.setInput('requests', requests);
    fixture.detectChanges();

    await component.schedule();

    expect(
      mockHistoryService.setMembershipsExpirationAsync
    ).toHaveBeenCalledWith({
      userMembershipsExpirationRequest: [
        {
          userId: 'user1',
          expirationDate: component.scheduleForm.value.scheduleDate,
          memberships: [selectedMembership],
        },
      ],
    });
    expect(scheduledSpy).toHaveBeenCalled();
    expect(component.processing).toBe(false);
  });

  it('should not include users with no selected memberships', async () => {
    const requests: UsersMembershipsModel[] = [
      {
        userid: 'user1',
        user: { userId: 'user1' },
        memberships: [makeMembership(false)],
        loadingMemberships: false,
      },
    ];

    componentRef.setInput('requests', requests);
    fixture.detectChanges();

    await component.schedule();

    expect(
      mockHistoryService.setMembershipsExpirationAsync
    ).toHaveBeenCalledWith({ userMembershipsExpirationRequest: [] });
  });

  it('should handle error in schedule() gracefully', async () => {
    mockHistoryService.setMembershipsExpirationAsync.mockRejectedValue(
      new Error('fail')
    );
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const requests: UsersMembershipsModel[] = [
      {
        userid: 'user1',
        user: { userId: 'user1' },
        memberships: [makeMembership(true)],
        loadingMemberships: false,
      },
    ];

    componentRef.setInput('requests', requests);
    fixture.detectChanges();

    await component.schedule();

    expect(consoleSpy).toHaveBeenCalled();
    expect(component.processing).toBe(false);
    consoleSpy.mockRestore();
  });
});
