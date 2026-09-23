#!/bin/bash
set -e

CONFIG_PATH="/opt/bitnami/nginx/html/ui/config.json"

# Mapping: ENV_VAR_NAME -> JSON_KEY TYPE(s=string, b=boolean)
declare -A MAPPING=(
  ["CIRCABC_PRODUCTION"]="production b"
  ["CIRCABC_ALFRESCO_URL"]="alfrescoURL s"
  ["CIRCABC_CIRCABC_URL"]="circabcURL s"
  ["CIRCABC_SERVER_URL"]="serverURL s"
  ["CIRCABC_ALFRESCO_HOST"]="alfrescoHost s"
  ["CIRCABC_BASE_HREF"]="baseHref s"
  ["CIRCABC_NODE_NAME"]="nodeName s"
  ["CIRCABC_SHOW_UI_SWITCH"]="showUiSwitch b"
  ["CIRCABC_ENVIRONMENT_TYPE"]="environmentType s"
  ["CIRCABC_CIRCABC_RELEASE"]="circabcRelease s"
  ["CIRCABC_ARES_BRIDGE_ENABLED"]="aresBridgeEnabled b"
  ["CIRCABC_ARES_BRIDGE_SERVER"]="aresBridgeServer s"
  ["CIRCABC_ARES_BRIDGE_URL"]="aresBridgeURL s"
  ["CIRCABC_ARES_BRIDGE_KEY"]="aresBridgeKey s"
  ["CIRCABC_ARES_BRIDGE_UI_URL"]="aresBridgeUiURL s"
  ["CIRCABC_ANALYTICS_URL"]="analyticsURL s"
  ["CIRCABC_ANALYTICS_SITE_ID"]="analyticsSiteId s"
  ["CIRCABC_ANALYTICS_INSTANCE"]="analyticsInstance s"
  ["CIRCABC_OFFICE_CLIENT_ID"]="officeClientId s"
  ["CIRCABC_SHARE_URL"]="shareURL s"
  ["CIRCABC_CAPTCHA_URL"]="captchaURL s"
  ["CIRCABC_EULOGIN_URL"]="euloginUrl s"
  ["CIRCABC_EULOGOUT_URL"]="eulogoutUrl s"
  ["CIRCABC_USE_ALFRESCO_API"]="useAlfrescoAPI b"
)

JSON="{"
FIRST=true

for ENV_VAR in "${!MAPPING[@]}"; do
  VAL="${!ENV_VAR}"
  if [ -n "$VAL" ]; then
    read -r KEY TYPE <<< "${MAPPING[$ENV_VAR]}"
    $FIRST || JSON+=","
    FIRST=false
    if [ "$TYPE" = "b" ]; then
      # Normalize boolean
      if [ "$VAL" = "true" ]; then
        JSON+="\"$KEY\":true"
      else
        JSON+="\"$KEY\":false"
      fi
    else
      JSON+="\"$KEY\":\"$VAL\""
    fi
  fi
done

JSON+="}"

echo "$JSON" > "$CONFIG_PATH"
echo "Generated runtime config at $CONFIG_PATH"

exec "$@"
