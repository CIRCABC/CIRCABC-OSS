import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { environment } from 'environments/environment';
import { TranslocoService } from '@ngneat/transloco';

@Component({
  selector: 'cbc-legal-notice',
  templateUrl: './legal-notice.component.html',
  styleUrls: ['./legal-notice.component.scss'],
})
export class LegalNoticeComponent implements OnInit {
  public step = 'privacy';
  public serverURL = environment.serverURL;
  public urlPSExists = false;
  public urlTOSExists = false;

  constructor(
    private actroute: ActivatedRoute,
    public translateService: TranslocoService
  ) {}

  ngOnInit(): void {
    this.actroute.paramMap.subscribe((paramns) => {
      if (paramns.get('link') === 'terms') {
        this.step = 'terms';
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
}
