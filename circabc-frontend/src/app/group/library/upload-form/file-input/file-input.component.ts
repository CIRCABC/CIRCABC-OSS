import { Component, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

@Component({
  selector: 'cbc-file-input',
  templateUrl: './file-input.component.html',
  styleUrl: './file-input.component.scss',
  imports: [
    NotificationMessageComponent,
    DataCyDirective,
    SpinnerComponent,
    SizePipe,
    TranslocoModule,
  ],
})
export class FileInputComponent {
  readonly fileSelected = output<FileUploadItem[]>();
  readonly disable = input(false);
  readonly maxFileUpload = input(300 * 1000 * 1000);
  readonly hasGuestAccess = input<boolean>();

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

  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

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
