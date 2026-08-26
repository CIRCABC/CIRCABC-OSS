<#escape x as jsonUtils.encodeJSONString(x)>
{
	"locked": ${lockInfo.locked?string},
	"message": <#if lockInfo.message??>"${lockInfo.message}"<#else>null</#if>,
	"readOnly": <#if lockInfo.readOnly??>${lockInfo.readOnly?string}<#else>false</#if>,
	"lockedBy": <#if lockInfo.lockedBy??>"${lockInfo.lockedBy}"<#else>null</#if>,
	"lockedDate": <#if lockInfo.lockedDate??>"${lockInfo.lockedDate?datetime?iso_utc}"<#else>null</#if>
}
</#escape>
