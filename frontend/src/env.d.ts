export {};

declare global {
  interface Window {
    env: {
      production?: boolean;
      alfrescoURL?: string;
      circabcURL?: string;
      nodeName?: string;
      showUiSwitch?: boolean;
      environmentType?: string;
      circabcRelease?: string;
      aresBridgeEnabled?: boolean;
      aresBridgeServer?: string;
      aresBridgeURL?: string;
      aresBridgeKey?: string;
      aresBridgeUiURL?: string;
      analyticsURL?: string;
      analyticsSiteId?: string;
      analyticsInstance?: string;
      officeClientId?: string;
      shareURL?: string;
      captchaURL?: string;
      euloginUrl?: string;
      eulogoutUrl?: string;
      useAlfrescoAPI?: boolean;
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      [key: string]: any;
    };
  }
}
