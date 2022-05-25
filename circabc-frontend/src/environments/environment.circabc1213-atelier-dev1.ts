import { Environment } from 'environments/environment.interface';

// tslint:disable:no-http-string
export const environment: Environment = {
  production: false,
  alfrescoURL: 'http://vs-cbc-dev1.net1.cec.eu.int:52080/service/api',
  circabcURL: 'http://vs-cbc-dev1.net1.cec.eu.int:52080/service/circabc',
  serverURL: 'http://vs-cbc-dev1.net1.cec.eu.int:52080/',
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: true,
  environmentType: 'dev',
  circabcRelease: 'ent',
  aresBridgeEnabled: false,
  aresBridgeServer: '',
  aresBridgeURL: '',
  aresBridgeKey: '',
  aresBridgeUiURL: '',
};
