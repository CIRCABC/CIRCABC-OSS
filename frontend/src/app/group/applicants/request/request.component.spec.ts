import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { Applicant, MembersService, Profile } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RequestComponent } from './request.component';

const mockApplicant: Applicant = {
  submitted: '2024-01-15T10:00:00Z',
  user: {
    userId: 'user1',
    firstname: 'John',
    lastname: 'Doe',
    email: 'john.doe@ec.europa.eu',
  },
  justification: 'I need access',
};

const mockProfiles: Profile[] = [
  { id: 'prof1', name: 'Access', title: { en: 'Access' } },
  { id: 'prof2', name: 'Admin', title: { en: 'Admin' } },
];

describe('RequestComponent', () => {
  let component: RequestComponent;
  let componentRef: ComponentRef<RequestComponent>;
  let fixture: ComponentFixture<RequestComponent>;

  const mockMembersService = {
    postMember: vi.fn().mockReturnValue(of(undefined)),
    putApplicant: vi.fn().mockReturnValue(of(undefined)),
    postMemberAsync: vi.fn().mockResolvedValue(undefined),
    putApplicantAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [RequestComponent],
      providers: [
        { provide: MembersService, useValue: mockMembersService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('applicant', mockApplicant);
    componentRef.setInput('groupId', 'group123');
    componentRef.setInput('availableProfiles', mockProfiles);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize forms on init', () => {
    expect(component.inviteForm).toBeDefined();
    expect(component.declineForm).toBeDefined();
  });

  it('should return user firstname, lastname, and email', () => {
    expect(component.firstname).toBe('John');
    expect(component.lastname).toBe('Doe');
    expect(component.email).toBe('john.doe@ec.europa.eu');
  });

  it('should return empty strings when user is undefined', () => {
    componentRef.setInput('applicant', {});
    fixture.detectChanges();
    expect(component.firstname).toBe('');
    expect(component.lastname).toBe('');
    expect(component.email).toBe('');
  });

  describe('prepareAccept', () => {
    it('should show accept form and set first profile as default', () => {
      component.prepareAccept();
      expect(component.showAcceptForm).toBe(true);
      expect(component.inviteForm.controls['selectedProfile'].value).toBe(
        'prof1'
      );
    });
  });

  describe('prepareDecline', () => {
    it('should show decline form', () => {
      component.prepareDecline();
      expect(component.showDeclineForm).toBe(true);
    });
  });

  describe('cancel', () => {
    it('should hide both forms', () => {
      component.showAcceptForm = true;
      component.showDeclineForm = true;
      component.cancel();
      expect(component.showAcceptForm).toBe(false);
      expect(component.showDeclineForm).toBe(false);
    });
  });

  describe('invite', () => {
    beforeEach(() => {
      component.prepareAccept();
      component.inviteForm.controls['selectedProfile'].setValue('prof1');
    });

    it('should call postMember and putApplicant on success', async () => {
      await component.invite();
      expect(mockMembersService.postMemberAsync).toHaveBeenCalledWith({
        id: 'group123',
        membershipPostDefinition: expect.objectContaining({
          memberships: expect.any(Array),
        }),
      });
      expect(mockMembersService.putApplicantAsync).toHaveBeenCalledWith({
        id: 'group123',
        applicantAction: expect.objectContaining({
          action: 'clean',
          username: 'user1',
        }),
        action: 'clean',
      });
      expect(component.processing()).toBe(false);
    });

    it('should emit requestProcessed on success', async () => {
      const emitSpy = vi.spyOn(component.requestProcessed, 'emit');
      await component.invite();
      expect(emitSpy).toHaveBeenCalledWith(mockApplicant);
    });

    it('should show error message on failure', async () => {
      mockMembersService.postMemberAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      await component.invite();
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });
  });

  describe('decline', () => {
    it('should call putApplicant with decline action', async () => {
      component.declineForm.controls['declineText'].setValue('Not eligible');
      await component.decline();
      expect(mockMembersService.putApplicantAsync).toHaveBeenCalledWith({
        id: 'group123',
        applicantAction: expect.objectContaining({
          action: 'decline',
          username: 'user1',
          message: 'Not eligible',
        }),
        action: 'decline',
      });
    });

    it('should emit requestProcessed after decline', async () => {
      const emitSpy = vi.spyOn(component.requestProcessed, 'emit');
      await component.decline();
      expect(emitSpy).toHaveBeenCalledWith(mockApplicant);
      expect(component.processing()).toBe(false);
    });
  });

  describe('selectedProfileControl', () => {
    it('should return the selectedProfile form control', () => {
      expect(component.selectedProfileControl).toBe(
        component.inviteForm.controls['selectedProfile']
      );
    });
  });
});
