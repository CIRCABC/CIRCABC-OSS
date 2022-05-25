<#escape x as jsonUtils.encodeJSONString(x)>
{
	"pivot":
		<#assign n = translationSet.pivot>
		<#include "node.get.json.ftl">,
	"translations":
		[
		<#if translationSet.translations??>
			<#list translationSet.translations as n>
				<#include "node.get.json.ftl">
				<#if (n_has_next)>,</#if>
			</#list>
		</#if>
		]
}
</#escape>