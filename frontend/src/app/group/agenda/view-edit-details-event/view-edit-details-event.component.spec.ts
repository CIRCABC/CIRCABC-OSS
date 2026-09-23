import { Location } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventDefinition,
  EventsService,
  InterestGroupService,
  NodesService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { ViewEditDetailsEventComponent } from './view-edit-details-event.component';

const mockEventDefinition: EventDefinition = {
  appointmentTypeEvent: true,
  title: 'Test Event',
  dateInfo: { date: '2026-05-01', startTime: '09:00', endTime: '10:00' },
  language: 'en',
  timezone: 'Europe/Brussels',
  repeatsInfo: { mainOccurence: 'OnlyOnce' },
  attendantsInfo: {
    audienceStatusOpen: false,
    invitedUsersOrProfiles: [],
    invitedExternalEmails: [],
    audience: [],
  },
  contactName: 'John Doe',
  contactPhone: '+32123456',
  contactEmail: 'john@ec.europa.eu',
  meetingPublicAvailability: false,
  meetingType: 'FaceToFace',
  eventType: 'Appointment',
  eventPriority: 'Medium',
  igId: 'ig-123',
};

describe('ViewEditDetailsEventComponent', () => {
  let component: ViewEditDetailsEventComponent;
  let fixture: ComponentFixture<ViewEditDetailsEventComponent>;

  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockEventsService = {
    getEventAsync: vi.fn().mockResolvedValue(mockEventDefinition),
    putEventAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({
      userId: 'user1',
      firstname: 'Test',
      lastname: 'User',
      email: 'test@ec.europa.eu',
    }),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({ id: 'node-1', permissions: {} }),
    getPathAsync: vi.fn().mockResolvedValue([]),
  };

  const mockPermEvalService = {
    isEveAdmin: vi.fn().mockReturnValue(true),
  };

  const mockUserService = {
    postUserEventAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue({
      name: 'Test Group',
      permissions: { library: 'LibManageOwn' },
      libraryId: 'lib-1',
      eventId: 'evt-root-1',
    }),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockLocation = {
    back: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewEditDetailsEventComponent],
      providers: [
        provideNativeDateAdapter(),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: EventsService, useValue: mockEventsService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UserService, useValue: mockUserService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Location, useValue: mockLocation },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewEditDetailsEventComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('defaultDate', new Date('2026-05-01'));
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('after initialization', () => {
    beforeEach(async () => {
      fixture.detectChanges();
      paramsSubject.next({ eventId: 'evt-1' });
      await vi.waitFor(() => {
        expect(component.formReady()).toBe(true);
      });
    });

    it('should load event data and build the form', () => {
      expect(mockEventsService.getEventAsync).toHaveBeenCalledWith({
        id: 'evt-1',
      });
      expect(component.updateEventForm).toBeDefined();
      expect(component.updateEventForm.get('title')?.value).toBe('Test Event');
    });

    it('should set eventId from route params', () => {
      expect(component.eventId).toBe('evt-1');
    });

    it('should identify as event when appointmentTypeEvent is true', () => {
      expect(component.isEvent()).toBe(true);
      expect(component.isMeeting()).toBe(false);
    });

    describe('tab navigation', () => {
      it('should default to GeneralInformation tab', () => {
        expect(component.isGeneralTab()).toBe(true);
      });

      it('should switch tabs via setTab', () => {
        component.setTab('Audience');
        expect(component.isAudienceTab()).toBe(true);
        expect(component.isGeneralTab()).toBe(false);
      });

      it('should identify RelevantSpace tab', () => {
        component.setTab('RelevantSpace');
        expect(component.isRelevantSpaceTab()).toBe(true);
      });

      it('should identify ContactInformation tab', () => {
        component.setTab('ContactInformation');
        expect(component.isContactTab()).toBe(true);
      });
    });

    describe('goBack', () => {
      it('should call location.back()', () => {
        component.goBack();
        expect(mockLocation.back).toHaveBeenCalled();
      });
    });

    describe('isEveAdmin', () => {
      it('should delegate to permEvalService', () => {
        expect(component.isEveAdmin()).toBe(true);
        expect(mockPermEvalService.isEveAdmin).toHaveBeenCalled();
      });
    });

    describe('toggleRepeats', () => {
      it('should toggle repeatsSelected', () => {
        expect(component.repeatsSelected).toBe(false);
        component.toggleRepeats();
        expect(component.repeatsSelected).toBe(true);
        component.toggleRepeats();
        expect(component.repeatsSelected).toBe(false);
      });
    });

    describe('otherSelected', () => {
      it('should return false for FaceToFace', () => {
        component.updateEventForm.controls['meetingType'].setValue(
          'FaceToFace'
        );
        expect(component.otherSelected()).toBe(false);
      });

      it('should return false for VirtualMeeting', () => {
        component.updateEventForm.controls['meetingType'].setValue(
          'VirtualMeeting'
        );
        expect(component.otherSelected()).toBe(false);
      });

      it('should return true for other values', () => {
        component.updateEventForm.controls['meetingType'].setValue('Other');
        expect(component.otherSelected()).toBe(true);
      });
    });

    describe('userInAudienceAndStatus', () => {
      it('should return false when appointmentTypeEvent is true', () => {
        expect(component.userInAudienceAndStatus('Accepted')).toBe(false);
      });
    });

    describe('cancel', () => {
      it('should call goBack', async () => {
        await component.cancel();
        expect(mockLocation.back).toHaveBeenCalled();
      });
    });
  });
});
