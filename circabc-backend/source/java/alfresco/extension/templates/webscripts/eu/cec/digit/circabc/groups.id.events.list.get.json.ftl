<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
		<#list eventItems as eventItem>
			{
			"id": "${eventItem.eventNodeRef.id}",
			"appointmentContainerId": "${eventItem.appointmentContainerId}",
			"igId": "${eventItem.interestGroup}",
			"appointmentType": "${eventItem.eventType}",
			"title": "${eventItem.title}",
			"appointmentDate": "${eventItem.date?string["yyyy-MM-dd"]}",
			"startTime": "${eventItem.startTime?time("HH:mm:ss.SSS")?string["HH:mm"]}",
			"endTime": "${eventItem.endTime?time("HH:mm:ss.SSS")?string["HH:mm"]}",
			"contact": "${eventItem.contact}",
			"meetingStatus": "${eventItem.meetingStatus}",
			"location": "${eventItem.location}",
			"eventAbstract": "${eventItem.description}",
			"occurrenceRate": "${eventItem.occurrenceRate}",
			"timeZone": "${eventItem.timeZone}"
			}<#if (eventItem_has_next)>, </#if>
		</#list>
	],
	"total": ${total?c}
}
</#escape>
