<%@page import="eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog"%>
<%@page import="eu.cec.digit.circabc.web.wai.app.WaiApplication"%>
<%@page import="org.alfresco.util.Pair"%>
<%@page import="java.util.List"%>
<%@page import="eu.cec.digit.circabc.web.wai.dialog.content.edit.OnlineEditingHelper"%>
<%@page import="eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean"%>
<%@page import="eu.cec.digit.circabc.web.Beans"%>
<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>

var tinyMCELinkList = new Array(
	// Name, URL

<%
	final CircabcNavigationBean bean  = Beans.getWaiNavigator();
	final CreateContentBaseDialog dialog  = (CreateContentBaseDialog) WaiApplication.getDialogManager().getBean();
	final List<Pair<String, String>> links = OnlineEditingHelper.generateLinks(dialog, bean);

	boolean first = true;

	for(final Pair<String, String> pair: links)
	{
		if(!first)
		{
			out.write(",\n");
		}
		else
		{
			first = false;
		}

		out.write("[\"");
		out.write(pair.getFirst());
		out.write("\", \"");
		out.write(pair.getSecond());
		out.write("\"]");
	}
%>
);
