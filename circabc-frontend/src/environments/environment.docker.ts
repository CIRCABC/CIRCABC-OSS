import { Environment } from 'environments/environment.interface';

/* eslint-disable */
export const environment: Environment = {
  production: false,
  alfrescoURL: '../service/api',
  circabcURL: '../service/circabc',
  serverURL: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? `:${window.location.port}` : ''
  }/`,
  alfrescoHost: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? `:${window.location.port}` : ''
  }`,
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
  officeClientId: '',
  shareURL: '',
  captchaURL: 'https://api.eucaptcha.eu',

  euloginUrl: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? `:${window.location.port}` : ''
  }/eulogin`,
  eulogoutUrl: `${window.location.protocol}//${window.location.hostname}${
    window.location.port ? `:${window.location.port}` : ''
  }/eulogout`,
  useAlfrescoAPI: false,
};
