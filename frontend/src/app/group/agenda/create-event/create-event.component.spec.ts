import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  EventsService,
  InterestGroupService,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateEventComponent } from './create-event.component';

const mockUser = {
  userId: 'testuser',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john@example.com',
  phone: '123456',
};

const mockIg = {
  id: 'ig-1',
  name: 'Test IG',
  libraryId: 'lib-1',
  permissions: { library: 'LibManageOwn' },
};

const mockLoginService = {
  getUser: vi.fn().mockReturnValue(mockUser),
};

const mockInterestGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
};

const mockEventsService = {
  postEvent: vi.fn().mockReturnValue(of(undefined)),
  postEventAsync: vi.fn().mockResolvedValue(undefined),
};

const mockNodesService = {
  getPathAsync: vi.fn().mockResolvedValue([]),
};

describe('CreateEventComponent', () => {
  let component: CreateEventComponent;
  let componentRef: ComponentRef<CreateEventComponent>;
  let fixture: ComponentFixture<CreateEventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateEventComponent],
      providers: [
        provideNativeDateAdapter(),
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: EventsService, useValue: mockEventsService },
        { provide: NodesService, useValue: mockNodesService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateEventComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('defaultDate', new Date(Date.now() + 86400000));
    componentRef.setInput('igId', 'ig-1');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form after ngOnInit', async () => {
    await component.ngOnInit();
    expect(component.formReady()).toBe(true);
    expect(component.newEventForm).toBeDefined();
  });

  it('should set user from login service', async () => {
    await component.ngOnInit();
    expect(component.user).toEqual(mockUser);
  });

  it('should start at wizard step 1', () => {
    expect(component.wizardStep).toBe(1);
  });

  describe('stepIsValid', () => {
    beforeEach(async () => {
      await component.ngOnInit();
    });

    it('should return false at step 1 when title is empty', () => {
      component.wizardStep = 1;
      expect(component.stepIsValid()).toBe(false);
    });

    it('should return true at step 1 when title is set and dateInfo is valid', () => {
      component.wizardStep = 1;
      component.newEventForm.controls['title'].setValue('Test Event');
      expect(component.stepIsValid()).toBe(true);
    });

    it('should return true at step 2 when repeatsInfo is valid', () => {
      component.wizardStep = 2;
      expect(component.stepIsValid()).toBe(true);
    });

    it('should return true at step 3 when attendantsInfo is valid', () => {
      component.wizardStep = 3;
      expect(component.stepIsValid()).toBe(true);
    });
  });

  describe('navigation', () => {
    beforeEach(async () => {
      await component.ngOnInit();
    });

    it('should advance wizard step when step is valid', () => {
      component.newEventForm.controls['title'].setValue('Test Event');
      component.nextWizardStep();
      expect(component.wizardStep).toBe(2);
    });

    it('should not advance wizard step when step is invalid', () => {
      component.nextWizardStep();
      expect(component.wizardStep).toBe(1);
    });

    it('should go back one step', () => {
      component.wizardStep = 3;
      component.previousWizardStep();
      expect(component.wizardStep).toBe(2);
    });

    it('canGoBack should return false at step 1', () => {
      expect(component.canGoBack()).toBe(false);
    });

    it('canGoBack should return true at step 2', () => {
      component.wizardStep = 2;
      expect(component.canGoBack()).toBe(true);
    });
  });

  describe('toggles', () => {
    beforeEach(async () => {
      await component.ngOnInit();
    });

    it('should toggle repeatsSelected', () => {
      expect(component.repeatsSelected).toBe(false);
      component.toggleRepeats();
      expect(component.repeatsSelected).toBe(true);
    });

    it('should toggle appointmentTypeEvent', () => {
      componentRef.setInput('appointmentTypeEvent', true);
      component.toggleEventOrMeeting();
      expect(component.appointmentTypeEvent()).toBe(false);
    });

    it('should set appointmentTypeEvent via radio', () => {
      component.radioEventOrMeeting(false);
      expect(component.appointmentTypeEvent()).toBe(false);
    });
  });

  describe('hasLibraryAccess', () => {
    it('should return true when library permission is not LibNoAccess', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.hasLibraryAccess()).toBe(true);
    });
  });

  describe('closeWizard', () => {
    it('should set showModal to false', async () => {
      componentRef.setInput('showModal', true);
      await component.ngOnInit();
      component.closeWizard('close');
      expect(component.showModal()).toBe(false);
    });
  });

  describe('otherSelected', () => {
    beforeEach(async () => {
      await component.ngOnInit();
    });

    it('should return false when meetingType is FaceToFace', () => {
      component.newEventForm.controls['meetingType'].setValue('FaceToFace');
      expect(component.otherSelected()).toBe(false);
    });

    it('should return true when meetingType is something else', () => {
      component.newEventForm.controls['meetingType'].setValue('Other');
      expect(component.otherSelected()).toBe(true);
    });
  });

  describe('save', () => {
    beforeEach(async () => {
      await component.ngOnInit();
      component.newEventForm.controls['title'].setValue('Test Event');
    });

    it('should call postEvent and emit eventMeetingSaved', async () => {
      const emitSpy = vi.spyOn(component.eventMeetingSaved, 'emit');
      await component.save();
      expect(mockEventsService.postEventAsync).toHaveBeenCalledWith({
        id: 'ig-1',
        eventDefinition: expect.objectContaining({ title: 'Test Event' }),
      });
      expect(emitSpy).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });
  });
});
