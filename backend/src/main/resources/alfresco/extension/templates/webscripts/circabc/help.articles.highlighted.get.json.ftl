<#escape x as jsonUtils.encodeJSONString(x)>

[
	<#list articles as article>
		<#include "help.article.get.json.ftl" />
	<#if (article_has_next)>,</#if>
  </#list>
]


</#escape>