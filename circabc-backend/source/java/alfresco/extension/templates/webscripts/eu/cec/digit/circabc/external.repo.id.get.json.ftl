<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list repos as repo>
    {
    "name":"${repo.name}",
    "registrationDate": "${repo.registrationDate?datetime?iso_utc}"
    }
    <#if (repo_has_next)>,</#if>
</#list>
]
</#escape>