import { Component, Inject } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  APP_ALF_VERSION,
  APP_VERSION,
  BUILD_DATE,
  NODE_NAME,
} from 'app/core/variables';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule],
})
export class FooterComponent {
  public circabcRelease = environment.circabcRelease;
  public appAlfVersion = '';
  public appVersion = '';
  public nodeName = '';
  public buildDate = '';

  constructor(
    @Inject(APP_ALF_VERSION) appAlfVersion: string,
    @Inject(APP_VERSION) appVersion: string,
    @Inject(NODE_NAME) nodeName: string,
    @Inject(BUILD_DATE) buildDate: string
  ) {
    if (appAlfVersion) {
      this.appAlfVersion = appAlfVersion;
    }
    if (appVersion) {
      this.appVersion = appVersion;
    }
    if (nodeName) {
      this.nodeName = nodeName;
    }
    if (buildDate) {
      this.buildDate = buildDate;
    }
  }
}
