import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ShareComponent } from './share.component';

describe('ShareComponent', () => {
  let component: ShareComponent;
  let fixture: ComponentFixture<ShareComponent>;
  const mockDialog = { open: vi.fn() };
  const originalRelease = environment.circabcRelease;

  beforeEach(() => {
    mockDialog.open.mockReset();

    TestBed.configureTestingModule({
      imports: [ShareComponent],
      providers: [
        { provide: MatDialog, useValue: mockDialog },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    fixture = TestBed.createComponent(ShareComponent);
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

  it('should initialize customisationForm with addDownload false', () => {
    expect(component.customisationForm.controls['addDownload'].value).toBe(
      false
    );
  });

  describe('toggle', () => {
    it('should toggle showBox', () => {
      expect(component.showBox).toBe(false);
      component.toggle();
      expect(component.showBox).toBe(true);
      component.toggle();
      expect(component.showBox).toBe(false);
    });
  });

  describe('hide', () => {
    it('should set showBox to false', () => {
      component.showBox = true;
      component.hide();
      expect(component.showBox).toBe(false);
    });
  });

  describe('getLink', () => {
    it.each([
      [
        'no link input',
        'http://example.com/page',
        false,
        'http://example.com/page',
      ],
      [
        'addDownload is checked',
        'http://example.com/page',
        true,
        'http://example.com/page?download=true',
      ],
      [
        'URL already has query params',
        'http://example.com/page?foo=bar',
        true,
        'http://example.com/page?foo=bar&download=true',
      ],
      [
        'existing download param is stripped before re-adding',
        'http://example.com/page?download=true',
        true,
        'http://example.com/page?download=true',
      ],
    ])(
      'should return correct link when %s',
      (_, routeLink, addDownload, expected) => {
        component.routeLink = routeLink;
        component.customisationForm.controls['addDownload'].setValue(
          addDownload
        );
        expect(component.getLink()).toBe(expected);
      }
    );
  });

  describe('imageLink', () => {
    it('should return blue image link by default', () => {
      expect(component.imageLink).toBe('img/icon-share-blue.png');
    });
  });

  describe('showSnackBar', () => {
    it('should set copied to true then false after timeout', () => {
      vi.useFakeTimers();
      component.showSnackBar();
      expect(component.copied()).toBe(true);
      vi.advanceTimersByTime(1000);
      expect(component.copied()).toBe(false);
      vi.useRealTimers();
    });
  });

  describe('showDialogModel', () => {
    it('should call showSnackBar for copyTarget action', async () => {
      vi.spyOn(component, 'showSnackBar');
      await component.showDialogModel('copyTarget');
      expect(component.showSnackBar).toHaveBeenCalled();
    });

    it('should open confirm dialog for echa release with SENSITIVE ranking', async () => {
      (environment as { circabcRelease: string }).circabcRelease = 'echa';
      fixture.componentRef.setInput('securityRanking', 'SENSITIVE');
      fixture.detectChanges();

      mockDialog.open.mockReturnValue({ afterClosed: () => of(true) });
      vi.spyOn(component, 'showSnackBar');

      await component.showDialogModel('copyTarget');
      expect(mockDialog.open).toHaveBeenCalled();
      expect(component.showSnackBar).toHaveBeenCalled();
    });

    it('should not proceed when dialog is dismissed', async () => {
      (environment as { circabcRelease: string }).circabcRelease = 'echa';
      fixture.componentRef.setInput('securityRanking', 'SENSITIVE');
      fixture.detectChanges();

      mockDialog.open.mockReturnValue({ afterClosed: () => of(false) });
      vi.spyOn(component, 'showSnackBar');

      await component.showDialogModel('copyTarget');
      expect(mockDialog.open).toHaveBeenCalled();
      expect(component.showSnackBar).not.toHaveBeenCalled();
    });
  });

  describe('dialogAccepted', () => {
    it('should hide modal and show snackbar for copyTarget', () => {
      component.acceptSncShowModal = true;
      component.typeAction = 'copyTarget';
      vi.spyOn(component, 'showSnackBar');
      component.dialogAccepted();
      expect(component.acceptSncShowModal).toBe(false);
      expect(component.showSnackBar).toHaveBeenCalled();
    });
  });

  describe('dialogRefused', () => {
    it('should set acceptSncShowModal to false', () => {
      component.acceptSncShowModal = true;
      component.dialogRefused();
      expect(component.acceptSncShowModal).toBe(false);
    });
  });
});
