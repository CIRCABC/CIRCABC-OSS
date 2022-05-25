import { Component, Inject } from '@angular/core';

import {
  APP_ALF_VERSION,
  APP_VERSION,
  BUILD_DATE,
  NODE_NAME,
} from 'app/core/variables';

@Component({
  selector: 'cbc-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
  preserveWhitespaces: true,
})
export class FooterComponent {
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
