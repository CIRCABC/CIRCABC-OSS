import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FileListComponent } from './file-list.component';

function createFile(name: string, selected = false): FileUploadItem {
  return { id: name, name, file: new File([], name), selected };
}

describe('FileListComponent', () => {
  let component: FileListComponent;
  let fixture: ComponentFixture<FileListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileListComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(FileListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('selectFile', () => {
    it('should select the given file and deselect others', () => {
      const files = [createFile('a.txt'), createFile('b.txt')];
      fixture.componentRef.setInput('fileList', files);
      fixture.detectChanges();

      component.selectFile(files[1]);

      expect(files[0].selected).toBe(false);
      expect(files[1].selected).toBe(true);
    });

    it('should deselect a file if it is already selected', () => {
      const files = [createFile('a.txt', true)];
      fixture.componentRef.setInput('fileList', files);
      fixture.detectChanges();

      component.selectFile(files[0]);

      expect(files[0].selected).toBe(false);
    });

    it('should emit fileListChange on select', () => {
      const files = [createFile('a.txt')];
      fixture.componentRef.setInput('fileList', files);
      fixture.detectChanges();

      const spy = vi.fn();
      component.fileListChange.subscribe(spy);

      component.selectFile(files[0]);

      expect(spy).toHaveBeenCalledWith(files);
    });
  });

  describe('removeFile', () => {
    it('should remove the specified file from the list', () => {
      const files = [createFile('a.txt'), createFile('b.txt')];
      fixture.componentRef.setInput('fileList', files);
      fixture.detectChanges();

      component.removeFile(files[0]);

      expect(component.fileList()).toHaveLength(1);
      expect(component.fileList()[0].name).toBe('b.txt');
    });

    it('should emit fileListChange after removal', () => {
      const files = [createFile('a.txt')];
      fixture.componentRef.setInput('fileList', files);
      fixture.detectChanges();

      const spy = vi.fn();
      component.fileListChange.subscribe(spy);

      component.removeFile(files[0]);

      expect(spy).toHaveBeenCalledWith(files);
    });
  });
});
