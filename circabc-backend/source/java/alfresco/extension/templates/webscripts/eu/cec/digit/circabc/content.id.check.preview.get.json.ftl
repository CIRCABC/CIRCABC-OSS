<#escape x as jsonUtils.encodeJSONString(x)>
{
	"ready": ${result.ready?string},
	"message": "${result.message}",
	"messageCode": "${result.messageCode}",
	"contentLength": ${result.contentLength?c}
}
</#escape>