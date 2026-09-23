import { Environment } from 'environments/environment.interface';

export const environment: Environment = {
  production: false,
  alfrescoURL: '../service/api',
  circabcURL: '../service/circabc',
  serverURL: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }/`,
  alfrescoHost: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }`,
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: false,
  environmentType: 'training',
  circabcRelease: 'ent',
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
  captchaURL: 'https://circabc.europa.eu/EuCaptcha-2.2.8',
  euloginUrl: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }/eulogin`,
  eulogoutUrl: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }/eulogout`,
  useAlfrescoAPI: false,
  aiAgentUrl: '',
};
