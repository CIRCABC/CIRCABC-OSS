import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';

@Component({
  selector: 'cbc-file-input',
  templateUrl: './file-input.component.html',
  styleUrls: ['./file-input.component.scss'],
})
export class FileInputComponent {
  @Output() readonly fileSelected = new EventEmitter<FileUploadItem[]>();
  @Input() disable = false;
  @Input() maxFileUpload = 300 * 1000 * 1000;
  @Input() hasGuestAccess: boolean | undefined;

  public filesToUpload: FileUploadItem[] = [];

  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  public drop(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();

    const dt = e.dataTransfer;
    if (dt !== null) {
      const files = dt.files;
      this.handleFiles(files);
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
    this.handleFiles(filesList);
  }

  private handleFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push({
          file: fileItem,
          id: fileItem.name,
          name: fileItem.name,
          selected: false,
          progress: 0,
          uploadStatus: 'start',
        });
      }
    }

    this.fileSelected.emit(this.filesToUpload);
    this.filesToUpload = [];
  }
}
