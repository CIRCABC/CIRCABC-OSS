export interface Environment {
  production: boolean;
  alfrescoURL: string;
  circabcURL: string;
  serverURL: string;
  alfrescoHost: string;
  baseHref: string;
  nodeName: string;
  showUiSwitch: boolean;
  environmentType:
    | 'prod'
    | 'dev'
    | 'acc'
    | 'stress'
    | 'test'
    | 'local'
    | 'training';
  circabcRelease: 'ent' | 'oss' | 'echa' | 'olaf';
  aresBridgeEnabled: boolean;
  aresBridgeServer: string;
  aresBridgeURL: string;
  aresBridgeKey: string;
  aresBridgeUiURL: string;
  analyticsURL: string;
  analyticsSiteId: string;
  analyticsInstance: string;
  officeClientId: string;
  shareURL: string;
  captchaURL: string;
  euloginUrl: string;
  eulogoutUrl: string;
  useAlfrescoAPI: boolean;
}
