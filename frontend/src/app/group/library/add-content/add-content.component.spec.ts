import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UploadService } from 'app/core/upload.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddContentComponent } from './add-content.component';

const mockUploadService = {
  uploadNewFile: vi.fn(),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

describe('AddContentComponent', () => {
  let component: AddContentComponent;
  let fixture: ComponentFixture<AddContentComponent>;
  let componentRef: ComponentRef<AddContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddContentComponent],
      providers: [
        { provide: UploadService, useValue: mockUploadService },
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

    fixture = TestBed.createComponent(AddContentComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('parentNode', { id: 'node-123' });
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with empty file lists on ngOnInit', () => {
    component.ngOnInit();
    expect(component.filesToUpload).toEqual([]);
    expect(component.filesUploaded()).toEqual([]);
  });

  it('should update showAddWizardStep1 on ngOnChanges', () => {
    component.ngOnChanges({
      showWizard: {
        currentValue: true,
        previousValue: false,
        firstChange: true,
        isFirstChange: () => true,
      },
    });
    expect(component.showAddWizardStep1).toBe(true);
  });

  it('should add files via fileChangeEvent', () => {
    const file = new File(['content'], 'test.txt', { type: 'text/plain' });
    const fileList = {
      length: 1,
      item: (_i: number) => file,
    } as unknown as FileList;
    const event = { target: { files: fileList } } as unknown as Event;

    component.fileChangeEvent(event);

    expect(component.filesToUpload).toHaveLength(1);
    expect(component.filesToUpload[0].file.name).toBe('test.txt');
    expect(component.idCount).toBe(1);
  });

  it('should add files via drop', () => {
    const file = new File(['data'], 'dropped.txt');
    const event = {
      stopPropagation: vi.fn(),
      preventDefault: vi.fn(),
      dataTransfer: {
        files: { length: 1, item: () => file } as unknown as FileList,
      },
    } as unknown as DragEvent;

    component.drop(event);

    expect(component.filesToUpload).toHaveLength(1);
    expect(component.filesToUpload[0].file.name).toBe('dropped.txt');
  });

  it('should delete a selected file', () => {
    const file1 = new File(['a'], 'a.txt');
    const file2 = new File(['b'], 'b.txt');
    component.filesToUpload = [
      { id: 0, file: file1 },
      { id: 1, file: file2 },
    ];

    component.deleteSelectedFile({ id: 0, file: file1 });

    expect(component.filesToUpload).toHaveLength(1);
    expect(component.filesToUpload[0].id).toBe(1);
  });

  it('should return file names', () => {
    component.filesToUpload = [
      { id: 0, file: new File([''], 'a.txt') },
      { id: 1, file: new File([''], 'b.txt') },
    ];
    expect(component.fileNames()).toEqual(['a.txt', 'b.txt']);
  });

  it('should return total files size', () => {
    const f1 = new File(['abc'], 'a.txt');
    const f2 = new File(['de'], 'b.txt');
    component.filesToUpload = [
      { id: 0, file: f1 },
      { id: 1, file: f2 },
    ];
    expect(component.filesSize()).toBe(f1.size + f2.size);
  });

  it('should return 0 for filesSize when no files', () => {
    component.filesToUpload = [];
    expect(component.filesSize()).toBe(0);
  });

  it('should report upload finished when progress equals max', () => {
    component.progressValue.set(3);
    component.progressMax = 3;
    expect(component.isUploadFinished()).toBe(true);
  });

  it('should report upload not finished when progress < max', () => {
    component.progressValue.set(1);
    component.progressMax = 3;
    expect(component.isUploadFinished()).toBe(false);
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result on close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('close');

      expect(component.showAddWizardStep1).toBe(false);
      expect(component.showAddWizardStep2).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.UPLOAD_FILE,
      });
    });

    it('should emit SUCCEED result on finish', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('finish');

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.UPLOAD_FILE,
      });
    });

    it('should not emit on unknown backTo value', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.cancelWizard('unknown');
      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('launchAddWizardStep2', () => {
    it('should upload files and track progress', async () => {
      const file = new File(['test'], 'upload.txt');
      component.filesToUpload = [{ id: 0, file }];
      mockUploadService.uploadNewFile.mockResolvedValue('uploaded-node-id');

      await component.launchAddWizardStep2();

      expect(component.showAddWizardStep1).toBe(false);
      expect(component.showAddWizardStep2).toBe(true);
      expect(mockUploadService.uploadNewFile).toHaveBeenCalledWith(
        file,
        'node-123'
      );
      expect(component.filesUploaded()).toContain(file);
      expect(component.progressValue()).toBe(1);
    });

    it('should not proceed if no files to upload', async () => {
      component.filesToUpload = [];
      component.showAddWizardStep1 = true;

      await component.launchAddWizardStep2();

      expect(component.showAddWizardStep1).toBe(true);
      expect(component.showAddWizardStep2).toBe(false);
      expect(mockUploadService.uploadNewFile).not.toHaveBeenCalled();
    });

    it('should handle upload error gracefully', async () => {
      const file = new File(['fail'], 'fail.txt');
      component.filesToUpload = [{ id: 0, file }];
      mockUploadService.uploadNewFile.mockRejectedValue(
        new Error('network error')
      );

      await component.launchAddWizardStep2();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'Error during file upload'
      );
      expect(component.progressValue()).toBe(1);
      expect(component.filesUploaded()).toHaveLength(0);
    });
  });

  describe('drag events', () => {
    it('should prevent default on dragenter', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
      } as unknown as DragEvent;
      component.dragenter(event);
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should prevent default on dragover', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
      } as unknown as DragEvent;
      component.dragover(event);
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });
  });
});
