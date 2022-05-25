export interface Environment {
  production: boolean;
  alfrescoURL: string;
  circabcURL: string;
  serverURL: string;
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
  circabcRelease: 'ent' | 'oss' | 'echa';
  aresBridgeEnabled: boolean;
  aresBridgeServer: string;
  aresBridgeURL: string;
  aresBridgeKey: string;
  aresBridgeUiURL: string;
  analyticsURL: string;
  analyticsSiteId: number;
  officeClientId: string;
  shareURL: string;
}
