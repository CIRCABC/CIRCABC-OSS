<#escape x as jsonUtils.encodeJSONString(x)>
{
  "syncEnabled": <#if syncStatus.syncEnabled>true<#else>false</#if>,
  "readFromDatabase": <#if syncStatus.readFromDatabase>true<#else>false</#if>,
  "users": {
    "alfrescoCount": ${syncStatus.users.alfrescoCount?c},
    "cbcCount": ${syncStatus.users.cbcCount?c},
    "delta": ${syncStatus.users.delta?c}
  },
  "interestGroups": {
    "alfrescoCount": ${syncStatus.interestGroups.alfrescoCount?c},
    "cbcCount": ${syncStatus.interestGroups.cbcCount?c},
    "delta": <#if syncStatus.interestGroups.delta?is_number>${syncStatus.interestGroups.delta?c}<#else>"${syncStatus.interestGroups.delta}"</#if>
  }
}
</#escape>
