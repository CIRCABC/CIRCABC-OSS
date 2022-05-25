// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `angular-cli.json`.

import { Environment } from 'environments/environment.interface';

/* eslint-disable */
export const environment: Environment = {
  production: false,
  alfrescoURL: '../service/api',
  circabcURL: '../service/circabc',
  serverURL: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? ':' + window.location.port : ''
  }/`,
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: false,
  environmentType: 'dev',
  circabcRelease: 'ent',
  aresBridgeEnabled: true,
  aresBridgeServer: 'https://heraresd.development.ec.europa.eu',
  aresBridgeURL:
    'https://heraresd.development.ec.europa.eu/Ares/bridge/services/v1',
  aresBridgeKey: 'c4c750c74d8e44beb36e12ed9c8db2ab',
  aresBridgeUiURL: 'https://heraresd.development.ec.europa.eu/Ares/bridge/ui',
  analyticsURL: 'circabc.development.europa.eu',
  analyticsSiteId: 43,
  officeClientId: '32b07f1c-465c-477c-a325-f356ccc127e2',
  shareURL: 'https://circabc.development.europa.eu/share/logincircabc',
};
/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
import 'zone.js/plugins/zone-error'; // Included with Angular CLI.
