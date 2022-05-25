<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

${formatMessage("mails_common_dear_user", fullName(person))}

<br /><br />

${formatMessage("mail_me_dossier_body_dossier",
		location.name,
		accessUrl(location),
                                titleOrName(interestGroup),
		accessUrl(interestGroup)
                                titleOrName(category),
		accessUrl(category))
	}

<br /><br /><br />

<i><u>${formatMessage("mail_me_dossier_body_link_header")}</u></i>

<br />

<ul>
	<#compress> 
		<#if space?exists>
			<#list space.childrenByXPath[".//*[subtypeOf('app:folderlink')]"] as folderlink>
				<li>${formatMessage("mails_common_href", folderlink.name, accessUrl(folderlink))}</li><br />
			</#list>
			<#list space.childrenByXPath[".//*[subtypeOf('app:filelink')]"] as filelink>
				<li>${formatMessage("mails_common_href", filelink.name, downloadUrl(filelink))}</li><br />
			</#list>
		<#else>
			No Dossier found!
		</#if>
	</#compress> 
</ul>


<br />

${formatMessage("mails_common_signature", appName)}

</#compress>