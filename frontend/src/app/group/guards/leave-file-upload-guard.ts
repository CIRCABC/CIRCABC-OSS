import { UploadFormComponent } from 'app/group/library/upload-form/upload-form.component';
import { Observable, of } from 'rxjs';

/**
 * Functional `CanDeactivate` guard that protects the file upload form from
 * accidental navigation.
 *
 * When the user attempts to leave the upload page while an upload is still
 * pending, the guard prompts for confirmation so that in-progress work is not
 * lost unintentionally. Navigation is permitted immediately when the upload has
 * finished, has been canceled, or when there are no files queued for upload.
 *
 * @param component - The {@link UploadFormComponent} instance being deactivated.
 * @returns `true` to allow navigation without prompting, or an observable
 * emitting the user's confirmation choice (`true` to leave, `false` to stay).
 */
export const canDeactivateUploadForm = (
  component: UploadFormComponent
): boolean | Observable<boolean> => {
  if (
    component.uploadFinished ||
    component.canceled ||
    component.filesToUpload.length === 0
  ) {
    return true;
  }

  const confirmation = globalThis.confirm(
    'Confirm you want to leave the page ?'
  );

  return of(confirmation);
};
