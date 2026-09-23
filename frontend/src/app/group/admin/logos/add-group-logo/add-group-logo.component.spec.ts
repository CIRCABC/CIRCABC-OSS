import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result/index';
import { InterestGroupService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddGroupLogoComponent } from './add-group-logo.component';

describe('AddGroupLogoComponent', () => {
  let component: AddGroupLogoComponent;
  let componentRef: ComponentRef<AddGroupLogoComponent>;
  let fixture: ComponentFixture<AddGroupLogoComponent>;

  const mockGroupService = {
    postGroupNewLogoAsync: vi.fn().mockResolvedValue({}),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddGroupLogoComponent, ReactiveFormsModule],
      providers: [
        { provide: InterestGroupService, useValue: mockGroupService },
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

    fixture = TestBed.createComponent(AddGroupLogoComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group-123');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize logoForm on init', () => {
    expect(component.logoForm).toBeDefined();
    expect(component.logoForm.contains('file')).toBe(true);
  });

  describe('cancel', () => {
    it('should clear files and emit result', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.filesToUpload.set([new File([''], 'test.png')]);

      component.cancel();

      expect(component.filesToUpload()).toEqual([]);
      expect(emitSpy).toHaveBeenCalledWith({});
    });
  });

  describe('uploadLogo', () => {
    it('should return early if groupId is undefined', async () => {
      componentRef.setInput('groupId', undefined);
      fixture.detectChanges();

      await component.uploadLogo();

      expect(mockGroupService.postGroupNewLogoAsync).not.toHaveBeenCalled();
    });

    it('should upload logo and emit success', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      const file = new File(['data'], 'logo.png');
      component.filesToUpload.set([file]);

      await component.uploadLogo();

      expect(mockGroupService.postGroupNewLogoAsync).toHaveBeenCalledWith({
        id: 'group-123',
        fileData: file,
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_CATEGORY_LOGO,
        result: ActionResult.SUCCEED,
      });
      expect(component.filesToUpload()).toEqual([]);
      expect(component.processing()).toBe(false);
    });

    it('should handle upload error', async () => {
      mockGroupService.postGroupNewLogoAsync.mockRejectedValue(
        new Error('fail')
      );
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.filesToUpload.set([new File(['data'], 'logo.png')]);

      await component.uploadLogo();

      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_CATEGORY_LOGO,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('fileChangeEvent', () => {
    it('should populate filesToUpload from event', () => {
      const file = new File([''], 'image.png');
      const event = {
        target: { files: { length: 1, item: () => file } },
      } as unknown as Event;

      component.fileChangeEvent(event);

      expect(component.filesToUpload()).toEqual([file]);
    });
  });

  describe('getFileName', () => {
    it('should return empty string when no files', () => {
      expect(component.getFileName()).toBe('');
    });

    it('should return file name when file exists', () => {
      component.filesToUpload.set([new File([''], 'test.png')]);
      expect(component.getFileName()).toBe('test.png');
    });
  });

  describe('hasFile', () => {
    it('should return false when no files', () => {
      expect(component.hasFile()).toBe(false);
    });

    it('should return true when files exist', () => {
      component.filesToUpload.set([new File([''], 'test.png')]);
      expect(component.hasFile()).toBe(true);
    });
  });

  describe('isValidImage', () => {
    it('should return false when no files', () => {
      expect(component.isValidImage()).toBe(false);
    });

    it('should return false for invalid extension', () => {
      component.filesToUpload.set([new File(['data'], 'file.txt')]);
      expect(component.isValidImage()).toBe(false);
    });

    it('should return false for file exceeding 1MB', () => {
      const largeContent = new Uint8Array(1024 * 1024 + 1);
      component.filesToUpload.set([new File([largeContent], 'big.png')]);
      expect(component.isValidImage()).toBe(false);
    });

    it('should return true for valid image under 1MB', () => {
      component.filesToUpload.set([new File(['data'], 'logo.png')]);
      expect(component.isValidImage()).toBe(true);
    });
  });
});
