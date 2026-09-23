#!/bin/bash
set -e

# Path to the env file
ENV_FILE="/opt/bitnami/nginx/html/ui/assets/env.js"

# Check if nginx.conf is missing (e.g. because of an empty-dir mount)
if [ ! -f "/opt/bitnami/nginx/conf/nginx.conf" ]; then
    echo "nginx.conf not found in /opt/bitnami/nginx/conf. Restoring from default backup..."
    if [ -d "/opt/bitnami/nginx/conf.default" ]; then
        cp -r /opt/bitnami/nginx/conf.default/* /opt/bitnami/nginx/conf/
        echo "Configuration restored."
    else
        echo "ERROR: Default configuration backup not found at /opt/bitnami/nginx/conf.default"
    fi
fi

echo "Generating env.js from environment variables..."
echo "Context Path: ${CONTEXT_PATH}"

echo "Updating base href in index.html to ${CONTEXT_PATH}/ui/"
sed -i "s|<base href=\"/\">|<base href=\"${CONTEXT_PATH}/ui/\">|g" /opt/bitnami/nginx/html/ui/index.html

mkdir -p $(dirname "$ENV_FILE")

# Construct the env.js file content
cat > "$ENV_FILE" <<EOF
(function(window) {
  window["env"] = window["env"] || {};

  window["env"]["production"] = "${PRODUCTION:-true}";
  window["env"]["baseHref"] = "${CONTEXT_PATH}/ui/";
  window["env"]["serverURL"] = "${CONTEXT_PATH}/alfresco/";
  window["env"]["alfrescoHost"] = "${CONTEXT_PATH}/alfresco";
  window["env"]["alfrescoURL"] = "${CONTEXT_PATH}${ALFRESCO_URL:-/alfresco/service/api}";
  window["env"]["circabcURL"] = "${CONTEXT_PATH}${CIRCABC_URL:-/alfresco/service/circabc}";
  window["env"]["nodeName"] = "${NODE_NAME:-N1}";
  window["env"]["showUiSwitch"] = ${SHOW_UI_SWITCH:-false};
  window["env"]["environmentType"] = "${ENVIRONMENT_TYPE:-acc}";
  window["env"]["circabcRelease"] = "${CIRCABC_RELEASE:-ent}";
  window["env"]["aresBridgeEnabled"] = ${ARES_BRIDGE_ENABLED:-true};
  window["env"]["aresBridgeServer"] = "${ARES_BRIDGE_SERVER:-https://webgate.acceptance.ec.testa.eu}";
  window["env"]["aresBridgeURL"] = "${ARES_BRIDGE_URL:-https://webgate.acceptance.ec.testa.eu/Ares/bridge/services/v1}";
  window["env"]["aresBridgeKey"] = "${ARES_BRIDGE_KEY:-ab7c7293960c4538bc6535fe20aa3757}";
  window["env"]["aresBridgeUiURL"] = "${ARES_BRIDGE_UI_URL:-https://webgate.acceptance.ec.testa.eu/Ares/bridge/ui}";
  window["env"]["analyticsURL"] = "${ANALYTICS_URL:-circabc.acceptance.europa.eu}";
  window["env"]["analyticsSiteId"] = "${ANALYTICS_SITE_ID:-fe2aaaa0-456a-44c0-a2d9-f7ef92d20d5d}";
  window["env"]["analyticsInstance"] = "${ANALYTICS_INSTANCE:-testing}";
  window["env"]["officeClientId"] = "${OFFICE_CLIENT_ID:-32b07f1c-465c-477c-a325-f356ccc127e2}";
  window["env"]["shareURL"] = "${SHARE_URL:-https://circabc.acceptance.europa.eu/share/logincircabc}";
  window["env"]["captchaURL"] = "${CONTEXT_PATH}${CAPTCHA_URL:-/eu-captcha}";
  window["env"]["euloginUrl"] = "${CONTEXT_PATH}${EULOGIN_URL:-/ecas}";
  window["env"]["eulogoutUrl"] = "${CONTEXT_PATH}${EULOGOUT_URL:-/ecas/logout}";
  window["env"]["useAlfrescoAPI"] = ${USE_ALFRESCO_API:-true};

})(this);
EOF

echo "env.js created successfully."

# Execute the CMD passed to the docker container (Nginx)
exec "$@"
