<#escape x as jsonUtils.encodeJSONString(x)>
{
"userId": <#if user.userId??>"${user.userId}"<#else>""</#if>,
"firstname": <#if user.firstname??>"${user.firstname}"<#else>""</#if>,
"lastname": <#if user.lastname??>"${user.lastname}"<#else>""</#if>,
"email": <#if user.email??>"${user.email}"<#else>""</#if>,
"phone": <#if user.phone??>"${user.phone}"<#else>""</#if>,
"contentFilterLang": <#if user.contentFilterLang??>"${user.contentFilterLang}"<#else>""</#if>,
"uiLang": <#if user.uiLang??>"${user.uiLang}"<#else>""</#if>,
"visibility": <#if user.visibility??>${user.visibility?string}<#else>true</#if>,
"avatar": <#if user.avatar??>"${user.avatar}"<#else>""</#if>,
"defaultAvatar": <#if user.defaultAvatar??>${user.defaultAvatar?string}<#else>false</#if>,
"properties": {
<#if user.properties??>
<#assign keys = user.properties?keys>
<#list keys as k>
"${k}": <#if user.properties[k]??>"${user.properties[k]}"<#else>""</#if>
<#if (k_has_next)>, </#if>
</#list>
</#if>
}
}
</#escape>