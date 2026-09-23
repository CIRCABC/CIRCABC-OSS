import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { BASE_PATH, InterestGroupService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ImportComponent } from './import.component';

describe('ImportComponent', () => {
  let component: ImportComponent;
  let fixture: ComponentFixture<ImportComponent>;

  const mockInterestGroupService = {
    postImportZipFile: vi.fn().mockReturnValue(of(undefined)),
    postImportZipFileAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockTranslocoService = {
    translate: vi.fn((key: string) => key),
  };

  const mockDialog = {
    open: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [ImportComponent],
      providers: [
        ReactiveFormsModule,
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: BASE_PATH, useValue: 'http://localhost:7001' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('targetNode', {
      id: 'node-123',
      name: 'Test Node',
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    expect(component.importForm.controls['notifyUser'].value).toBe(true);
    expect(component.importForm.controls['deleteFile'].value).toBe(true);
    expect(component.importForm.controls['disableNotification'].value).toBe(
      true
    );
    expect(component.importForm.controls['encoding'].value).toBe('CP437');
  });

  describe('importExceeds', () => {
    it('should return true when size exceeds max', () => {
      const exceeding = component.importMaxSize * 1024 * 1024 + 1;
      expect(component.importExceeds(exceeding)).toBe(true);
    });

    it('should return false when size is within limit', () => {
      const within = component.importMaxSize * 1024 * 1024 - 1;
      expect(component.importExceeds(within)).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should emit modalHide with CANCELED result when backTo is close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.fileToUpload.set(new File([''], 'test.zip'));

      component.cancel('close');

      expect(component.fileToUpload()).toBeUndefined();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.IMPORT_ZIP,
      });
    });

    it('should not emit when backTo is not close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancel('other');
      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('import', () => {
    it('should not proceed if fileToUpload is undefined', async () => {
      component.fileToUpload.set(undefined);
      await component.import();
      expect(
        mockInterestGroupService.postImportZipFileAsync
      ).not.toHaveBeenCalled();
    });

    it('should not proceed if file exceeds max size', async () => {
      const bigFile = new File(['x'], 'big.zip');
      Object.defineProperty(bigFile, 'size', { value: 100 * 1024 * 1024 });
      component.fileToUpload.set(bigFile);

      await component.import();
      expect(
        mockInterestGroupService.postImportZipFileAsync
      ).not.toHaveBeenCalled();
    });

    it('should call postImportZipFile and emit success on success', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      const file = new File(['content'], 'test.zip');
      component.fileToUpload.set(file);

      await component.import();

      expect(
        mockInterestGroupService.postImportZipFileAsync
      ).toHaveBeenCalledWith({
        folderId: 'node-123',
        notifyUser: true,
        deleteFile: true,
        disableNotification: true,
        encoding: 'CP437',
        fileData: file,
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.IMPORT_ZIP,
      });
      expect(component.fileToUpload()).toBeUndefined();
      expect(component.uploading()).toBe(false);
    });

    it('should emit failure and show error for duplicate file', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      mockInterestGroupService.postImportZipFileAsync.mockRejectedValue({
        error: {
          message: 'A file with this name already exists in the folder',
        },
      });
      component.fileToUpload.set(new File(['content'], 'test.zip'));

      await component.import();

      expect(mockTranslocoService.translate).toHaveBeenCalledWith(
        'import.zip.exists.failed'
      );
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.FAILED,
        type: ActionType.IMPORT_ZIP,
      });
      expect(component.uploading()).toBe(false);
    });

    it('should emit failure and show error for file too big', async () => {
      mockInterestGroupService.postImportZipFileAsync.mockRejectedValue({
        error: { message: 'File is too big to be imported' },
      });
      component.fileToUpload.set(new File(['content'], 'test.zip'));

      await component.import();

      expect(mockTranslocoService.translate).toHaveBeenCalledWith(
        'import.zip.big.failed'
      );
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });
  });

  describe('getImportIndexFileTemplate', () => {
    it('should call saveUrlAs with correct URL and return false', () => {
      const result = component.getImportIndexFileTemplate();

      expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
        'http://localhost:7001/groups/import/template',
        'index.txt'
      );
      expect(result).toBe(false);
    });
  });

  describe('drag and drop', () => {
    it('should set fileToUpload on drop', () => {
      const file = new File(['content'], 'dropped.zip');
      const dataTransfer = { files: [file] } as unknown as DataTransfer;
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer,
      } as unknown as DragEvent;

      component.drop(event);

      expect(component.fileToUpload()).toBe(file);
    });

    it('should not set fileToUpload if dataTransfer is null', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: null,
      } as unknown as DragEvent;

      component.drop(event);

      expect(component.fileToUpload()).toBeUndefined();
    });
  });
});
