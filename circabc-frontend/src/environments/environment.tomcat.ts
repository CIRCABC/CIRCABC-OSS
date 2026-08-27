import { Environment } from 'environments/environment.interface';

/* eslint-disable */
export const environment: Environment = {
  production: false,
  alfrescoURL: 'http://localhost:8080/service/api',
  circabcURL: 'http://localhost:8080/service/circabc',
  serverURL: 'http://localhost:8080/',
  alfrescoHost: 'http://localhost:8080',
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: false,
  environmentType: 'local',
  circabcRelease: 'oss',
  aresBridgeEnabled: false,
  aresBridgeServer: '',
  aresBridgeURL: '',
  aresBridgeKey: '',
  aresBridgeUiURL: '',
  analyticsURL: '',
  analyticsSiteId: '',
  analyticsInstance: '',
  officeClientId: '32b07f1c-465c-477c-a325-f356ccc127e2',
  shareURL: 'https://circabc.acceptance.europa.eu/share/logincircabc',
  captchaURL: 'https://api.eucaptcha.eu',
  euloginUrl: 'http://localhost:8080/eulogin',
  eulogoutUrl: 'http://localhost:8080/eulogout',
  useAlfrescoAPI: false,
};

// captchaURL: 'http://localhost:8080',
// captchaURL: 'https://api.eucaptcha.eu',
// captchaURL: 'http://localhost:8080/EuCaptcha',

/*
 * To test tracking in Matomo
 *       circabcRelease: 'oss'
 *       analyticsURL: 'https://mymatomoaccount.matomo.cloud/',  *
 *       analyticsSiteId: '1',
 *       analyticsInstance: '', //only necessary for Webanalytics
 *
 *  To test tracking in Webanalitics CIRCABC
 *       circabcRelease: 'ent'
 *       analyticsURL: 'localhost', // Production => analyticsURL: 'circabc.europa.eu'
 *       analyticsSiteId: '43',
 *       analyticsInstance: 'analytics',
 *
 *  *  To test tracking in Webanalitics S-CIRCABC
 *       circabcRelease: 'ent'
 *       analyticsURL: '/secpac.jrc.cec.eu.int'
 *       analyticsSiteId: 'd42c0411-4283-4787-bd15-e0aafa2bdbba',
 *       analyticsInstance: 'ec',
 *
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
import 'zone.js/plugins/zone-error'; // Included with Angular CLI.
