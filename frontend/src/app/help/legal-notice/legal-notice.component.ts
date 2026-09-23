import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { environment } from 'environments/environment';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

/**
 * Renders the CIRCABC legal notice help page, which displays one of three
 * legal documents as a PDF: the Privacy Statement, the Terms of Service, or
 * the Accessibility statement.
 *
 * The document to show is selected via the `link` route parameter and stored
 * in {@link LegalNoticeComponent.step}. Each document is fetched from the
 * server as a language-specific PDF (based on the currently active Transloco
 * language) and rendered with `ngx-extended-pdf-viewer`. Before rendering, the
 * component probes the server to verify that the requested PDF actually exists.
 */
@Component({
  selector: 'cbc-legal-notice',
  templateUrl: './legal-notice.component.html',
  styleUrl: './legal-notice.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgxExtendedPdfViewerModule,
    SetTitlePipe,
    TranslocoModule,
    RouterLink,
  ],
})
export class LegalNoticeComponent implements OnInit {
  /** Provides access to the current route, used to read the `link` parameter. */
  private readonly actroute = inject(ActivatedRoute);
  /** Transloco service used to resolve the active language for PDF URLs. */
  translateService = inject(TranslocoService);

  /**
   * Currently selected legal document to display. One of `'privacy'`,
   * `'terms'` or `'accessibility'`. Defaults to `'privacy'`.
   */
  public step = signal('privacy');
  /** Base server URL from which the legal PDF documents are served. */
  public serverURL = environment.serverURL;
  /** Whether the current build is the ECHA (S-CIRCABC) release variant. */
  public isSCircabc = environment.circabcRelease === 'echa';
  /** Whether the language-specific Privacy Statement PDF exists on the server. */
  public urlPSExists = false;
  /** Whether the language-specific Terms of Service PDF exists on the server. */
  public urlTOSExists = false;
  /** Whether the language-specific Accessibility PDF exists on the server. */
  public urlACCESSExists = false;

  /**
   * Angular lifecycle hook. Reads the `link` route parameter to determine which
   * legal document ({@link step}) to display, then verifies which of the legal
   * PDFs are available on the server via {@link fileExists}.
   */
  ngOnInit(): void {
    this.actroute.paramMap.subscribe((paramns) => {
      if (paramns.get('link') === 'terms') {
        this.step.set('terms');
      }
      if (paramns.get('link') === 'accessibility') {
        this.step.set('accessibility');
      }
    });
    this.fileExists();
  }

  /**
   * Synchronously checks whether each language-specific legal PDF (Privacy
   * Statement, Terms of Service and Accessibility) exists on the server by
   * issuing blocking `HEAD` requests, and updates {@link urlPSExists},
   * {@link urlTOSExists} and {@link urlACCESSExists} accordingly.
   */
  fileExists() {
    const requestPS = new XMLHttpRequest();
    requestPS.open('HEAD', this.urlPS(), false);
    requestPS.send();
    if (requestPS.readyState === 4 && requestPS.status === 200) {
      this.urlPSExists = true;
    } else {
      this.urlPSExists = false;
    }

    const requesTOS = new XMLHttpRequest();
    requesTOS.open('HEAD', this.urlTOS(), false);
    requesTOS.send();
    if (requesTOS.readyState === 4 && requesTOS.status === 200) {
      this.urlTOSExists = true;
    } else {
      this.urlTOSExists = false;
    }

    const requesACCESS = new XMLHttpRequest();
    requesACCESS.open('HEAD', this.urlACCESS(), false);
    requesACCESS.send();
    if (requesACCESS.readyState === 4 && requesACCESS.status === 200) {
      this.urlACCESSExists = true;
    } else {
      this.urlACCESSExists = false;
    }
  }

  /**
   * Builds the URL of the Privacy Statement PDF for the active language.
   *
   * @returns The fully qualified URL to the language-specific Privacy
   * Statement PDF.
   */
  urlPS() {
    return `${
      environment.serverURL
    }ps/ps-${this.translateService.getActiveLang()}.pdf`;
  }

  /**
   * Builds the URL of the Terms of Service PDF for the active language.
   *
   * @returns The fully qualified URL to the language-specific Terms of
   * Service PDF.
   */
  urlTOS() {
    return `${
      environment.serverURL
    }tos/tos-${this.translateService.getActiveLang()}.pdf`;
  }

  /**
   * Builds the URL of the Accessibility statement PDF for the active language.
   *
   * @returns The fully qualified URL to the language-specific Accessibility
   * statement PDF.
   */
  urlACCESS() {
    return `${
      environment.serverURL
    }access/access-${this.translateService.getActiveLang()}.pdf`;
  }
}
