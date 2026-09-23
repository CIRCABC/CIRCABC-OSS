import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { SaveAsService } from '../../core/save-as.service';
import { SaveAsComponent } from './save-as.component';

class MockLoader {
  getTranslation() {
    return of({});
  }
}

describe('SaveAsComponent', () => {
  let component: SaveAsComponent;
  let fixture: ComponentFixture<SaveAsComponent>;
  let mockSaveAsService: { saveAsDirect: ReturnType<typeof vi.fn> };
  let mockDialog: { open: ReturnType<typeof vi.fn> };

  const originalRelease = environment.circabcRelease;

  beforeEach(async () => {
    mockSaveAsService = { saveAsDirect: vi.fn() };
    mockDialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [SaveAsComponent],
      providers: [
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: MatDialog, useValue: mockDialog },
        provideTransloco({
          config: {
            defaultLang: 'en',
            availableLangs: ['en'],
            reRenderOnLangChange: false,
          },
          loader: MockLoader,
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SaveAsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    (environment as { circabcRelease: string }).circabcRelease =
      originalRelease;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call saveAsDirect when id and name are set', async () => {
    fixture.componentRef.setInput('id', '123');
    fixture.componentRef.setInput('name', 'file.pdf');
    fixture.detectChanges();

    await component.download();

    expect(mockSaveAsService.saveAsDirect).toHaveBeenCalledWith(
      '123',
      'file.pdf'
    );
  });

  it('should not call saveAsDirect when id is missing', async () => {
    fixture.componentRef.setInput('name', 'file.pdf');
    fixture.detectChanges();

    await component.download();

    expect(mockSaveAsService.saveAsDirect).not.toHaveBeenCalled();
  });

  it('should not call saveAsDirect when name is missing', async () => {
    fixture.componentRef.setInput('id', '123');
    fixture.detectChanges();

    await component.download();

    expect(mockSaveAsService.saveAsDirect).not.toHaveBeenCalled();
  });

  describe('when circabcRelease is echa and sensitive is true', () => {
    beforeEach(() => {
      (environment as { circabcRelease: string }).circabcRelease = 'echa';
      fixture.componentRef.setInput('id', '123');
      fixture.componentRef.setInput('name', 'file.pdf');
      fixture.componentRef.setInput('sensitive', true);
      fixture.detectChanges();
    });

    it('should show dialog and proceed if confirmed', async () => {
      const dialogRef = {
        afterClosed: () => of(true),
      } as unknown as MatDialogRef<unknown>;
      mockDialog.open.mockReturnValue(dialogRef);

      await component.download();

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockSaveAsService.saveAsDirect).toHaveBeenCalledWith(
        '123',
        'file.pdf'
      );
    });

    it('should show dialog and abort if not confirmed', async () => {
      const dialogRef = {
        afterClosed: () => of(false),
      } as unknown as MatDialogRef<unknown>;
      mockDialog.open.mockReturnValue(dialogRef);

      await component.download();

      expect(mockDialog.open).toHaveBeenCalled();
      expect(mockSaveAsService.saveAsDirect).not.toHaveBeenCalled();
    });
  });
});
