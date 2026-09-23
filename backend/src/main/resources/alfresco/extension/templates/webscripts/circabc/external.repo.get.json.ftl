<#escape x as jsonUtils.encodeJSONString(x)>
 [
	<#list repos as repo>
        "${repo}"
        <#if (repo_has_next)>,</#if>
    </#list>
]
</#escape>