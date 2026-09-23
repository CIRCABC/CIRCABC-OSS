import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MembershipApplicationComponent } from './membership-application.component';

describe('MembershipApplicationComponent', () => {
  let component: MembershipApplicationComponent;
  let fixture: ComponentFixture<MembershipApplicationComponent>;

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockMembersService = {
    postApplicant: vi.fn().mockReturnValue(of(undefined)),
    postApplicantAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [MembershipApplicationComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: MembersService, useValue: mockMembersService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(MembershipApplicationComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    fixture = TestBed.createComponent(MembershipApplicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with username and default values on init', () => {
    expect(component.applicationForm.value).toEqual({
      username: 'testuser',
      action: 'submitNew',
      message: '',
    });
  });

  it('should have message as required', () => {
    expect(component.applicationForm.controls['message'].valid).toBe(false);
    component.applicationForm.controls['message'].setValue('I want to join');
    expect(component.applicationForm.controls['message'].valid).toBe(true);
  });

  describe('submitApplication', () => {
    it('should do nothing if groupId is undefined', async () => {
      await component.submitApplication();
      expect(mockMembersService.postApplicantAsync).not.toHaveBeenCalled();
    });

    it('should call postApplicant and emit finished with SUCCEED on success', async () => {
      fixture.componentRef.setInput('groupId', 'group-123');
      component.applicationForm.controls['message'].setValue('Please add me');

      const finishedSpy = vi.fn();
      component.finished.subscribe(finishedSpy);

      await component.submitApplication();

      expect(mockMembersService.postApplicantAsync).toHaveBeenCalledWith({
        id: 'group-123',
        applicantAction: {
          username: 'testuser',
          action: 'submitNew',
          message: 'Please add me',
        },
      });
      expect(finishedSpy).toHaveBeenCalledWith({
        type: ActionType.APPLY_FOR_MEMBERSHIP,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should emit finished with FAILED on error', async () => {
      fixture.componentRef.setInput('groupId', 'group-123');
      mockMembersService.postApplicantAsync.mockRejectedValue(
        new Error('fail')
      );

      const finishedSpy = vi.fn();
      component.finished.subscribe(finishedSpy);

      await component.submitApplication();

      expect(finishedSpy).toHaveBeenCalledWith({
        type: ActionType.APPLY_FOR_MEMBERSHIP,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should reset form, set showModal to false, and emit canceled', () => {
      component.applicationForm.controls['message'].setValue('some text');
      component.showModal.set(true);

      const canceledSpy = vi.fn();
      component.canceled.subscribe(canceledSpy);

      component.cancel();

      expect(component.applicationForm.value).toEqual({
        username: 'testuser',
        action: 'submitNew',
        message: '',
      });
      expect(component.showModal()).toBe(false);
      expect(canceledSpy).toHaveBeenCalledWith({
        type: ActionType.APPLY_FOR_MEMBERSHIP,
        result: ActionResult.CANCELED,
      });
    });
  });
});
