<#escape x as jsonUtils.encodeJSONString(x)>
<#if statistics??>
[
	<#list statistics as s>
	{
		"name": "${s.dataName}", 
		"value": "${s.dataValue}"
	}<#if (s_has_next)>,</#if>
	</#list>
] 
<#elseif timeline??>
[
	<#list timeline as t>
	{
		"monthActivity": "${t.monthActivity?string["MM yyyy"]}", 
		"service": "${t.service}",
		"activity": "${t.activity}",
		<#if (t.actionId)??>"actionId": ${t.actionId},</#if>
		"actionNumber": ${t.actionNumber}
	}<#if (t_has_next)>,</#if>
	</#list>
] 
<#elseif structure??>
{
	"structure": "${structure}"
}
</#if>
</#escape>