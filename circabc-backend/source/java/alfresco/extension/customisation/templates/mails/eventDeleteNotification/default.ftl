<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailheader.ftl"].nodeRef>


<#-- ------------------------------------- -->
<#-- --------------CONTENT---------------- -->
<#-- ------------------------------------- -->


<tr>
    <td bgcolor="#ffffff" align="center" style="padding: 0 10px 0 10px;">
      <!--[if (gte mso 9)|(IE)]>
      <table role="presentation" align="center" border="0" cellspacing="0" cellpadding="0" width="600">
      <tr>
      <td align="center" valign="top" width="600">
      <![endif]-->
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left">
                      	<h1 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                      		${formatMessage("email_label_event_agenda")}</h1>
                      </td>
                    </tr>
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;"> 
						${formatMessage("mails_common_dear_genereric")}
						<br>
                        <br>
							${formatMessage("delete_event_audience_notification_body",
								appointment.title
								agendaAccessUrl(eventRef))
							}

                    </tr>
                  </table></td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="center"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="right">
                      <table role="presentation" border="0" cellspacing="0" cellpadding="0" class="mobile-btn-container">
                          <tr>
                            <td align="center" style="border-radius: 3px;" bgcolor="#8dcb6c">
                            	<a href="${agendaAccessUrl(eventRef)}" title="Read more" target="_blank" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 3px; padding: 15px 25px; border: 1px solid #8dcb6c; display: inline-block;" class="mobile-btn">
                            		${formatMessage("email_label_visit")} &#187;</a>
                            	</td>
                          </tr>
                        </table></td>
                    </tr>
                  </table></td>
              </tr>
            </table></td>
        </tr>
      </table>
      <!--[if (gte mso 9)|(IE)]>
      </td>
      </tr>
      </table>
      <![endif]-->
    </td>
  </tr>

	  
	  
  <tr>
    <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
  </tr>
  
  
  <tr>
    <td bgcolor="#eff5f1" align="center" style="padding: 0 10px 0 10px;">
      <!--[if (gte mso 9)|(IE)]>
      <table role="presentation" align="center" border="0" cellspacing="0" cellpadding="0" width="600">
      <tr>
      <td align="center" valign="top" width="600">
      <![endif]-->
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td bgcolor="#eff5f1">
			<table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left">
                	<h2 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                		${formatMessage("create_event_audience_table_summary")}
					</h2>
				</td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
				
			             
		             <b>${formatMessage("title")}: </b>${appointment.title}<br/>
		             <b>${formatMessage("event_create_event_wizard_step1_type")}: </b>${formatMessage(concatStr('event_create_event_wizard_step1_type_', appointment.eventType)?lower_case)}<br/>
		             <b>${formatMessage("event_create_meetings_wizard_step1_date")}: </b>${appointment.startDateAsDate?string('dd-MM-yyyy')} : ${appointment.startTimeAsDate?string('HH:mm')} [${appointment.timeZoneId}]<br/>
		             <b>${formatMessage("event_create_meetings_wizard_step1_end_time")}: </b> ${appointment.endTimeAsDate?string('dd-MM-yyyy')} : ${appointment.endTimeAsDate?string('HH:mm')} [${appointment.timeZoneId}]<br/>
		             <b>${formatMessage("event_create_meetings_wizard_step1_location")}: </b>${appointment.location}<br/>
		             <b>${formatMessage("event_create_meetings_wizard_step3_title")}: </b>${formatMessage(concatStr('event_create_meetings_wizard_step1_audience_status_', appointment.audienceStatus)?lower_case)}<br/>
					 
					 <#if concatStr(appointment.audienceStatus) = 'Closed'>
				   		<br />${formatMessage('create_event_audience_audience_closed')}
				   		<br />${formatMessage('create_event_audience_audience_users')}:<br />
						<#list appointment.invitedUsers as user>
			                ${fullName(user, !appointment.useBCC)}<#if user_has_next>,&nbsp;</#if>
						</#list>
			        </#if>
					 
				</td>
              </tr>
              
              <#-- IF THE EVENT HAS A DESCRIPTION -->
              <#if appointment.eventAbstract != "">
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left">
                	<h2 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                		${formatMessage("event_create_meetings_wizard_step1_abstract")}
					</h2>
				</td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
				
			             
		             ${appointment.eventAbstract}
					 
				</td>
              </tr>
              </#if>
              
              <#-- IF THE EVENT HAS AN INVITATION -->
              <#if appointment.invitationMessage != "">
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left">
                	<h2 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                		${formatMessage("event_create_meetings_wizard_step1_message")}
					</h2>
				</td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
				
			             
		             ${appointment.invitationMessage}
					 
				</td>
              </tr>
              </#if>
              
              
            </table>
		  </td>
        </tr>
		 <tr>
			<td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
		  </tr>
      </table>
      <!--[if (gte mso 9)|(IE)]>
      </td>
      </tr>
      </table>
      <![endif]-->
    </td>
  </tr>


<#-- ------------------------------------- -->
<#-- ------------END CONTENT-------------- -->
<#-- ------------------------------------- -->

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailfooter.ftl"].nodeRef>

</#compress>

