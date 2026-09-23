import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { FileExtensionIconComponent } from './file-extension-icon.component';

describe('FileExtensionIconComponent', () => {
  let fixture: ComponentFixture<FileExtensionIconComponent>;
  let component: FileExtensionIconComponent;
  let componentRef: ComponentRef<FileExtensionIconComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [FileExtensionIconComponent],
    });
    fixture = TestBed.createComponent(FileExtensionIconComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  describe('getExtension', () => {
    it('should return the extension for a known file type', () => {
      componentRef.setInput('filename', 'document.pdf');
      expect(component.getExtension()).toBe('pdf');
    });

    it('should return blank for an unknown extension', () => {
      componentRef.setInput('filename', 'file.xyz');
      expect(component.getExtension()).toBe('blank');
    });

    it('should return blank when filename has no extension', () => {
      componentRef.setInput('filename', 'noextension');
      expect(component.getExtension()).toBe('blank');
    });

    it('should return blank when filename is undefined', () => {
      expect(component.getExtension()).toBe('blank');
    });

    it('should handle filenames with multiple dots', () => {
      componentRef.setInput('filename', 'archive.backup.zip');
      expect(component.getExtension()).toBe('zip');
    });
  });

  describe('getFilePath', () => {
    it('should return the correct image path for a known extension', () => {
      componentRef.setInput('filename', 'report.docx');
      expect(component.getFilePath()).toBe(
        `${environment.baseHref}img/file-extensions/24-grey/docx-file-format.png`
      );
    });

    it('should return the blank image path when no filename is set', () => {
      expect(component.getFilePath()).toBe(
        `${environment.baseHref}img/file-extensions/24-grey/blank-file-format.png`
      );
    });
  });
});
