import { Component, NgZone, input } from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SaveAsService } from 'app/core/save-as.service';
import { environment } from 'environments/environment';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-save-as',
  templateUrl: './save-as.component.html',
  styleUrl: './save-as.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class SaveAsComponent {
  readonly id = input<string>();
  readonly name = input<string>();
  readonly showIcon = input(true);
  readonly sensitive = input(false);

  public acceptSncShowModal = false;

  constructor(
    private saveAsService: SaveAsService,
    private ngZone: NgZone,
    private translateService: TranslocoService,
    private dialog: MatDialog
  ) {}

  public async download() {
    if (environment.circabcRelease === 'echa' && this.sensitive()) {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    this.ngZone.runOutsideAngular(() => {
      const id = this.id();
      const name = this.name();
      if (id && name) {
        this.saveAsService.saveAsDirect(id, name);
      }
    });
  }

  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.download',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.download.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
