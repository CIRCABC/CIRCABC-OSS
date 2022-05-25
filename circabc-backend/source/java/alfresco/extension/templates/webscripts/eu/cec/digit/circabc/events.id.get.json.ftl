<#escape x as jsonUtils.encodeJSONString(x)>
<#compress>
{
	"appointmentTypeEvent": ${isEvent?string},
	"id": "${appointmentId}",
	"igId": "${igId}",
	"title": "${appointment.title}",
	"audienceStatus": "${appointment.audienceStatus}",
	"dateInfo": {
		"date": "${appointment.date?date("yyyyMMdd")?string["yyyy-MM-dd"]}",
		"startTime": "${appointment.startTime?time("HH:mm:ss.SSS")?string["HH:mm"]}",
		"endTime": "${appointment.endTime?time("HH:mm:ss.SSS")?string["HH:mm"]}"
	},
	"enableNotification": ${appointment.enableNotification?string},
	"eventAbstract": "${appointment.eventAbstract}",
	"invitationMessage": "${appointment.invitationMessage}",
	"language": "${appointment.language}",
	"timezone": "${appointment.timeZoneId}",
	"location": "${appointment.location}",
	"repeatsInfo": {
		"mainOccurence": "${appointment.occurenceRate.mainOccurence}",
		<#if (appointment.occurenceRate.timesOccurence)??>"timesOccurence": "${appointment.occurenceRate.timesOccurence}",</#if>
		<#if (appointment.occurenceRate.everyTimesOccurence)??>"everyTimesOccurence": "${appointment.occurenceRate.everyTimesOccurence}",</#if>
		"everyTime": ${appointment.occurenceRate.every},
		"times": ${appointment.occurenceRate.times}
	},
	
	<#if (appointment.priority)??>"eventPriority": "${appointment.priority}",</#if>
	<#if (appointment.eventType)??>"eventType": "${appointment.eventType}",</#if>
	
	<#if (appointment.availability)??>"meetingPublicAvailability": ${(appointment.availability == "Public")?string},</#if>
	<#if (appointment.organization)??>"meetingOrganisation": "${appointment.organization}",</#if>
	<#if (appointment.agenda)??>"meetingAgenda": "${appointment.agenda}",</#if>
	<#if (appointment.meetingTypeString)??>"meetingType": "${appointment.meetingTypeString}",</#if>
	<#if (appointment.librarySection)??>"meetingLibrarySection": "${appointment.librarySection.id}",</#if>
	
	<#if (appointment.name)??>"contactName": "${appointment.name}",</#if>
	<#if (appointment.phone)??>"contactPhone": "${appointment.phone}",</#if>
	<#if (appointment.email)??>"contactEmail": "${appointment.email}",</#if>
	<#if (appointment.url)??>"contactUrl": "${appointment.url}",</#if>
	
	"attendantsInfo": {
		"audienceStatusOpen": ${(appointment.invitedUsers?size == 0)?string},
		"invitedUsersOrProfiles": [<#if appointment.invitedUsers?size &gt; 0><#list 0..appointment.invitedUsers?size - 1 as index><#if !appointment.invitedUsers[index]?contains("@") && appointment.invitedUsers[index]?length &gt; 0>"${appointment.invitedUsers[index]}"<#if index &lt; appointment.invitedUsers?size - 1 && !appointment.invitedUsers[index + 1]?contains("@")>, </#if></#if></#list></#if>],
		"invitedExternalEmails": [<#if appointment.invitedUsers?size &gt; 0><#list 0..appointment.invitedUsers?size - 1 as index><#if appointment.invitedUsers[index]?contains("@") && appointment.invitedUsers[index]?length &gt; 0>"${appointment.invitedUsers[index]}"<#if index &lt; appointment.invitedUsers?size - 1 && appointment.invitedUsers[index + 1]?contains("@")>, </#if></#if></#list></#if>],
		"audience": [<#assign keys = appointment.audience?keys><#list keys as k>{ "userId": "${k}", "status": "${appointment.audience[k]}" }<#if (k_has_next)>, </#if></#list>]
	}
}
</#compress>
</#escape>