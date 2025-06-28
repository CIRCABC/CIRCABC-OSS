import { Component, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

@Component({
  selector: 'cbc-file-list',
  templateUrl: './file-list.component.html',
  styleUrl: './file-list.component.scss',
  imports: [SpinnerComponent, SizePipe, TranslocoModule],
})
export class FileListComponent {
  readonly fileList = input<FileUploadItem[]>([]);
  readonly uploadingFileName = input<string>();
  readonly uploadingProgress = input(0);
  readonly maxFileSize = input(1000 * 1000 * 300);
  readonly fileListChange = output<FileUploadItem[]>();

  public selectFile(file: FileUploadItem) {
    this.fileList().forEach((fileItem) => {
      if (fileItem.name === file.name && fileItem.selected) {
        fileItem.selected = false;
      } else if (fileItem.name === file.name && !fileItem.selected) {
        fileItem.selected = true;
      } else {
        fileItem.selected = false;
      }
    });

    this.fileListChange.emit(this.fileList());
  }

  public removeFile(file: FileUploadItem) {
    const idx = this.fileList().findIndex((fileItem) => {
      return fileItem.name === file.name;
    });
    this.fileList().splice(idx, 1);
    this.fileListChange.emit(this.fileList());
  }
}
