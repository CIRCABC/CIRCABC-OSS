import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { EmailService, type InterestGroup } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LeaderContactComponent } from './leader-contact.component';

const mockGroup: InterestGroup = {
  id: 'group-123',
  name: 'Test Group',
  permissions: {},
};

describe('LeaderContactComponent', () => {
  let component: LeaderContactComponent;
  let componentRef: ComponentRef<LeaderContactComponent>;
  let fixture: ComponentFixture<LeaderContactComponent>;
  let mockEmailService: {
    contactLeadersByEmail: ReturnType<typeof vi.fn>;
    contactLeadersByEmailAsync: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockEmailService = {
      contactLeadersByEmail: vi.fn().mockReturnValue(of(undefined)),
      contactLeadersByEmailAsync: vi.fn().mockResolvedValue(undefined),
    };

    await TestBed.configureTestingModule({
      imports: [LeaderContactComponent, ReactiveFormsModule],
      providers: [
        { provide: EmailService, useValue: mockEmailService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LeaderContactComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('group', mockGroup);
    fixture.detectChanges();
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form with empty message', () => {
    expect(component.contactLeaderForm.value.message).toBe('');
  });

  describe('send', () => {
    it('should call emailService and emit success', async () => {
      component.contactLeaderForm.setValue({ message: 'Hello leaders' });

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.send();

      expect(mockEmailService.contactLeadersByEmailAsync).toHaveBeenCalledWith({
        id: 'group-123',
        body: 'Hello leaders',
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CONTACT_LEADERS,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should emit failed on error', async () => {
      mockEmailService.contactLeadersByEmailAsync.mockRejectedValue(
        new Error('fail')
      );
      component.contactLeaderForm.setValue({ message: 'Hello' });

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.send();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CONTACT_LEADERS,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should not call emailService if group has no id', async () => {
      componentRef.setInput('group', { name: 'No ID', permissions: {} });
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      await component.send();

      expect(
        mockEmailService.contactLeadersByEmailAsync
      ).not.toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalled();
    });
  });

  describe('cancel', () => {
    it('should emit canceled result', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancel();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CONTACT_LEADERS,
        result: ActionResult.CANCELED,
      });
    });
  });
});
