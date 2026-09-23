import { Environment } from 'environments/environment.interface';

export const environment: Environment = {
  production: true,
  alfrescoURL: 'http://ip-10-69-125-232.eu-west-1.compute.internal/service/api',
  circabcURL:
    'http://ip-10-69-125-232.eu-west-1.compute.internal/service/circabc',
  serverURL: 'http://ip-10-69-125-232.eu-west-1.compute.internal/',
  alfrescoHost: 'http://ip-10-69-125-232.eu-west-1.compute.internal',
  baseHref: '/ui/',
  nodeName: 'N1',
  showUiSwitch: false,
  environmentType: 'test',
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

  euloginUrl: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }/eulogin`,
  eulogoutUrl: `${globalThis.location.protocol}//${globalThis.location.hostname}${
    globalThis.location.port ? `:${globalThis.location.port}` : ''
  }/eulogout`,
  useAlfrescoAPI: false,
  aiAgentUrl: '',
};
