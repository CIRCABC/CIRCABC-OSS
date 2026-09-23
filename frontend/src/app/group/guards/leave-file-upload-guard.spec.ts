import { firstValueFrom, Observable } from 'rxjs';
import { vi } from 'vitest';
import { FileUploadItem } from '../library/upload-form/file-upload-item';
import { UploadFormComponent } from '../library/upload-form/upload-form.component';
import { canDeactivateUploadForm } from './leave-file-upload-guard';

describe('canDeactivateUploadForm', () => {
  let component: Pick<
    UploadFormComponent,
    'uploadFinished' | 'canceled' | 'filesToUpload'
  >;

  const mockFile: FileUploadItem = {
    id: '1',
    file: new File([], 'f.txt'),
    name: 'f.txt',
  };

  beforeEach(() => {
    component = { uploadFinished: false, canceled: false, filesToUpload: [] };
  });

  it('should return true when uploadFinished is true', () => {
    component.uploadFinished = true;
    component.filesToUpload = [mockFile];
    expect(canDeactivateUploadForm(component as UploadFormComponent)).toBe(
      true
    );
  });

  it('should return true when canceled is true', () => {
    component.canceled = true;
    component.filesToUpload = [mockFile];
    expect(canDeactivateUploadForm(component as UploadFormComponent)).toBe(
      true
    );
  });

  it('should return true when filesToUpload is empty', () => {
    expect(canDeactivateUploadForm(component as UploadFormComponent)).toBe(
      true
    );
  });

  it('should return of(true) when user confirms', async () => {
    component.filesToUpload = [mockFile];
    vi.spyOn(globalThis, 'confirm').mockReturnValue(true);

    const result = canDeactivateUploadForm(component as UploadFormComponent);
    expect(await firstValueFrom(result as Observable<boolean>)).toBe(true);
  });

  it('should return of(false) when user cancels', async () => {
    component.filesToUpload = [mockFile];
    vi.spyOn(globalThis, 'confirm').mockReturnValue(false);

    const result = canDeactivateUploadForm(component as UploadFormComponent);
    expect(await firstValueFrom(result as Observable<boolean>)).toBe(false);
  });
});
