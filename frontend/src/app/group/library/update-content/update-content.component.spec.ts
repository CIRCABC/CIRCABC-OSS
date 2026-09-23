import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UpdateContentComponent } from './update-content.component';

vi.mock('environments/environment', () => ({
  environment: {
    circabcRelease: 'ent',
  },
}));

describe('UpdateContentComponent', () => {
  let component: UpdateContentComponent;
  let componentRef: ComponentRef<UpdateContentComponent>;
  let fixture: ComponentFixture<UpdateContentComponent>;

  const mockUploadService = {
    updateExistingFileContent: vi.fn().mockResolvedValue('node-id'),
    updateCheckedOutFileContent: vi.fn().mockResolvedValue(undefined),
  };

  const mockDialog = {
    open: vi.fn().mockReturnValue({ afterClosed: () => of(true) }),
  };

  const mockTranslocoService = {
    translate: vi.fn().mockReturnValue('translated'),
  };

  const targetNode: ModelNode = {
    id: 'node-123',
    name: 'test.pdf',
    properties: {},
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateContentComponent],
      providers: [
        { provide: UploadService, useValue: mockUploadService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: TranslocoService, useValue: mockTranslocoService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateContentComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('targetNode', targetNode);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
    (environment as { circabcRelease: string }).circabcRelease = 'ent';
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with default values', () => {
    expect(component.fileToUpload()).toBeUndefined();
    expect(component.uploading()).toBe(false);
    expect(component.showWizard()).toBe(false);
    expect(component.updateCheckedOut()).toBe(false);
  });

  describe('fileChangeEvent', () => {
    it('should set fileToUpload from input event', () => {
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      const fileList = {
        length: 1,
        item: (i: number) => (i === 0 ? file : null),
      } as FileList;

      const event = { target: { files: fileList } } as unknown as Event;
      component.fileChangeEvent(event);

      expect(component.fileToUpload()).toBe(file);
    });
  });

  describe('drop', () => {
    it('should set fileToUpload from dropped files', () => {
      const file = new File(['content'], 'dropped.txt', { type: 'text/plain' });
      const fileList = {
        length: 1,
        item: (i: number) => (i === 0 ? file : null),
      } as FileList;

      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: { files: fileList },
      } as unknown as DragEvent;
      component.drop(event);

      expect(component.fileToUpload()).toBe(file);
    });

    it('should not set fileToUpload when dataTransfer is null', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
        dataTransfer: null,
      } as unknown as DragEvent;
      component.drop(event);

      expect(component.fileToUpload()).toBeUndefined();
    });
  });

  describe('dragenter', () => {
    it('should prevent default', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
      } as unknown as DragEvent;
      component.dragenter(event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('dragover', () => {
    it('should prevent default', () => {
      const event = {
        stopPropagation: vi.fn(),
        preventDefault: vi.fn(),
      } as unknown as DragEvent;
      component.dragover(event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('update', () => {
    it('should call updateExistingFileContent when file is set and not checked out', async () => {
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.update();

      expect(mockUploadService.updateExistingFileContent).toHaveBeenCalledWith(
        file,
        'node-123',
        true
      );
      expect(component.fileToUpload()).toBeUndefined();
      expect(component.uploading()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.UPDATE_FILE_CONTENT,
      });
    });

    it('should call updateCheckedOutFileContent when updateCheckedOut is true', async () => {
      componentRef.setInput('updateCheckedOut', true);
      fixture.detectChanges();

      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      await component.update();

      expect(
        mockUploadService.updateCheckedOutFileContent
      ).toHaveBeenCalledWith(file, 'node-123', true);
    });

    it('should not upload when fileToUpload is undefined', async () => {
      component.fileToUpload.set(undefined);

      await component.update();

      expect(
        mockUploadService.updateExistingFileContent
      ).not.toHaveBeenCalled();
      expect(
        mockUploadService.updateCheckedOutFileContent
      ).not.toHaveBeenCalled();
    });

    it('should show confirm dialog for echa release with SENSITIVE ranking', async () => {
      (environment as { circabcRelease: string }).circabcRelease = 'echa';

      const sensitiveNode: ModelNode = {
        id: 'node-456',
        properties: { security_ranking: 'SENSITIVE' },
      };
      componentRef.setInput('targetNode', sensitiveNode);
      fixture.detectChanges();

      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      await component.update();

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockUploadService.updateExistingFileContent).toHaveBeenCalled();
    });

    it('should abort update when confirm dialog returns falsy for echa SENSITIVE', async () => {
      (environment as { circabcRelease: string }).circabcRelease = 'echa';
      mockDialog.open.mockReturnValue({ afterClosed: () => of(false) });

      const sensitiveNode: ModelNode = {
        id: 'node-456',
        properties: { security_ranking: 'SENSITIVE' },
      };
      componentRef.setInput('targetNode', sensitiveNode);
      fixture.detectChanges();

      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      await component.update();

      expect(
        mockUploadService.updateExistingFileContent
      ).not.toHaveBeenCalled();
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result when backTo is close', () => {
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('close');

      expect(component.fileToUpload()).toBeUndefined();
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.UPDATE_FILE_CONTENT,
      });
    });

    it('should not emit when backTo is not close', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('other');

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('notifyFormGroup', () => {
    it('should default notify to true', () => {
      expect(component.notifyFormGroup.controls.notify.value).toBe(true);
    });

    it('should use notify value from form when updating', async () => {
      component.notifyFormGroup.controls.notify.setValue(false);
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      component.fileToUpload.set(file);

      await component.update();

      expect(mockUploadService.updateExistingFileContent).toHaveBeenCalledWith(
        file,
        'node-123',
        false
      );
    });
  });
});
