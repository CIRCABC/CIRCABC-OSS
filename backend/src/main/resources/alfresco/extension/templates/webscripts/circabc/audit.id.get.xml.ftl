<?xml version='1.0' encoding='utf-8'?>
<logResults>
	<#list logResults as logResult>
	<logResult>
		<logDate>${logResult.logDate?string["yyyy-MM-dd HH:mm"]}</logDate> 
		<activityDescription>${logResult.activityDescription?xml}</activityDescription>
		<serviceDescription>${logResult.serviceDescription?xml}</serviceDescription>
		<userId>${logResult.userName?xml}</userId>
		<information><#if logResult.info??>"${logResult.info?xml}"<#else>""</#if></information>
		<path>${logResult.path?xml}</path>
		<success>${(logResult.isOK == 1)?string}</success>
	</logResult>
	</#list>
</logResults>