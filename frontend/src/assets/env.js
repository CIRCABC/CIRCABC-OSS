// biome-ignore lint/complexity/useArrowFunction: IIFE pattern for runtime env injection
(function(window) {
  window["env"] = window["env"] || {};

  // Environment variables
  window["env"]["production"] = true;
  window["env"]["alfrescoURL"] = '../alfresco/service/api';
  window["env"]["circabcURL"] = '../alfresco/service/circabc';
  window["env"]["environmentType"] = 'acc';
  window["env"]["circabcRelease"] = 'ent';
  window["env"]["aresBridgeEnabled"] = true;
  window["env"]["aresBridgeServer"] = 'https://webgate.acceptance.ec.testa.eu';
  window["env"]["aresBridgeURL"] = 'https://webgate.acceptance.ec.testa.eu/Ares/bridge/services/v1';
  window["env"]["aresBridgeUiURL"] = 'https://webgate.acceptance.ec.testa.eu/Ares/bridge/ui';
  window["env"]["aresBridgeKey"] = 'ab7c7293960c4538bc6535fe20aa3757';
  window["env"]["analyticsURL"] = 'circabc.acceptance.europa.eu';
  window["env"]["analyticsSiteId"] = 'fe2aaaa0-456a-44c0-a2d9-f7ef92d20d5d';
  window["env"]["analyticsInstance"] = 'testing';
  window["env"]["officeClientId"] = '32b07f1c-465c-477c-a325-f356ccc127e2';
  window["env"]["shareURL"] = 'https://circabc.acceptance.europa.eu/share/logincircabc';
  window["env"]["captchaURL"] = 'https://circabc.acceptance.europa.eu/EuCaptcha-2.2.8';
  window["env"]["useAlfrescoAPI"] = true;
  window["env"]["aiAgentUrl"] = "/circabc-caas/ai-agent";
  
})(this);
