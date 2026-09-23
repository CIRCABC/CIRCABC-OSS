import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  NgZone,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SaveAsService } from 'app/core/save-as.service';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

/**
 * Standalone Angular component (`cbc-save-as`) that renders a "save as" /
 * download control for a node (typically a document) identified by its id and
 * name.
 *
 * When triggered, it delegates the actual file download to the
 * {@link SaveAsService}. For the ECHA release, if the referenced content is
 * flagged as sensitive (SNC — Sensitive Non-Classified), it first prompts the
 * user with a confirmation dialog explaining the handling obligations before
 * proceeding with the download.
 *
 * Key collaborators:
 * - {@link SaveAsService} — performs the direct download of the file.
 * - {@link MatDialog} — opens the SNC confirmation dialog.
 * - {@link TranslocoService} — provides the translated confirmation message.
 * - {@link NgZone} — runs the download outside Angular's zone to avoid
 *   unnecessary change detection.
 */
@Component({
  selector: 'cbc-save-as',
  templateUrl: './save-as.component.html',
  styleUrl: './save-as.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class SaveAsComponent {
  /** Service that performs the actual file download. */
  private readonly saveAsService = inject(SaveAsService);
  /** Angular zone used to run the download outside change detection. */
  private readonly ngZone = inject(NgZone);
  /** Transloco service used to build the translated SNC dialog message. */
  private readonly translateService = inject(TranslocoService);
  /** Material dialog service used to open the SNC confirmation dialog. */
  private readonly dialog = inject(MatDialog);

  /** Input: the identifier of the node/document to download. */
  readonly id = input<string>();
  /** Input: the file name to use for the downloaded content. */
  readonly name = input<string>();
  /** Input: whether to display the download icon. Defaults to `true`. */
  readonly showIcon = input(true);
  /**
   * Input: whether the content is sensitive (SNC). When `true` on the ECHA
   * release, a confirmation dialog is shown before downloading. Defaults to
   * `false`.
   */
  readonly sensitive = input(false);

  /** Flag indicating whether the SNC acceptance modal is shown. */
  public acceptSncShowModal = false;

  /**
   * Triggers the download of the referenced node.
   *
   * On the ECHA release, when the content is flagged as {@link sensitive}, a
   * confirmation dialog is displayed first; the download is aborted if the
   * user does not confirm. Otherwise the download is delegated to
   * {@link SaveAsService.saveAsDirect} and executed outside the Angular zone.
   * The download only proceeds when both {@link id} and {@link name} are set.
   *
   * @returns A promise that resolves once the confirmation flow has completed
   * and the download has been initiated (or skipped).
   */
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

  /**
   * Opens the SNC (Sensitive Non-Classified) download confirmation dialog and
   * waits for the user's response.
   *
   * The dialog presents a translated message that includes a link to the
   * relevant Commission decision C(2019)1904 and requires the user to
   * acknowledge before downloading.
   *
   * @returns A promise that resolves with the dialog result once it is closed
   * (truthy when the user confirms, falsy otherwise).
   */
  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
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
