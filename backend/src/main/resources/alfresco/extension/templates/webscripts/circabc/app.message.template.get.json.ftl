<#escape x as jsonUtils.encodeJSONString(x)>
{
	"id": ${message.id?c},
	"content": <#if message.content??>"${message.content}"<#else>""</#if>,
	"dateClosure": <#if message.dateClosure??>"${message.dateClosure.toDate()?datetime?iso_utc}"<#else>""</#if>,
	"level": <#if message.level??>"${message.level}"<#else>"info"</#if>,
	"enabled": <#if message.enabled??>${message.enabled?string}<#else>false</#if>,
	"displayTime": <#if message.displayTime??>${message.displayTime?c}<#else>-1</#if>
}
</#escape>