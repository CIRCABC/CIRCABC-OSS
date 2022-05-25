import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { of } from 'rxjs';

import { UploadFormComponent } from 'app/group/library/upload-form/upload-form.component';

@Injectable()
export class LeaveFileUploadGuard
  implements CanDeactivate<UploadFormComponent>
{
  canDeactivate(component: UploadFormComponent) {
    if (
      component.uploadFinished ||
      component.canceled ||
      component.filesToUpload.length === 0
    ) {
      return true;
    }

    const confirmation = window.confirm('Confirm you want to leave the page ?');

    return of(confirmation);
  }
}
