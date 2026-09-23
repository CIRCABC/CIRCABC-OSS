import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FileInputComponent } from './file-input.component';

function createMockFileList(files: File[]): FileList {
  return {
    length: files.length,
    item: (index: number) => files[index] ?? null,
    [Symbol.iterator]: function* () {
      for (const f of files) yield f;
    },
  } as unknown as FileList;
}

function createMockDragEvent(dataTransfer: DataTransfer | null): DragEvent {
  return {
    stopPropagation: vi.fn(),
    preventDefault: vi.fn(),
    dataTransfer,
  } as unknown as DragEvent;
}

describe('FileInputComponent', () => {
  let component: FileInputComponent;
  let fixture: ComponentFixture<FileInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileInputComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FileInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('dragenter', () => {
    it('should stop propagation and prevent default', () => {
      const event = createMockDragEvent(null);
      component.dragenter(event);
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('dragover', () => {
    it('should stop propagation and prevent default', () => {
      const event = createMockDragEvent(null);
      component.dragover(event);
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });
  });

  describe('drop', () => {
    it('should handle dropped files and emit fileSelected', () => {
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      const fileList = createMockFileList([file]);
      const event = createMockDragEvent({
        files: fileList,
      } as unknown as DataTransfer);

      let emitted: FileUploadItem[] | undefined;
      component.fileSelected.subscribe((val: FileUploadItem[]) => {
        emitted = val;
      });

      component.drop(event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
      expect(emitted).toEqual([
        {
          file,
          id: 'test.txt',
          name: 'test.txt',
          selected: false,
          progress: 0,
          uploadStatus: 'start',
        },
      ]);
    });

    it('should not emit when dataTransfer is null', () => {
      const event = createMockDragEvent(null);

      let emitted = false;
      component.fileSelected.subscribe(() => {
        emitted = true;
      });

      component.drop(event);

      expect(emitted).toBe(false);
    });
  });

  describe('fileChangeEvent', () => {
    it('should handle selected files and emit fileSelected', () => {
      const file = new File(['data'], 'doc.pdf', { type: 'application/pdf' });
      const fileList = createMockFileList([file]);
      const input = {
        files: fileList,
        value: 'C:\\fakepath\\doc.pdf',
      } as unknown as HTMLInputElement;

      let emitted: FileUploadItem[] | undefined;
      component.fileSelected.subscribe((val: FileUploadItem[]) => {
        emitted = val;
      });

      component.fileChangeEvent({ target: input } as unknown as Event);

      expect(emitted).toEqual([
        {
          file,
          id: 'doc.pdf',
          name: 'doc.pdf',
          selected: false,
          progress: 0,
          uploadStatus: 'start',
        },
      ]);
      expect(input.value).toBe('');
    });

    it('should handle multiple files', () => {
      const file1 = new File(['a'], 'a.txt');
      const file2 = new File(['b'], 'b.txt');
      const fileList = createMockFileList([file1, file2]);
      const input = {
        files: fileList,
        value: '',
      } as unknown as HTMLInputElement;

      let emitted: FileUploadItem[] | undefined;
      component.fileSelected.subscribe((val: FileUploadItem[]) => {
        emitted = val;
      });

      component.fileChangeEvent({ target: input } as unknown as Event);

      expect(emitted).toHaveLength(2);
    });

    it('should reset filesToUpload after emitting', () => {
      const file = new File(['x'], 'x.txt');
      const fileList = createMockFileList([file]);
      const input = {
        files: fileList,
        value: '',
      } as unknown as HTMLInputElement;

      component.fileChangeEvent({ target: input } as unknown as Event);

      expect(component.filesToUpload).toEqual([]);
    });
  });
});
