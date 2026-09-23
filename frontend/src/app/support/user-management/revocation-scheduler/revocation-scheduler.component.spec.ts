import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { HistoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { RevocationSchedulerComponent } from './revocation-scheduler.component';

vi.mock('app/core/util/date-calendar-util', () => ({
  setupCalendarDateHandling: vi.fn().mockReturnValue({ unsubscribe: vi.fn() }),
}));

const mockHistoryService = {
  revokeUserMemberships: vi.fn().mockReturnValue(of(undefined)),
  revokeUserMembershipsAsync: vi.fn().mockResolvedValue(undefined),
};

async function setup() {
  await TestBed.configureTestingModule({
    imports: [RevocationSchedulerComponent, ReactiveFormsModule],
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

  const fixture: ComponentFixture<RevocationSchedulerComponent> =
    TestBed.createComponent(RevocationSchedulerComponent);
  fixture.componentRef.setInput('userIds', ['user1', 'user2']);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance };
}

describe('RevocationSchedulerComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should create and initialize the form', async () => {
    const { component } = await setup();
    expect(component).toBeDefined();
    expect(component.scheduleForm).toBeDefined();
    expect(component.scheduleForm.controls['scheduleDate']).toBeDefined();
  });

  it('should call revokeUserMemberships and emit scheduled on success', async () => {
    const { component } = await setup();
    const scheduledSpy = vi.spyOn(component.scheduled, 'emit');

    await component.schedule();

    expect(mockHistoryService.revokeUserMembershipsAsync).toHaveBeenCalledWith({
      userRevocationRequest: {
        userIds: ['user1', 'user2'],
        revocationDate: component.scheduleForm.value.scheduleDate,
        action: 'revoke',
      },
    });
    expect(scheduledSpy).toHaveBeenCalled();
    expect(component.processing).toBe(false);
  });

  it('should handle error in schedule and not emit scheduled', async () => {
    mockHistoryService.revokeUserMembershipsAsync.mockRejectedValue(
      new Error('fail')
    );
    const { component } = await setup();
    const scheduledSpy = vi.spyOn(component.scheduled, 'emit');

    await component.schedule();

    expect(scheduledSpy).not.toHaveBeenCalled();
    expect(component.processing).toBe(false);
  });

  it('should emit canceled on cancel', async () => {
    const { component } = await setup();
    const canceledSpy = vi.spyOn(component.canceled, 'emit');

    component.cancel();

    expect(canceledSpy).toHaveBeenCalled();
  });
});
