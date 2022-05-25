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
  environmentType: 'prod',
  circabcRelease: 'ent',
  aresBridgeEnabled: true,
  aresBridgeServer: 'https://webgate.ec.testa.eu',
  aresBridgeURL: 'https://webgate.ec.testa.eu/Ares/bridge/services/v1',
  aresBridgeKey: '6d474506912a497e90012554bb2d3126',
  aresBridgeUiURL: 'https://webgate.ec.testa.eu/Ares/bridge/ui',
  analyticsURL: '',
  analyticsSiteId: 0,
  officeClientId: '',
  shareURL: 'https://circabc.europa.eu/share/logincircabc',
};
