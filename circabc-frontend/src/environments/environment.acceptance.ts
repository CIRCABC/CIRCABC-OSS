import { Environment } from 'environments/environment.interface';

/* eslint-disable */
export const environment: Environment = {
  production: true,
  alfrescoURL: '../service/api',
  circabcURL: '../service/circabc',
  serverURL: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? ':' + window.location.port : ''
  }/`,
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: false,
  environmentType: 'acc',
  circabcRelease: 'ent',
  aresBridgeEnabled: true,
  aresBridgeServer: 'https://webgate.acceptance.ec.testa.eu',
  aresBridgeURL:
    'https://webgate.acceptance.ec.testa.eu/Ares/bridge/services/v1',
  aresBridgeKey: 'ab7c7293960c4538bc6535fe20aa3757',
  aresBridgeUiURL: 'https://webgate.acceptance.ec.testa.eu/Ares/bridge/ui',
  analyticsURL: 'circabc.acceptance.europa.eu',
  analyticsSiteId: 43,
  officeClientId: '32b07f1c-465c-477c-a325-f356ccc127e2',
  shareURL: 'https://circabc.acceptance.europa.eu/share/logincircabc',
};
