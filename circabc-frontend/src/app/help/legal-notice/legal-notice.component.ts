import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { environment } from 'environments/environment';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@Component({
  selector: 'cbc-legal-notice',
  templateUrl: './legal-notice.component.html',
  styleUrl: './legal-notice.component.scss',
  imports: [NgxExtendedPdfViewerModule, SetTitlePipe, TranslocoModule],
})
export class LegalNoticeComponent implements OnInit {
  public step = 'privacy';
  public serverURL = environment.serverURL;
  public urlPSExists = false;
  public urlTOSExists = false;
  public urlACCESSExists = false;

  constructor(
    private actroute: ActivatedRoute,
    public translateService: TranslocoService
  ) {}

  ngOnInit(): void {
    this.actroute.paramMap.subscribe((paramns) => {
      if (paramns.get('link') === 'terms') {
        this.step = 'terms';
      }
      if (paramns.get('link') === 'accessibility') {
        this.step = 'accessibility';
      }
    });
    this.fileExists();
  }

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

  urlPS() {
    return `${
      environment.serverURL
    }ps/ps-${this.translateService.getActiveLang()}.pdf`;
  }

  urlTOS() {
    return `${
      environment.serverURL
    }tos/tos-${this.translateService.getActiveLang()}.pdf`;
  }

  urlACCESS() {
    return `${
      environment.serverURL
    }access/access-${this.translateService.getActiveLang()}.pdf`;
  }
}
