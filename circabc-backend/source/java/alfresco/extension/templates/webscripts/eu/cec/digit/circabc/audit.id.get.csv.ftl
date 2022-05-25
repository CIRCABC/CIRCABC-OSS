"logDate","activityDescription","serviceDescription","userId","path","information","success"
<#list logResults as logResult>
"${logResult.logDate?string["yyyy-MM-dd HH:mm"]}", "${logResult.activityDescription}","${logResult.serviceDescription}","${logResult.userName}","${logResult.path}",<#if logResult.info??>"${logResult.info?replace("\n"," ")?replace("\"","\"\"")}"<#else>" "</#if>,${(logResult.isOK == 1)?string}
</#list>
