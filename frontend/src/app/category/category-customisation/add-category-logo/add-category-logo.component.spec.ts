import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result/index';
import { CategoryService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddCategoryLogoComponent } from './add-category-logo.component';

describe('AddCategoryLogoComponent', () => {
  let component: AddCategoryLogoComponent;
  let componentRef: ComponentRef<AddCategoryLogoComponent>;
  let fixture: ComponentFixture<AddCategoryLogoComponent>;

  const mockCategoryService = {
    postCategoryLogoByCategoryId: vi.fn().mockReturnValue(of({})),
    postCategoryLogoByCategoryIdAsync: vi.fn().mockResolvedValue({}),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCategoryLogoComponent, ReactiveFormsModule],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
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

    fixture = TestBed.createComponent(AddCategoryLogoComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('categoryId', 'cat-123');
    componentRef.setInput('showModal', true);
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
    it('should clear files and emit modalHide', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.filesToUpload.set([new File([''], 'test.png')]);

      component.cancel();

      expect(component.filesToUpload()).toEqual([]);
      expect(emitSpy).toHaveBeenCalledWith({});
    });
  });

  describe('uploadLogo', () => {
    it('should upload successfully and emit SUCCEED result', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      const file = new File(['data'], 'logo.png');
      component.filesToUpload.set([file]);
      mockCategoryService.postCategoryLogoByCategoryIdAsync.mockResolvedValue(
        {}
      );

      await component.uploadLogo();

      expect(
        mockCategoryService.postCategoryLogoByCategoryIdAsync
      ).toHaveBeenCalledWith({ id: 'cat-123', fileData: file });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_CATEGORY_LOGO,
        result: ActionResult.SUCCEED,
      });
      expect(component.filesToUpload()).toEqual([]);
      expect(component.processing()).toBe(false);
    });

    it('should handle error and emit FAILED result', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      const translateService = TestBed.inject(TranslocoService);
      vi.spyOn(translateService, 'translate').mockReturnValue('Upload error');
      component.filesToUpload.set([new File(['data'], 'logo.png')]);
      mockCategoryService.postCategoryLogoByCategoryIdAsync.mockRejectedValue(
        new Error('fail')
      );

      await component.uploadLogo();

      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalledWith(
        'Upload error'
      );
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ADD_CATEGORY_LOGO,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('fileChangeEvent', () => {
    it('should populate filesToUpload from event', () => {
      const file = new File(['content'], 'image.png');
      const fileList = {
        length: 1,
        item: (i: number) => (i === 0 ? file : null),
        0: file,
      } as unknown as FileList;
      const event = { target: { files: fileList } } as unknown as Event;

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

    it('should return true for valid image under 1MB', () => {
      component.filesToUpload.set([
        new File(['x'], 'photo.png', { type: 'image/png' }),
      ]);
      expect(component.isValidImage()).toBe(true);
    });

    it('should return false for invalid extension', () => {
      component.filesToUpload.set([new File(['x'], 'file.exe')]);
      expect(component.isValidImage()).toBe(false);
    });

    it('should return false for file over 1MB', () => {
      const largeContent = new Uint8Array(1024 * 1024 + 1);
      component.filesToUpload.set([
        new File([largeContent], 'big.png', { type: 'image/png' }),
      ]);
      expect(component.isValidImage()).toBe(false);
    });
  });
});
