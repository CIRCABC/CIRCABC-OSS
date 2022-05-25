import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';

@Component({
  selector: 'cbc-file-list',
  templateUrl: './file-list.component.html',
  styleUrls: ['./file-list.component.scss'],
})
export class FileListComponent {
  @Input() fileList: FileUploadItem[] = [];
  @Input() uploadingFileName!: string;
  @Input() uploadingProgress = 0;
  @Input() maxFileSize = 1000 * 1000 * 300;
  @Output() readonly fileListChange = new EventEmitter<FileUploadItem[]>();

  public selectFile(file: FileUploadItem) {
    this.fileList.forEach((fileItem) => {
      if (fileItem.name === file.name && fileItem.selected) {
        fileItem.selected = false;
      } else if (fileItem.name === file.name && !fileItem.selected) {
        fileItem.selected = true;
      } else {
        fileItem.selected = false;
      }
    });

    this.fileListChange.emit(this.fileList);
  }

  public removeFile(file: FileUploadItem) {
    const idx = this.fileList.findIndex((fileItem) => {
      return fileItem.name === file.name;
    });
    this.fileList.splice(idx, 1);
    this.fileListChange.emit(this.fileList);
  }
}
