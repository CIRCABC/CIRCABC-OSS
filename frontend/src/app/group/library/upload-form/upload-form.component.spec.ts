import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import {
  FileService,
  NodesService,
  PermissionService,
  TranslationsService,
} from 'app/core/generated/circabc';
import { ALF_BASE_PATH, CBC_BASE_PATH } from 'app/core/variables';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FileUploadItem } from './file-upload-item';
import { UploadFormComponent } from './upload-form.component';

function makeFileItem(overrides: Partial<FileUploadItem> = {}): FileUploadItem {
  return {
    id: '1',
    file: new File(['content'], 'test.txt', { type: 'text/plain' }),
    name: 'test.txt',
    ...overrides,
  };
}

describe('UploadFormComponent', () => {
  let component: UploadFormComponent;
  let fixture: ComponentFixture<UploadFormComponent>;

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({ id: 'node1', name: 'folder' }),
  };
  const mockFileService = {
    uploadFileAsync: vi.fn().mockResolvedValue({ nodeRef: 'ref1' }),
    fireNewContentNotificationAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockTranslationsService = {
    postTranslationEnhancedAsync: vi.fn().mockResolvedValue({}),
  };
  const mockPermissionService = {
    putPermissionAsync: vi.fn().mockResolvedValue({}),
  };
  const mockRouter = { navigate: vi.fn() };
  const mockRoute = { params: of({ nodeId: 'target1' }) };
  const mockDialog = { open: vi.fn() };
  const mockTranslocoService = {
    translate: vi.fn().mockReturnValue('translated'),
    config: { reRenderOnLangChange: false, defaultLang: 'en' },
    langChanges$: of('en'),
    getActiveLang: vi.fn().mockReturnValue('en'),
    selectTranslation: vi.fn().mockReturnValue(of({})),
    selectTranslate: vi.fn().mockReturnValue(of('')),
    load: vi.fn().mockReturnValue(of({})),
    _loadDependencies: vi.fn().mockReturnValue(of([])),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTransloco({
          config: {
            defaultLang: 'en',
            availableLangs: ['en'],
            reRenderOnLangChange: false,
          },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: ALF_BASE_PATH, useValue: '' },
        { provide: CBC_BASE_PATH, useValue: '' },
        { provide: NodesService, useValue: mockNodesService },
        { provide: FileService, useValue: mockFileService },
        { provide: TranslationsService, useValue: mockTranslationsService },
        { provide: PermissionService, useValue: mockPermissionService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: MatDialog, useValue: mockDialog },
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    })
      .overrideComponent(UploadFormComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(UploadFormComponent);
    component = fixture.componentInstance;
  });

  describe('targetNode loading', () => {
    it('should compute targetNodeId from route params and load target node', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.targetNodeId()).toBe('target1');
      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'target1',
      });
      expect(component.targetNode()).toEqual({ id: 'node1', name: 'folder' });
    });
  });

  describe('addNewFiles', () => {
    it('should add files that are not already in selection', () => {
      const file = makeFileItem();
      component.addNewFiles([file]);
      expect(component.filesToUpload).toHaveLength(1);
      expect(component.fileSelected).toBe(file);
    });

    it('should not add duplicate files', () => {
      const file = makeFileItem();
      component.addNewFiles([file]);
      component.addNewFiles([file]);
      expect(component.filesToUpload).toHaveLength(1);
    });
  });

  describe('isFileInSelection', () => {
    it('should return true if file with same name exists', () => {
      component.filesToUpload = [makeFileItem({ name: 'a.txt' })];
      expect(component.isFileInSelection(makeFileItem({ name: 'a.txt' }))).toBe(
        true
      );
    });

    it('should return false if file is not in selection', () => {
      component.filesToUpload = [makeFileItem({ name: 'a.txt' })];
      expect(component.isFileInSelection(makeFileItem({ name: 'b.txt' }))).toBe(
        false
      );
    });
  });

  describe('fileListChanged', () => {
    it('should update filesToUpload and set fileSelected for selected file', () => {
      const files = [
        makeFileItem({ id: '1', selected: true }),
        makeFileItem({ id: '2', name: 'other.txt' }),
      ];
      component.fileListChanged(files);
      expect(component.filesToUpload).toBe(files);
      expect(component.fileSelected).toBe(files[0]);
    });

    it('should set fileSelected to undefined if no file is selected', () => {
      component.fileListChanged([makeFileItem({ selected: false })]);
      expect(component.fileSelected).toBeUndefined();
    });
  });

  describe('startNewUpload', () => {
    it('should reset upload state', () => {
      component.uploadingProgress = 5;
      component.uploadFinished = true;
      component.filesToUpload = [makeFileItem()];
      component.fileSelected = makeFileItem();

      component.startNewUpload();

      expect(component.uploadingProgress).toBe(0);
      expect(component.uploadFinished).toBe(false);
      expect(component.filesToUpload).toEqual([]);
      expect(component.fileSelected).toBeUndefined();
    });
  });

  describe('cancelOrClose', () => {
    it('should set canceled and navigate to parent', () => {
      component.cancelOrClose();
      expect(component.canceled).toBe(true);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['..'], {
        relativeTo: mockRoute,
      });
    });
  });

  describe('preparePivotDocuments', () => {
    it('should filter pivot documents', () => {
      component.filesToUpload = [
        makeFileItem({ id: '1', isPivot: true }),
        makeFileItem({ id: '2', name: 'b.txt', isPivot: false }),
      ];
      component.preparePivotDocuments();
      expect(component.pivotDocuments).toHaveLength(1);
      expect(component.pivotDocuments[0].id).toBe('1');
    });
  });

  describe('prepareTranslations', () => {
    it('should filter translation files', () => {
      component.filesToUpload = [
        makeFileItem({ id: '1', isTranslation: true }),
        makeFileItem({ id: '2', name: 'b.txt', isTranslation: false }),
      ];
      component.prepareTranslations();
      expect(component.translations).toHaveLength(1);
    });
  });

  describe('getNodeRefOfPivot', () => {
    it('should return nodeRef of matching pivot', () => {
      component.filesToUpload = [
        makeFileItem({ id: 'p1', nodeRef: 'ref-abc' }),
      ];
      expect(component.getNodeRefOfPivot('p1')).toBe('ref-abc');
    });

    it('should return empty string if pivot not found', () => {
      component.filesToUpload = [];
      expect(component.getNodeRefOfPivot('missing')).toBe('');
    });
  });

  describe('anyInvalidName', () => {
    it('should return true for empty name', () => {
      component.filesToUpload = [makeFileItem({ name: '' })];
      expect(component.anyInvalidName()).toBe(true);
    });

    it('should return true for name with invalid characters', () => {
      component.filesToUpload = [makeFileItem({ name: 'file*.txt' })];
      expect(component.anyInvalidName()).toBe(true);
    });

    it('should return false for valid names', () => {
      component.filesToUpload = [makeFileItem({ name: 'valid-file.txt' })];
      expect(component.anyInvalidName()).toBe(false);
    });
  });

  describe('anyFileTooLarge', () => {
    it('should return true if any file exceeds max size', () => {
      const largeFile = new File(['x'], 'big.bin');
      Object.defineProperty(largeFile, 'size', { value: 1024 * 1024 * 301 });
      component.filesToUpload = [makeFileItem({ file: largeFile })];
      expect(component.anyFileTooLarge()).toBe(true);
    });

    it('should return false if all files are within limit', () => {
      component.filesToUpload = [makeFileItem()];
      expect(component.anyFileTooLarge()).toBe(false);
    });
  });

  describe('nameValidator', () => {
    it('should return null for valid name', () => {
      expect(component.nameValidator('document.pdf')).toBeNull();
    });

    it('should return null for null input', () => {
      expect(component.nameValidator(null as unknown as string)).toBeNull();
    });

    it('should return error for empty/whitespace name', () => {
      expect(component.nameValidator('   ')).toEqual({
        invalidFileName: { additionalInfo: 'empty name' },
      });
    });

    it('should return error for name with forbidden characters', () => {
      expect(component.nameValidator('file:name')).toEqual({
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      });
    });

    it('should return error for name ending with dot', () => {
      expect(component.nameValidator('file.')).toEqual({
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      });
    });

    it('should return error for name ending with space', () => {
      expect(component.nameValidator('file ')).toEqual({
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      });
    });
  });

  describe('uploadFiles', () => {
    it('should set uploading to true', () => {
      component.filesToUpload = [makeFileItem()];
      component.uploadFiles();
      expect(component.uploading).toBe(true);
    });
  });
});
