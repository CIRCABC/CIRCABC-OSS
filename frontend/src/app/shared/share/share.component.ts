import { ClipboardModule } from '@angular/cdk/clipboard';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

/**
 * Standalone component (`cbc-share`) that renders a "share" control (a share
 * icon and optional label) which, when toggled, reveals a small box allowing
 * the user to copy the current resource link to the clipboard or send it by
 * e-mail.
 *
 * The component builds the shareable link from either an explicit {@link link}
 * input or the current browser URL, and can optionally append a
 * `download=true` query parameter based on a reactive form checkbox.
 *
 * For the ECHA release, sharing of `SENSITIVE` or `SPECIAL_HANDLING` resources
 * triggers a confirmation dialog ({@link ConfirmDialogComponent}) before the
 * link is copied or e-mailed.
 *
 * Key collaborators: {@link FormBuilder} (customisation form),
 * {@link TranslocoService} (i18n of dialog messages) and {@link MatDialog}
 * (security confirmation dialog).
 */
@Component({
  selector: 'cbc-share',
  templateUrl: './share.component.html',
  styleUrl: './share.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ClipboardModule,
    ReactiveFormsModule,
    TranslocoModule,
    ModalComponent,
  ],
})
export class ShareComponent implements OnInit {
  /** Builder used to create the reactive customisation form. */
  private readonly formBuilder = inject(FormBuilder);
  /** Translation service used to translate the security confirmation message. */
  private readonly translateService = inject(TranslocoService);
  /** Angular Material dialog service used to open the confirmation dialog. */
  private readonly dialog = inject(MatDialog);

  /** Whether the shared resource is flagged as sensitive. */
  readonly sensitive = input(false);
  /** Whether to display a textual label next to the share icon. */
  readonly showLabel = input(true);
  /** Whether the share box is aligned to the right of the trigger. */
  readonly orientationRight = input(true);
  /** Whether the "add direct download" option is enabled in the share box. */
  readonly enableDirectDownload = input(false);
  /** Explicit link to share; when omitted the current browser URL is used. */
  readonly link = input<string>();
  /** Colour variant of the share icon: `'grey'` or `'blue'`. */
  readonly color = input<'grey' | 'blue'>('blue'); // grey or blue

  /** Security ranking of the resource (e.g. `SENSITIVE`, `SPECIAL_HANDLING`). */
  readonly securityRanking = input<string>();

  /** Whether the share box is currently visible. */
  public showBox = false;
  /** Whether the "copied" confirmation feedback is currently shown. */
  public readonly copied = signal(false);
  /** The current resource link, initialised from the browser location. */
  public routeLink = globalThis.location.href;
  /** Reactive form holding the customisation options (e.g. add download). */
  public customisationForm!: FormGroup;
  /** Whether the SNC (sensitive) acceptance modal should be shown. */
  public acceptSncShowModal = false;
  /** Free-form message used by the component's UI. */
  public message = '';
  /** The action being performed: either `'copyTarget'` or `'emailMe'`. */
  public typeAction = '';

  /**
   * Angular lifecycle hook. Initialises the customisation form with a single
   * `addDownload` control defaulting to `false`.
   */
  ngOnInit(): void {
    this.customisationForm = this.formBuilder.group({
      addDownload: [false],
    });
  }

  /**
   * Toggles the visibility of the share box. When opening, refreshes the
   * current link and synchronises the `addDownload` form control with whether
   * the current URL already requests a direct download.
   */
  toggle() {
    this.showBox = !this.showBox;
    this.routeLink = globalThis.location.href;

    this.customisationForm.controls.addDownload.setValue(
      this.routeLink.includes('download=true')
    );
  }

  /** Hides the share box. */
  hide() {
    this.showBox = false;
  }

  /**
   * Builds the shareable link. Uses the explicit {@link link} input when
   * provided, otherwise the current route link. Any existing `download=`
   * query parameter is stripped, and a `download=true` parameter is appended
   * when the `addDownload` option is selected.
   *
   * @returns The fully resolved link to share.
   */
  getLink() {
    let result: string;
    const link = this.link();
    if (link) {
      result = link;
    } else {
      result = this.routeLink;
    }

    if (result.includes('download=')) {
      result = result.substring(0, result.indexOf('download=') - 1);
    }

    if (this.customisationForm.value.addDownload) {
      result = `${result}${
        result.includes('?') ? '&download=true' : '?download=true'
      }`;
    }

    return result;
  }

  /**
   * Path to the share icon image matching the current {@link color} variant.
   *
   * @returns The relative image path (e.g. `img/icon-share-blue.png`).
   */
  get imageLink(): string {
    return `img/icon-share-${this.color()}.png`;
  }

  /**
   * Shows the "copied" confirmation feedback for one second before hiding it.
   */
  public showSnackBar() {
    this.copied.set(true);
    setTimeout(() => {
      this.copied.set(false);
    }, 1000);
  }

  /**
   * Callback for the clipboard copy action. Triggers the copy-target dialog
   * flow when the copy succeeded.
   *
   * @param isCopied Whether the link was successfully copied to the clipboard.
   */
  public ClipboardCopied(isCopied: boolean) {
    if (isCopied) {
      this.showDialogModel('copyTarget');
    }
  }

  /**
   * Performs the requested share action, optionally gated by a security
   * confirmation dialog. For the ECHA release, when the resource is
   * `SENSITIVE` or `SPECIAL_HANDLING` the user must confirm before proceeding.
   * On confirmation, either the copied feedback is shown (`copyTarget`) or an
   * e-mail draft is opened (`emailMe`).
   *
   * @param typeAction The action to perform: `'copyTarget'` or `'emailMe'`.
   * @returns A promise that resolves once the action (or its cancellation)
   * has completed.
   */
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
      globalThis.location.href = `mailto:?Subject=Link to CIRCABC&Body=${this.getLink()}`;
    }
  }

  /**
   * Opens the SNC (sensitive/special handling) confirmation dialog with a
   * translated message and waits for the user's decision.
   *
   * @returns A promise resolving to the dialog result (`true` when confirmed).
   */
  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
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

  /**
   * Handles acceptance of the SNC modal: closes the modal and completes the
   * pending action (copy feedback or opening an e-mail draft).
   */
  dialogAccepted() {
    this.acceptSncShowModal = false;
    if (this.typeAction === 'copyTarget') {
      this.showSnackBar();
    } else {
      globalThis.location.href = `mailto:?Subject=Link to CIRCABC&Body=${this.getLink()}`;
    }
  }

  /** Handles refusal of the SNC modal by simply closing it. */
  dialogRefused() {
    this.acceptSncShowModal = false;
  }
}
