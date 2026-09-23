import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UploadService } from 'app/core/upload.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ChangeAvatarComponent } from './change-avatar.component';

const mockUser = { userId: 'testUser', email: 'test@test.com' };

const mockLoginService = {
  getUser: vi.fn().mockReturnValue(mockUser),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockUploadService = {
  updateAvatar: vi.fn().mockResolvedValue(undefined),
};

const mockTranslocoService = {
  translate: vi.fn().mockReturnValue('error message'),
};

describe('ChangeAvatarComponent', () => {
  let component: ChangeAvatarComponent;
  let fixture: ComponentFixture<ChangeAvatarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeAvatarComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: UploadService, useValue: mockUploadService },
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeAvatarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user on init', () => {
    expect(mockLoginService.getUser).toHaveBeenCalled();
  });

  describe('fileNameValid', () => {
    it('should return true when no file is selected', () => {
      component.fileToUpload.set(undefined);
      expect(component.fileNameValid()).toBe(true);
    });

    it('should return true for valid image file under 1MB', () => {
      component.fileToUpload.set(
        new File(['x'], 'photo.png', {
          type: 'image/png',
        })
      );
      expect(component.fileNameValid()).toBe(true);
    });

    it('should return false for non-image file', () => {
      component.fileToUpload.set(
        new File(['x'], 'doc.pdf', {
          type: 'application/pdf',
        })
      );
      expect(component.fileNameValid()).toBe(false);
    });

    it('should return false for file over 1MB', () => {
      const bigContent = new Uint8Array(1024 * 1024 + 1);
      component.fileToUpload.set(
        new File([bigContent], 'big.png', {
          type: 'image/png',
        })
      );
      expect(component.fileNameValid()).toBe(false);
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result and reset state on close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.showAddWizardStep1 = true;
      component.showAddWizardStep2 = true;

      component.cancelWizard('close');

      expect(component.showWizard()).toBe(false);
      expect(component.showAddWizardStep1).toBe(false);
      expect(component.showAddWizardStep2).toBe(false);
      expect(component.fileToUpload()).toBeUndefined();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.UPLOAD_FILE,
      });
    });

    it('should emit SUCCEED result on finish', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('finish');

      expect(component.showWizard()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.UPLOAD_FILE,
      });
    });

    it('should do nothing for unknown backTo value', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('unknown');
      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('launchAddWizardStep2', () => {
    it('should not proceed if file name is invalid', async () => {
      component.fileToUpload.set(new File(['x'], 'doc.txt'));
      await component.launchAddWizardStep2();
      expect(component.showAddWizardStep2).toBe(false);
      expect(mockUploadService.updateAvatar).not.toHaveBeenCalled();
    });

    it('should upload file and emit avatarUploaded on success', async () => {
      const emitSpy = vi.spyOn(component.avatarUploaded, 'emit');
      component.fileToUpload.set(
        new File(['x'], 'avatar.png', {
          type: 'image/png',
        })
      );

      await component.launchAddWizardStep2();

      expect(component.showAddWizardStep1).toBe(false);
      expect(component.showAddWizardStep2).toBe(true);
      expect(mockUploadService.updateAvatar).toHaveBeenCalledWith(
        'testUser',
        expect.any(File)
      );
      expect(emitSpy).toHaveBeenCalledWith({ result: ActionResult.SUCCEED });
    });

    it('should show error and cancel on upload failure', async () => {
      mockUploadService.updateAvatar.mockRejectedValueOnce(new Error('fail'));
      component.fileToUpload.set(
        new File(['x'], 'avatar.jpg', {
          type: 'image/jpeg',
        })
      );

      await component.launchAddWizardStep2();

      expect(mockTranslocoService.translate).toHaveBeenCalledWith(
        'error.image.upload'
      );
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'error message'
      );
    });
  });

  describe('drag and drop', () => {
    it('should set fileToUpload on drop', () => {
      const file = new File(['x'], 'dropped.png');
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: { files: [file] },
      } as unknown as DragEvent;

      component.drop(event);

      expect(component.fileToUpload()).toBe(file);
    });

    it('should handle null dataTransfer on drop', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: null,
      } as unknown as DragEvent;

      component.drop(event);
      expect(component.fileToUpload()).toBeUndefined();
    });
  });

  describe('fileChangeEvent', () => {
    it('should set fileToUpload from input event', () => {
      const file = new File(['x'], 'selected.png');
      const event = { target: { files: [file] } } as unknown as Event;

      component.fileChangeEvent(event);

      expect(component.fileToUpload()).toBe(file);
    });
  });
});
