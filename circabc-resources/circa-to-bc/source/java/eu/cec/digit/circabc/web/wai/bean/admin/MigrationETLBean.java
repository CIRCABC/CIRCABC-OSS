/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.naming.LimitExceededException;

import org.alfresco.util.Pair;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.journal.etl.ETLReport;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUser;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUserComparator;
import eu.cec.digit.circabc.migration.journal.etl.TransformationElement;
import eu.cec.digit.circabc.migration.journal.etl.TransformationElementComparator;
import eu.cec.digit.circabc.service.migration.ETLException;
import eu.cec.digit.circabc.service.migration.ETLService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

/**
 * Bean that back the importation file.
 *
 * @author Pignot Yanick
 */
public class MigrationETLBean extends BaseWaiDialog
{
	private static final String PARAM_ANCHOR = "anchor";
	private static final String SEARCH_ERROR = "importation_etl_dialog_action_search_error";
	private static final String SEARCH_BAD_PARAM = "importation_etl_dialog_action_search_bad_param";
	private static final String SEARCH_TOO_MANY_RESULTS = "importation_etl_dialog_action_search_too_many_results";
	private static final String SEARCH_ERROR_UNEXPECTED = "importation_etl_dialog_action_search_unexpected_error";
	private static final String SEARCH_RESULT = "importation_etl_dialog_action_search_result";
	private static final String PARAM_USERID = "userid";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_MONIKER = "moniker";
	private static final String PARAM_CN = "cn";


	private static final long serialVersionUID = 8511113203603141478L;

	private static final Log logger = LogFactory.getLog(MigrationETLBean.class);

	private static final String MAKE_VALID_SUCCESS = "importation_etl_dialog_action_make_valid_success";
	private static final String MAKE_VALID_ERROR = "importation_etl_dialog_action_make_valid_error";
	private static final String MAKE_INVALID_SUCCESS = "importation_etl_dialog_action_make_pathologic_success";
	private static final String SELECT_FOR_EDIT_SUCCESS = "importation_etl_dialog_action_select_edit_success";
	private static final String ITERATIONS_ERROR = "importation_dialog_errors_get_iterations";
	private static final String ERROR_NO_SELECT = "importation_etl_dialog_error_no_select";

    private transient ETLService etlService;

    private String message ;
    private String selectedIteration;
    private String newestIteration;
    private ETLReport report;
    private WebPathologicUser userToEdit;
    private transient DataModel validUserDataModel;
    private transient DataModel invalidUserDataModel;
    private String searchForUid;
    private String searchForEmail;
    private String searchForMoniker;
    private String searchForCn;
    private String lastVisitedAnchor;
    private int anchorCounter;

    public void reset(ActionEvent event)
    {
    	this.init(null);
    }

    @Override
    public void init(final Map<String, String> parameters)
    {
        super.init(parameters);
        selectedIteration = null;
        newestIteration = null;
        message = null;
        report = null;
        validUserDataModel = null;
        invalidUserDataModel = null;
        userToEdit = null;
        searchForUid = null;
        searchForEmail = null;
        searchForCn = null;
        searchForMoniker = null;
        validUserDataModel = null;
        invalidUserDataModel = null;
        lastVisitedAnchor = null;
        anchorCounter = 0;
        if(!hasRight())
        {
            logger.warn("Security violation!!! User (" + getNavigator().getCurrentUserName() + ") tries to access to the importation page !");

            throw new IllegalStateException("You have not enough rights to perform this operation. ");
        }
    }

    public boolean hasRight()
    {
        return getNavigator().getCurrentUser().isAdmin();
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception
    {
    	if(this.selectedIteration == null)
		{
			throw new IllegalArgumentException(translate(ERROR_NO_SELECT));
		}

    	if(report == null)
    	{
    		try
			{
    			report = getEtlService().proposeEtl(this.selectedIteration);
			}
    		catch (Exception e)
			{
    			if(logger.isErrorEnabled()) {
            		logger.error("An Error occur", e); 
    			}

				Utils.addErrorMessage("An expected error occured: " + e.getMessage() + ". With cause: " + (e.getCause() == null ? "" : e.getCause().getMessage()));
			}

    		isFinished = false;
    		return "next";
    	}
    	else if(message == null)
    	{
    		try
    		{
    			getEtlService().applyEtl(report);

        		message = "The transformation of the iteration " + getSelectedIteration() + " successfully terminated. " +
    			"<br /><br /><b>The valid file (ready to be imported):</b>" +
    			WebMigrationHelper.computeHref(report.getValidImportRoot(), getNodeService(), getPermissionService()) +
    			"<br /><br /><b>The residual file (must be transformed yet):</b>" +
        		WebMigrationHelper.computeHref(report.getResidualImportRoot(), getNodeService(), getPermissionService());

        		report = null;
    		}
    		catch(Exception e)
    		{
    			if(logger.isErrorEnabled()) {
            		logger.error("An Error occur", e); 
    			}
    			Utils.addErrorMessage("An expected error occured: " + e.getMessage() + ". With cause: " + (e.getCause() == null ? "" : e.getCause().getMessage()));
    		}

    		isFinished = false;
    		return "next";
    	}
    	else
    	{
    		return outcome;
    	}
    }

    public boolean isFirstStep()
    {
    	return selectedIteration == null ;
    }

    public boolean isSecondStep()
    {
    	return isFirstStep() == false && getMessage() == null;
    }

    public boolean isThirdStep()
    {
    	return isFirstStep() == false && getMessage() != null;
    }

    public DataModel getIterationsDataModel()
    {
    	//construct the data model
		final DataModel dataModel = new ListDataModel();
    	try
    	{
    		final List<MigrationIteration> origIterations = getEtlService().getIterations(false);
			final List<WebIteration> iterations = new ArrayList<WebIteration>(origIterations.size());
			for(MigrationIteration it : origIterations)
			{
				iterations.add(new WebIteration(it));
			}

			dataModel.setWrappedData(iterations);

			 if(iterations.size() > 0)
			 {
		        newestIteration = iterations.get(0).getIdentifier();
		     }

		}
    	catch(Exception e)
    	{
    		Utils.addErrorMessage(translate(ITERATIONS_ERROR), e);
    		dataModel.setWrappedData(Collections.<MigrationIteration>emptyList());
    	}
    	return dataModel;
    }


    public DataModel getValidUserDataModel()
    {
    	if(report != null && validUserDataModel == null)
    	{
    		//construct the data model
            validUserDataModel = new ListDataModel();
            final List<TransformationElement> transformationElements = report.getTransformationElements();
            Collections.sort(transformationElements, new TransformationElementComparator());
    		validUserDataModel.setWrappedData(transformationElements);
    	}
        return validUserDataModel;
    }
    public DataModel getInvalidUserDataModel()
    {
    	if(report != null && invalidUserDataModel == null)
    	{
    		//construct the data model
            invalidUserDataModel = new ListDataModel();
            final List<PathologicUser> pathologicUsers = report.getPathologicUsers();
            final List<WebPathologicUser> webPathologicUsers = new ArrayList<WebPathologicUser>(pathologicUsers.size());
            int pos = 0;
            for (final PathologicUser user : pathologicUsers)
    		{
            	webPathologicUsers.add(new WebPathologicUser(user, pos++));
    		}
            Collections.sort(webPathologicUsers, new PathologicUserComparator());
    		invalidUserDataModel.setWrappedData(webPathologicUsers);
    	}

    	anchorCounter = 0;

        return invalidUserDataModel;
    }

    /**
	 * Action handler called when the 'Make valid' action is performed in the model
	 */
	@SuppressWarnings("unchecked")
	public void makeValid(final ActionEvent event)
	{
		final WebPathologicUser wrapper = (WebPathologicUser) this.invalidUserDataModel.getRowData();
		setAnchor(event);

		if (wrapper != null)
		{
			final String selectedUser = wrapper.getSelectedUser();
			final Serializable selectedUserCircaId = wrapper.getPerson().getUserId().getValue();
			if(selectedUser == null || wrapper.getPropositions().containsKey(selectedUser) == false)
			{
				Utils.addErrorMessage(translate(MAKE_VALID_ERROR));
			}
			else
			{
				report.makeValide(wrapper, selectedUser);

				((List<WebPathologicUser>) invalidUserDataModel.getWrappedData()).remove(wrapper);

				if(userToEdit != null && selectedUserCircaId.equals(userToEdit.getPerson().getUserId().getValue()))
				{
					userToEdit = null;
				}

				Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MAKE_VALID_SUCCESS, wrapper.getPerson().getUserId().getValue()));
			}
		}
	}

	/**
	 * Action handler called when the 'Make pathologic' action is performed in the model
	 */
	@SuppressWarnings("unchecked")
	public void makePathologic(final ActionEvent event)
	{
		final TransformationElement wrapper = (TransformationElement) this.validUserDataModel.getRowData();
		if (wrapper != null)
		{
			final PathologicUser pathologicUser = report.makePathologic(wrapper);

			final List<WebPathologicUser> wrappedData = (List<WebPathologicUser>) invalidUserDataModel.getWrappedData();
			wrappedData.add(new WebPathologicUser(pathologicUser, wrappedData.size()));

			Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MAKE_INVALID_SUCCESS, wrapper.getValidUserId()));
		}
	}

	/**
	 * Action handler called when the 'Select for editing' action is performed in the model
	 */
	public void selectForEditing(final ActionEvent event)
	{
		final WebPathologicUser wrapper = (WebPathologicUser) this.invalidUserDataModel.getRowData();
		setAnchor(event);
		if (wrapper != null)
		{
			// copy it ....
			userToEdit = new WebPathologicUser(wrapper, wrapper.getTableIndex());
			userToEdit.setSelectedUser(wrapper.getSelectedUser());
			Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(SELECT_FOR_EDIT_SUCCESS, wrapper.getPerson().getUserId().getValue()));
		}
		else
		{
			userToEdit = null;
		}
	}

	/**
	 * Action handler called when the 'Update Selection' action is performed in the model
	 */
	public void updateSelection(final ActionEvent event)
	{
		final Map<String, String> parameters = buildParameterMap(event);
		final String userid = parameters.get(PARAM_USERID);

		searchForEmail = null;
		searchForUid = null;
		searchForMoniker = null;
		searchForCn = null;

		userToEdit.setSelectedUser(userid);
	}

	/**
	 * Action handler called when the 'Remove Selection' action is performed in the model
	 */
	@SuppressWarnings("unchecked")
	public void removeSelection(final ActionEvent event)
	{
		final Map<String, String> parameters = buildParameterMap(event);
		final String userid = parameters.get(PARAM_USERID);

		if(userid != null)
		{
			final List<PathologicUser> pathologicUsers = report.getPathologicUsers();

			PathologicUser originalOne = null;

			for(final PathologicUser wrapped: pathologicUsers)
			{
				if(wrapped.getPerson().getUserId().equals(userToEdit.getPerson().getUserId()))
				{
					originalOne = wrapped;
					break;
				}
			}

			originalOne.resetPropositions(userToEdit.getPropositions());

			report.makeValide(originalOne, userid);

			Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MAKE_VALID_SUCCESS, originalOne.getPerson().getUserId().getValue()));
		}

		this.userToEdit = null;
		this.searchForUid = null;
		this.searchForMoniker = null;
		this.searchForEmail = null;
		this.searchForCn = null;
		this.invalidUserDataModel = null;
		this.validUserDataModel = null;
	}

	/**
	 * Action handler called when the 'Update Selection' action is performed in the model
	 */
	public void searchEcas(final ActionEvent event)
	{
		final Map<String, String> parameters = buildParameterMap(event);

		try
		{
			getEtlService().proposeUsers(userToEdit,
					getValueIfTrue(parameters, PARAM_USERID, searchForUid),
					getValueIfTrue(parameters, PARAM_MONIKER, searchForMoniker),
					getValueIfTrue(parameters, PARAM_EMAIL, searchForEmail),
					getValueIfTrue(parameters, PARAM_CN, searchForCn));

			if(userToEdit.getSelectedUserData() == null)
			{
				// the old selected user is not longer in the propositions
				userToEdit.setSelectedUser(null);
			}

			final Pair<String, String> queryData = getQueryData(parameters, searchForUid, searchForMoniker, searchForEmail, searchForCn);

			Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(SEARCH_RESULT, queryData.getSecond(), queryData.getFirst(), userToEdit.getPropositions().size()));
		}
		catch (final ETLException e)
		{
			Utils.addErrorMessage(translate(SEARCH_ERROR, e.getMessage()), e);
		}
		catch (final IllegalArgumentException e)
		{
			Utils.addErrorMessage(e.getMessage());
		}
		catch (final Exception e)
		{
			if(e instanceof LimitExceededException || (e.getCause() != null && e.getCause() instanceof LimitExceededException))
			{
				Utils.addErrorMessage(translate(SEARCH_TOO_MANY_RESULTS), e);
			}
			else
			{
				Utils.addErrorMessage(translate(SEARCH_ERROR_UNEXPECTED, e.getMessage()), e);
			}
		}
	}

	private void setAnchor(final ActionEvent event)
	{
		final UIComponent component = event.getComponent();
		if (component instanceof UIActionLink)
		{
			final Map<String, String> params = ((UIActionLink)component).getParameterMap();

			if(params != null && params.containsKey(PARAM_ANCHOR))
			{
				this.lastVisitedAnchor = "#" + params.get(PARAM_ANCHOR);
			}
		}
	}


	private Pair<String, String> getQueryData(final Map<String, String> parameters, final String uid, final String moniker, final String email, final String cn)
	{
		if(getValueIfTrue(parameters, PARAM_USERID, uid) != null)
		{
			return new Pair<String, String>(PARAM_USERID, uid);
		}
		else if(getValueIfTrue(parameters, PARAM_EMAIL, email) != null)
		{
			return new Pair<String, String>(PARAM_EMAIL, email);
		}
		else if(getValueIfTrue(parameters, PARAM_MONIKER, moniker) != null)
		{
			return new Pair<String, String>(PARAM_MONIKER, moniker);
		}
		else if(getValueIfTrue(parameters, PARAM_CN, cn) != null)
		{
			return new Pair<String, String>(PARAM_CN, cn);
		}

		else
		{
			return new Pair<String, String>("N/A", "Null");
		}
	}

	private String getValueIfTrue(final Map<String, String> parameters, final String paramName, final String value)
	{
		if(parameters != null && parameters.containsKey(paramName) && "true".equals(parameters.get(paramName)))
		{
			if(value == null || value.trim().length() < 1)
			{
				throw new IllegalArgumentException(translate(SEARCH_BAD_PARAM, paramName));
			}
			else
			{
				return value;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * @param event
	 * @return
	 */
	private Map<String, String> buildParameterMap(final ActionEvent event)
	{
		final List childs;
		if(event.getComponent() instanceof HtmlCommandButton)
		{
			final HtmlCommandButton button = (HtmlCommandButton) event.getComponent();
			childs = button.getChildren();
		}
		else
		{
			final UIActionLink command = (UIActionLink) event.getComponent();
			childs = command.getChildren();
		}

		UIParameter parameter;
		final Map<String, String> parameters = new HashMap<String, String>();

		for(final Object child : childs)
		{
			if(child instanceof UIParameter)
			{
				parameter = (UIParameter) child;
				parameters.put(
						parameter.getName(),
						parameter.getValue().toString());
			}
		}
		return parameters;
	}

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome)
    {
        this.parameters = null;
        return outcome;
    }

    @Override
    public String cancel()
    {
        init(null);
        report = null;
        return super.cancel();
    }

    public String retry()
    {
        init(null);
        return null;
    }

    /**
     * @param etlService the etlService to set
     */
    public final void setEtlService(ETLService etlService)
    {
        this.etlService = etlService;
    }

    /**
     * @return the etlService
     */
    public final ETLService getEtlService()
    {
        if(etlService == null)
        {
            etlService = (ETLService) Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getService(ManageMigrationBaseBean.IMPORT_ETL_SERVICE);
        }
        return etlService;
    }


    /**
     * @return the selectedIteration
     */
    public final String getSelectedIteration()
    {
        return selectedIteration != null ? selectedIteration : newestIteration;
    }


    /**
     * @param selectedIteration the selectedIteration to set
     */
    public final void setSelectedIteration(String selectedIteration)
    {
    	if(selectedIteration != null)
        {
    		this.selectedIteration = selectedIteration;
        }
    }


    public String getBrowserTitle()
    {
        return "";
    }


    public String getPageIconAltText()
    {
        return "";
    }

	/**
	 * @return the pathologicUsers
	 */
	public final List<PathologicUser> getPathologicUsers()
	{
		return report.getPathologicUsers();
	}

	/**
	 * @return the transformationElements
	 */
	public final List<TransformationElement> getTransformationElements()
	{
		return report.getTransformationElements();
	}

	/**
	 * @return the message
	 */
	public final String getMessage()
	{
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public final void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * @return the userToEdit
	 */
	public final WebPathologicUser getUserToEdit()
	{
		return userToEdit;
	}

	/**
	 * @return the searchForEmail
	 */
	public final String getSearchForEmail()
	{
		if(searchForEmail == null)
		{
			final User selectedUser = userToEdit.getSelectedUserData();
			if(selectedUser != null)
			{
				searchForEmail = selectedUser.getEmail();
			}
		}
		return searchForEmail;
	}

	/**
	 * @param searchForEmail the searchForEmail to set
	 */
	public final void setSearchForEmail(String searchForEmail)
	{
		this.searchForEmail = searchForEmail;
	}

	/**
	 * @return the searchForMoniker
	 */
	public final String getSearchForMoniker()
	{
		if(searchForMoniker == null)
		{
			final User selectedUser = userToEdit.getSelectedUserData();
			if(selectedUser != null)
			{
				searchForMoniker = selectedUser.getMoniker();
			}
		}
		return searchForMoniker;
	}

	/**
	 * @param searchForMoniker the searchForMoniker to set
	 */
	public final void setSearchForMoniker(String searchForMoniker)
	{
		this.searchForMoniker = searchForMoniker;
	}

	/**
	 * @return the searchForUid
	 */
	public final String getSearchForUid()
	{
		if(searchForUid == null)
		{
			searchForUid = userToEdit.getSelectedUser();
		}
		return searchForUid;
	}

	/**
	 * @param searchForUid the searchForUid to set
	 */
	public final void setSearchForUid(String searchForUid)
	{
		this.searchForUid = searchForUid;
	}

	/**
	 * @return the searchForCn
	 */
	public final String getSearchForCn()
	{
		return searchForCn;
	}

	/**
	 * @param searchForCn the searchForCn to set
	 */
	public final void setSearchForCn(String searchForCn)
	{
		this.searchForCn = searchForCn;
	}

	/**
	 * @return the lastVisitedAnchor
	 */
	public final String getLastVisitedAnchor()
	{
		return lastVisitedAnchor;
	}

	/**
	 * @param lastVisitedAnchor the lastVisitedAnchor to set
	 */
	public final void setLastVisitedAnchor(String lastVisitedAnchor)
	{
		this.lastVisitedAnchor = lastVisitedAnchor;
	}

	/**
	 * @return the anchorCounter
	 */
	public final String getAnchorCounter()
	{
		return String.valueOf(anchorCounter);
	}

	/**
	 * @return the anchorCounter
	 */
	public final String getIncAnchorCounter()
	{
		try
		{
			FacesContext.getCurrentInstance().getResponseWriter().write("<a name=\"ninvalidAnchor" + (++anchorCounter) + "\"></a>");
		}
		catch (IOException ignore){}

		return String.valueOf(anchorCounter);
	}
}