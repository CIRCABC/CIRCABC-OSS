import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EmailService,
  InterestGroup,
  InterestGroupService,
  MailTemplateDefinition,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ContactComponent } from './contact.component';

const mockIg: InterestGroup = {
  id: 'ig-123',
  name: 'Test Group',
  permissions: { library: 'LibAdmin' },
};

const mockTemplates: MailTemplateDefinition[] = [
  { id: '1', name: 'Template 1', subject: 'Subject 1', text: '<p>Body 1</p>' },
];

describe('ContactComponent', () => {
  let component: ContactComponent;
  let fixture: ComponentFixture<ContactComponent>;
  let paramsSubject: Subject<{ id: string }>;

  const mockEmailService = {
    getUserMailTemplatesAsync: vi.fn().mockResolvedValue(mockTemplates),
    postGroupEmail: vi.fn().mockReturnValue(of(undefined)),
    postGroupEmailAsync: vi.fn().mockResolvedValue(undefined),
    saveUserMailTemplate: vi.fn().mockReturnValue(of(undefined)),
    saveUserMailTemplateAsync: vi.fn().mockResolvedValue(undefined),
    deleteUserMailTemplates: vi.fn().mockReturnValue(of(undefined)),
    deleteUserMailTemplatesAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockInterestGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockIg),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  const mockPermEvalService = {
    isDirAdmin: vi.fn().mockReturnValue(false),
    isDirManageMembers: vi.fn().mockReturnValue(false),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [ContactComponent],
      providers: [
        { provide: EmailService, useValue: mockEmailService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize forms on ngOnInit', () => {
    expect(component.emailForm).toBeDefined();
    expect(component.membersForm).toBeDefined();
  });

  it('should load group and templates when route params emit', async () => {
    await component.loadAll('ig-123');

    expect(mockInterestGroupService.getInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-123',
      }
    );
    expect(mockEmailService.getUserMailTemplatesAsync).toHaveBeenCalled();
    expect(component.currentIg()).toEqual(mockIg);
    expect(component.loaded()).toBe(true);
  });

  it('should prepend empty template after loading mail templates', async () => {
    await component.loadAll('ig-123');

    expect(component.templates()[0]).toEqual({
      id: '0',
      name: '',
      subject: '',
      text: '',
    });
    expect(component.selectedTemplateId()).toBe('0');
  });

  describe('tab management', () => {
    it('should default to email tab', () => {
      expect(component.isEmailTab()).toBe(true);
      expect(component.isUserTab()).toBe(false);
      expect(component.isFileTab()).toBe(false);
    });

    it('should switch tabs', () => {
      component.setTab('user');
      expect(component.isUserTab()).toBe(true);
      expect(component.isEmailTab()).toBe(false);

      component.setTab('file');
      expect(component.isFileTab()).toBe(true);
    });
  });

  describe('isSendable', () => {
    it('should return false when no recipients', () => {
      component.emailForm.patchValue({ subject: 'Test', content: 'Body' });
      expect(component.isSendable()).toBe(false);
    });

    it('should return false when subject is empty', () => {
      component.membersForm.patchValue({
        invitedUsersOrProfiles: [{ userId: 'u1' }],
      });
      component.emailForm.patchValue({ subject: '', content: 'Body' });
      expect(component.isSendable()).toBe(false);
    });

    it('should return true when all conditions met', () => {
      component.membersForm.patchValue({
        invitedUsersOrProfiles: [{ userId: 'u1' }],
      });
      component.emailForm.patchValue({ subject: 'Test', content: 'Body' });
      component.sending.set(false);
      expect(component.isSendable()).toBe(true);
    });

    it('should return false when sending', () => {
      component.membersForm.patchValue({
        invitedUsersOrProfiles: [{ userId: 'u1' }],
      });
      component.emailForm.patchValue({ subject: 'Test', content: 'Body' });
      component.sending.set(true);
      expect(component.isSendable()).toBe(false);
    });
  });

  describe('sendEmail', () => {
    beforeEach(async () => {
      paramsSubject.next({ id: 'ig-123' });
      await fixture.whenStable();
    });

    it('should call postGroupEmail and reset form', async () => {
      component.emailForm.patchValue({ subject: 'Sub', content: 'Content' });
      component.membersForm.patchValue({
        invitedUsersOrProfiles: [{ userId: 'user1' }],
      });
      component.selectedNodes.set(['node1']);

      await component.sendEmail();

      expect(mockEmailService.postGroupEmailAsync).toHaveBeenCalledWith({
        id: 'ig-123',
        emailDefinition: {
          subject: 'Sub',
          content: 'Content',
          users: [{ userId: 'user1' }],
          profiles: [],
          attachments: ['node1'],
        },
      });
      expect(component.sending()).toBe(false);
      expect(component.selectedNodes()).toEqual([]);
    });
  });

  describe('cancel', () => {
    it('should reset forms and clear selected nodes', () => {
      component.emailForm.patchValue({ subject: 'Test', content: 'Body' });
      component.selectedNodes.set(['n1', 'n2']);

      component.cancel();

      expect(component.emailForm.value.subject).toBeNull();
      expect(component.selectedNodes()).toEqual([]);
    });
  });

  describe('getNumberRecipients', () => {
    it('should return 0 when no recipients', () => {
      expect(component.getNumberRecipients()).toBe(0);
    });

    it('should return count of recipients', () => {
      component.membersForm.patchValue({
        invitedUsersOrProfiles: [{ userId: 'a' }, { userId: 'b' }],
      });
      expect(component.getNumberRecipients()).toBe(2);
    });
  });

  describe('getNumberAttachments', () => {
    it('should return count of selected nodes', () => {
      component.selectedNodes.set(['a', 'b', 'c']);
      expect(component.getNumberAttachments()).toBe(3);
    });
  });

  describe('selectTemplate', () => {
    beforeEach(async () => {
      paramsSubject.next({ id: 'ig-123' });
      await fixture.whenStable();
    });

    it('should update form fields with template values', () => {
      component.selectTemplate('1');

      expect(component.selectedTemplateId()).toBe('1');
      expect(component.emailForm.value.subject).toBe('Subject 1');
      expect(component.emailForm.value.content).toBe('<p>Body 1</p>');
    });
  });

  describe('isDirAdmin / isDirManageMembers', () => {
    beforeEach(async () => {
      paramsSubject.next({ id: 'ig-123' });
      await fixture.whenStable();
    });

    it('should delegate to permEvalService', () => {
      mockPermEvalService.isDirAdmin.mockReturnValue(true);
      expect(component.isDirAdmin()).toBe(true);
      expect(mockPermEvalService.isDirAdmin).toHaveBeenCalledWith(mockIg);
    });
  });

  describe('template save/remove', () => {
    beforeEach(async () => {
      paramsSubject.next({ id: 'ig-123' });
      await fixture.whenStable();
    });

    it('isRemovableTemplate should return false for id 0', () => {
      component.selectedTemplateId.set('0');
      expect(component.isRemovableTemplate()).toBe(false);
    });

    it('isRemovableTemplate should return true for non-zero id', () => {
      component.selectedTemplateId.set('1');
      expect(component.isRemovableTemplate()).toBe(true);
    });

    it('saveTemplate should call saveUserMailTemplate', async () => {
      component.emailForm.patchValue({
        templateName: 'MyTemplate',
        subject: 'Sub',
        content: 'Content',
        checkSaveTemplate: true,
      });

      await component.saveTemplate();

      expect(mockEmailService.saveUserMailTemplateAsync).toHaveBeenCalledWith({
        templateName: 'MyTemplate',
        templateSubject: 'Sub',
        templateText: 'Content',
        overwrite: false,
      });
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });

    it('removeTemplate should call deleteUserMailTemplates', async () => {
      component.selectedTemplateId.set('1');

      await component.removeTemplate();

      expect(
        mockEmailService.deleteUserMailTemplatesAsync
      ).toHaveBeenCalledWith({
        templateIds: ['1'],
      });
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });
  });

  describe('conditional templateName validation', () => {
    it('should require templateName when checkSaveTemplate is true', () => {
      component.emailForm.patchValue({ checkSaveTemplate: true });
      expect(component.emailForm.controls['templateName'].valid).toBe(false);
    });

    it('should not require templateName when checkSaveTemplate is false', () => {
      component.emailForm.patchValue({ checkSaveTemplate: false });
      expect(component.emailForm.controls['templateName'].valid).toBe(true);
    });
  });
});
