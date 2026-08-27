<#escape x as jsonUtils.encodeJSONString(x)>
{
	"granted": ${result.granted?string}<#if result.readOnly??>,
	"readOnly": ${result.readOnly?string}</#if><#if result.locked??>,
	"locked": ${result.locked?string}</#if>
}
</#escape>
