<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailheader.ftl"].nodeRef>


<#-- ------------------------------------- -->
<#-- --------------CONTENT---------------- -->
<#-- ------------------------------------- -->

<tr>
    <td bgcolor="#ffffff" align="center" style="padding: 0 10px 0 10px;">
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td>
            <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td>
                    <table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left">
                      	<h1 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                      		${formatMessage("category_group_delete_request_template_mail_subject")}</h1>
                      </td>
                    </tr>
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
                        ${formatMessage("mails_common_dear_user", fullName(toUserRef))}
                        <br>
                        <br>
                        ${formatMessage("category_group_delete_request_rejection_leaders_i")}
                        (${igName})
                        <br>
                        ${formatMessage("category_group_delete_request_rejection_leaders_ii")}
                        (${categoryName})
                        ${formatMessage("category_group_delete_request_rejection_leaders_iii")}
                        <p>
                        ${formatMessage("category_group_delete_request_rejection_template_mail_reason")}
                        (${reason})
                        </p>
                        </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
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
 
  
<#-- ------------------------------------- -->
<#-- ------------END CONTENT-------------- -->
<#-- ------------------------------------- -->

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailfooter.ftl"].nodeRef>

</#compress>

