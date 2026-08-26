import { ClipboardModule } from '@angular/cdk/clipboard';
import { Component, OnInit, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { environment } from 'environments/environment';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-share',
  templateUrl: './share.component.html',
  styleUrl: './share.component.scss',
  preserveWhitespaces: true,
  imports: [
    ClipboardModule,
    ReactiveFormsModule,
    TranslocoModule,
    ModalComponent,
  ],
})
export class ShareComponent implements OnInit {
  readonly sensitive = input(false);
  readonly showLabel = input(true);
  readonly orientationRight = input(true);
  readonly enableDirectDownload = input(false);
  readonly link = input<string>();
  readonly color = input<'grey' | 'blue'>('blue'); // grey or blue

  readonly securityRanking = input<string>();

  public showBox = false;
  public copied = false;
  public routeLink = window.location.href;
  public customisationForm!: FormGroup;
  public acceptSncShowModal = false;
  public message = '';
  public typeAction = '';

  public constructor(
    private formBuilder: FormBuilder,
    private translateService: TranslocoService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.customisationForm = this.formBuilder.group({
      addDownload: [false],
    });
  }

  toggle() {
    this.showBox = !this.showBox;
    this.routeLink = window.location.href;

    this.customisationForm.controls.addDownload.setValue(
      this.routeLink.includes('download=true')
    );
  }

  hide() {
    this.showBox = false;
  }

  getLink() {
    let result = '';
    const link = this.link();
    if (link) {
      result = link;
    } else {
      result = this.routeLink;
    }

    if (result.indexOf('download=') !== -1) {
      result = result.substring(0, result.indexOf('download=') - 1);
    }

    if (this.customisationForm.value.addDownload) {
      result = `${result}${
        result.indexOf('?') !== -1 ? '&download=true' : '?download=true'
      }`;
    }

    return result;
  }

  get imageLink(): string {
    return `img/icon-share-${this.color()}.png`;
  }

  public showSnackBar() {
    this.copied = true;
    setTimeout(() => {
      this.copied = false;
    }, 1000);
  }

  public ClipboardCopied(isCopied: boolean) {
    if (isCopied) {
      this.showDialogModel('copyTarget');
    }
  }

  public async showDialogModel(typeAction: 'copyTarget' | 'emailMe') {
    this.typeAction = typeAction;
    const security_ranking = this.securityRanking();
    if (
      environment.circabcRelease === 'echa' &&
      (security_ranking === 'SENSITIVE' ||
        security_ranking === 'SPECIAL_HANDLING')
    ) {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    if (typeAction === 'copyTarget') {
      this.showSnackBar();
    } else {
      window.location.href = `mailto:?Subject=Link to CIRCABC&Body=${this.getLink()}`;
    }
  }

  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.share',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.share.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }

  dialogAccepted() {
    this.acceptSncShowModal = false;
    if (this.typeAction === 'copyTarget') {
      this.showSnackBar();
    } else {
      window.location.href = `mailto:?Subject=Link to CIRCABC&Body=${this.getLink()}`;
    }
  }

  dialogRefused() {
    this.acceptSncShowModal = false;
  }
}
