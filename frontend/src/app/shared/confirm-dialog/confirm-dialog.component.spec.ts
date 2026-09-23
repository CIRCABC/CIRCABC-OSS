import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ConfirmDialogComponent, DialogData } from './confirm-dialog.component';

const mockDialogData: DialogData = {
  title: 'Test Title',
  message: 'Test Message',
  message2: '',
  labelOK: 'OK',
  labelCancel: 'Cancel',
  layoutStyle: '',
  nodeLog: [],
  notify: null,
};

const mockDialogRef = {
  close: vi.fn(),
};

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;

  beforeEach(() => {
    mockDialogRef.close.mockReset();

    TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    const fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should inject dialog data', () => {
    expect(component.data).toEqual(mockDialogData);
  });

  it('onOk should close dialog with true', () => {
    component.onOk();
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('onCancel should close dialog with false', () => {
    component.onCancel();
    expect(mockDialogRef.close).toHaveBeenCalledWith(false);
  });

  it('closeNotification should close dialog with notify form value', () => {
    component.notifyFormGroup.controls.notify.setValue(false);
    component.closeNotification();
    expect(mockDialogRef.close).toHaveBeenCalledWith(false);
  });

  it('closeNotification should close dialog with true when notify is true', () => {
    component.notifyFormGroup.controls.notify.setValue(true);
    component.closeNotification();
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });
});
