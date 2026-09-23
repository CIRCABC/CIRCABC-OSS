import { Environment } from 'environments/environment.interface';

/* eslint-disable @typescript-eslint/no-explicit-any -- runtime env values are untyped */
export const environment: Environment = {
  // biome-ignore lint/complexity/useSimplifiedLogicExpression: runtime env override pattern
  production: window.env?.production || true,
  alfrescoURL: window.env?.alfrescoURL || '../alfresco/service/api',
  circabcURL: window.env?.circabcURL || '../alfresco/service/circabc',
  serverURL:
    window.env?.serverURL ||
    `${window.location.protocol}//${window.location.hostname}${
      window.location.port ? `:${window.location.port}` : ''
    }/`,
  alfrescoHost:
    window.env?.alfrescoHost ||
    `${window.location.protocol}//${window.location.hostname}${
      window.location.port ? `:${window.location.port}` : ''
    }`,
  baseHref: window.env?.baseHref || '/circabc-caas/ui/',
  nodeName: window.env?.nodeName || 'N1',
  // biome-ignore lint/complexity/useSimplifiedLogicExpression: runtime env override pattern
  showUiSwitch: window.env?.showUiSwitch || false,
  environmentType: (window.env?.environmentType as any) || 'acc',
  circabcRelease: (window.env?.circabcRelease as any) || 'ent',
  // biome-ignore lint/complexity/useSimplifiedLogicExpression: runtime env override pattern
  aresBridgeEnabled: window.env?.aresBridgeEnabled || true,
  aresBridgeServer:
    window.env?.aresBridgeServer || 'https://webgate.acceptance.ec.testa.eu',
  aresBridgeURL:
    window.env?.aresBridgeURL ||
    'https://webgate.acceptance.ec.testa.eu/Ares/bridge/services/v1',
  aresBridgeKey:
    window.env?.aresBridgeKey || 'ab7c7293960c4538bc6535fe20aa3757',
  aresBridgeUiURL:
    window.env?.aresBridgeUiURL ||
    'https://webgate.acceptance.ec.testa.eu/Ares/bridge/ui',
  analyticsURL: window.env?.analyticsURL || 'circabc.acceptance.europa.eu',
  analyticsSiteId:
    window.env?.analyticsSiteId || 'fe2aaaa0-456a-44c0-a2d9-f7ef92d20d5d',
  analyticsInstance: window.env?.analyticsInstance || 'testing',
  officeClientId:
    window.env?.officeClientId || '32b07f1c-465c-477c-a325-f356ccc127e2',
  shareURL:
    window.env?.shareURL ||
    'https://circabc.acceptance.europa.eu/share/logincircabc',
  captchaURL:
    window.env?.captchaURL ||
    'https://circabc.acceptance.europa.eu/EuCaptcha-2.2.8',

  euloginUrl:
    window.env?.euloginUrl ||
    `${window.location.protocol}//${window.location.hostname}${
      window.location.port ? `:${window.location.port}` : ''
    }/ecas/eulogin`,
  eulogoutUrl:
    window.env?.eulogoutUrl ||
    `${window.location.protocol}//${window.location.hostname}${
      window.location.port ? `:${window.location.port}` : ''
    }/ecas/eulogin/logout/cas`,
  // biome-ignore lint/complexity/useSimplifiedLogicExpression: runtime env override pattern
  useAlfrescoAPI: window.env?.useAlfrescoAPI || true,
  aiAgentUrl: window.env?.aiAgentUrl || '',
};
