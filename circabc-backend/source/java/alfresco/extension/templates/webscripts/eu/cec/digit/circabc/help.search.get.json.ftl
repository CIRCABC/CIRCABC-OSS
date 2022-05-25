<#escape x as jsonUtils.encodeJSONString(x)>
{
	"categories": 
	[
	  <#list search.categories as category>
		<#include "help.category.get.json.ftl" />
		<#if (category_has_next)>,</#if>
	  </#list>
	 ]
	,
	"articles":
	[
	  <#list search.articles as article>
		<#include "help.article.get.json.ftl" />
		<#if (article_has_next)>,</#if>
	  </#list>
	 ] 
	 ,
	"links":
	[
	  <#list search.links as link>
		<#include "help.link.get.json.ftl" />
		<#if (link_has_next)>,</#if>
	  </#list>
	 ] 
}


</#escape>