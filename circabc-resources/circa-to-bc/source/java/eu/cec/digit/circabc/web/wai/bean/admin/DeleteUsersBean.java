package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

public class DeleteUsersBean extends BaseWaiDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4942879585650008363L;
	
	private static final Log logger = LogFactory.getLog(DeleteUsersBean.class);
	
	
	private List<String> selectedItems; 
	private List<SelectItem> selectItems;

	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	private String ecasUserName; 
	
	public void init(final Map<String, String> parameters)
    {
      
		super.init(parameters);
		userName = "";
		firstName = "";
		lastName = "";
		email ="";
		ecasUserName="";
		selectedItems = Collections.emptyList();
		selectItems = Collections.emptyList();
	    if(!hasRight())
	    {
	        logger.warn("Security violation!!! User (" + getNavigator().getCurrentUserName() + ") tries to access to the copy user page !");
	
	        throw new IllegalStateException("You have not enough rights to perform this operation. ");
	    }
    }
     public boolean hasRight()
    {
        return getNavigator().getCurrentUser().isAdmin();
    }


	public String getPageIconAltText()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getBrowserTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	 public void reset(ActionEvent event)
	 {
		 this.init(null);
	 }
	@Override
	protected String finishImpl(FacesContext context, String outcome) throws Throwable
	{
		getUserService().deleteUsers(selectedItems );
		return "finish";
	}

	
	public void search()
	{
		selectedItems = new ArrayList<String>();
		selectItems = new ArrayList<SelectItem>();
		 
		String luceneQuery = buildLuceneQuery();
		ResultSet resultSet = null;
		try {
			resultSet = executeLuceneQuery(luceneQuery);
			for (final ResultSetRow row : resultSet) {
				if (getNodeService().exists(row.getNodeRef())) {
					String userName = (String) getNodeService().getProperty(
							row.getNodeRef(), ContentModel.PROP_USERNAME);
					if (!userName.equals("guest") && !userName.equals("admin") && !userName.equals("<USERNAME>") )
					{
						selectedItems.add(userName);
						selectItems.add(new SelectItem(userName, userName));
					}
				}

			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}

	}
	
	private ResultSet executeLuceneQuery(final String query)
	{
		final SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery(query);
		sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
		return  getSearchService().query(sp);
	}
	
	private String buildLuceneQuery()
	{

		final StringBuffer query = new StringBuffer();

		query.append("TYPE:\"cm:person\"");
		if (!userName.isEmpty())
		{
			query.append(" AND @cm\\:userName:\"");
			query.append(userName);
			query.append("\"");
		}
		if (!firstName.isEmpty())
		{
			query.append(" AND @cm\\:firstName:\"");
			query.append(firstName);
			query.append("\"");
		}
		if (!lastName.isEmpty())
		{
			query.append(" AND @cm\\:lastName:\"");
			query.append(lastName);
			query.append("\"");
		}
		if (!email.isEmpty())
		{
			query.append(" AND @cm\\:email:\"");
			query.append(email);
			query.append("\"");
		}
		
		if (!ecasUserName.isEmpty())
		{
			query.append(" AND @cu\\:ecasUserName:\"");
			query.append(ecasUserName);
			query.append("\"");
		}
				
		return query.toString();
	}

	public List<String> getSelectedItems() {
		return selectedItems;
	}
	public void setSelectedItems(List<String> selectedItems) {
		this.selectedItems = selectedItems;
	}
	public List<SelectItem> getSelectItems() {
		return selectItems;
	}
	public void setSelectItems(List<SelectItem> selectItems) {
		this.selectItems = selectItems;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEcasUserName() {
		return ecasUserName;
	}
	public void setEcasUserName(String ecasUserName) {
		this.ecasUserName = ecasUserName;
	}
	
	
	

}
