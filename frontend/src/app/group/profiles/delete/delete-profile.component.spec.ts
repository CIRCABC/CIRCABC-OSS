import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ProfileService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteProfileComponent } from './delete-profile.component';

describe('DeleteProfileComponent', () => {
  let component: DeleteProfileComponent;
  let fixture: ComponentFixture<DeleteProfileComponent>;

  const mockProfileService = {
    deleteProfile: vi.fn().mockReturnValue(of(undefined)),
    deleteProfileAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  beforeEach(async () => {
    mockProfileService.deleteProfile.mockClear();
    mockProfileService.deleteProfile.mockReturnValue(of(undefined));
    mockProfileService.deleteProfileAsync.mockClear();
    mockProfileService.deleteProfileAsync.mockResolvedValue(undefined);
    mockUiMessageService.addErrorMessage.mockClear();

    await TestBed.configureTestingModule({
      imports: [DeleteProfileComponent],
      providers: [
        { provide: ProfileService, useValue: mockProfileService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(DeleteProfileComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DeleteProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('cancelWizard', () => {
    it('should emit canceled result and reset state', () => {
      const emitSpy = vi.spyOn(component.profileDeleted, 'emit');
      component.showModal.set(true);
      component.profile.set({ id: '123', name: 'Test' });

      component.cancelWizard();

      expect(component.showModal()).toBe(false);
      expect(component.profile()).toBeUndefined();
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.DELETE_PROFILE,
          result: ActionResult.CANCELED,
        })
      );
    });
  });

  describe('delete', () => {
    it('should delete profile and emit success result', async () => {
      const emitSpy = vi.spyOn(component.profileDeleted, 'emit');
      component.profile.set({ id: 'prof-1', name: 'Admin' });
      component.showModal.set(true);

      await component.delete();

      expect(mockProfileService.deleteProfileAsync).toHaveBeenCalledWith({
        id: 'prof-1',
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.DELETE_PROFILE,
          result: ActionResult.SUCCEED,
        })
      );
      expect(component.showModal()).toBe(false);
      expect(component.profile()).toBeUndefined();
      expect(component.deleting()).toBe(false);
    });

    it('should show error message on failure', async () => {
      mockProfileService.deleteProfileAsync.mockRejectedValue(
        new Error('fail')
      );
      component.profile.set({ id: 'prof-1', name: 'Admin' });

      await component.delete();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(component.deleting()).toBe(false);
    });

    it('should do nothing if profile has no id', async () => {
      const emitSpy = vi.spyOn(component.profileDeleted, 'emit');
      component.profile.set({} as never);

      await component.delete();

      expect(mockProfileService.deleteProfileAsync).not.toHaveBeenCalled();
      expect(emitSpy).not.toHaveBeenCalled();
      expect(component.deleting()).toBe(false);
    });
  });
});
