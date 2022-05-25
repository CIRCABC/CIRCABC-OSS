package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;

import eu.cec.digit.circabc.util.exporter.CircabcExporter;
import eu.cec.digit.circabc.util.importer.ImportActionExecuter;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

/**
 * Bean to implement the import admin screen.
 * 
 * @author schwerr
 */
public class ImporterBean extends BaseWaiDialog {
	
	private static final long serialVersionUID = 8516088497863408026L;
	
	private final static String SELECT_CATEGORY_NONE = "--";
	
	private Map<String, Object> categories = null;
	private String selectedCategory = null;
	
	private Map<String, Object> packages = null;
	private List<String> selectedPackages = null;
	
	private static String exportDirectory = "/ec/local/weblogic/app/exports";
	
	private boolean importAuthorities = true;
	private boolean importStructure = true;
	private boolean importHeaders = false;
	private boolean keepOriginalNodeIds = true;
	
	private ActionService actionService = null;
	private static Map<String, Action> actionMap = new HashMap<String, Action>();
	
	private Date importStartDate = null;
	
	/**
	 * Initializes the system.
	 */
	public void init(final Map<String, String> parameters) {
		
		super.init(parameters);
		
		categories = selectCategories();
		
		selectPackages(false);
		
		importStartDate = new Date();
	}
	
	/**
	 * Retrieves all the categories and fills a map for the select combo boxes.
	 * 
	 * @return
	 */
	private Map<String, Object> selectCategories() {
		
		categories = new TreeMap<String, Object>();
		categories.put(SELECT_CATEGORY_NONE, SELECT_CATEGORY_NONE);
		selectedCategory = SELECT_CATEGORY_NONE;
		
		List<NodeRef> categoryNodeRefs = getManagementService().getCategories();
		
		// Add the selected categories to the map
		for (NodeRef categoryNodeRef : categoryNodeRefs) {
			
			String title = (String) getNodeService().getProperty(categoryNodeRef, 
							ContentModel.PROP_TITLE);
			
			categories.put(title, categoryNodeRef.toString());
		}
		
		return categories;
	}
	
	/**
	 * Executed when a category is selected to update the packages list.
	 * 
	 * @param event
	 */
	public void selectPackages(ValueChangeEvent event) {
		
		String newValue = (String) event.getNewValue();
		
		selectPackages(!SELECT_CATEGORY_NONE.equals(newValue));
	}
	
	/**
	 * Retrieves the packages from the directory in case category has been 
	 * selected.
	 * 
	 * @param categorySelected
	 * @return
	 */
	private Map<String, Object> selectPackages(boolean categorySelected) {
		
		packages = new TreeMap<String, Object>();
		
		File exportDirectoryFile = new File(exportDirectory);
		
		// Check if the export directory is valid
		if (!exportDirectoryFile.exists()) {
			Utils.addErrorMessage(translate("migration_export_category_ig_dialog_page_error_directory"));
			return packages;
		}
		
		// Select the prefix for IG or Category
		final String prefix = categorySelected ? CircabcExporter.IG_PREFIX : 
											CircabcExporter.CATEGORY_PREFIX;
		
		// List the files that start with prefix
		String[] fileNames = exportDirectoryFile.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix);
			}
		});
		
		// Add the retrieved files to the combo box map
		for (String fileName : fileNames) {
			
			int pos = fileName.lastIndexOf(CircabcExporter.CIRCABC_SUFFIX);
			
			if (pos == -1) {
				continue;
			}
			
			String displayName = fileName.substring(prefix.length(), pos);
			
			// Add the fileName to the list if it exists. this is done because
			// one import package can have many composing files (the ones added 
			// here)
			if (packages.containsKey(displayName)) {
				fileName = (String) packages.get(displayName) + ":" + fileName;
			}
			
			packages.put(displayName, fileName);
		}
		
		return packages;
	}
	
	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
	 */
	@Override
	public String getPageIconAltText() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
	 */
	@Override
	public String getBrowserTitle() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Calls the import action.
	 * 
	 * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
	 */
	@Override
	protected String finishImpl(FacesContext context, String outcome)
			throws Throwable {
		
		if ((importAuthorities || importStructure || importHeaders) && selectedPackages.size()>0)
		{
			NodeRef importNodeRef = null;
			if (SELECT_CATEGORY_NONE.equals(selectedCategory))
			{
				// use circabcNodeRef as parent for the selected category
				importNodeRef = getManagementService().getCircabcNodeRef();
			}
			else if (selectedCategory != null)
			{
				importNodeRef = new NodeRef(selectedCategory);
			}
			
			String importName = getImportName();
			Action action = actionService.createAction(ImportActionExecuter.NAME);
			
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_DIRECTORY, exportDirectory);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_AUTHORITIES, importAuthorities);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_STRUCTURE, importStructure);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_HEADERS, importHeaders);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_KEEP_UUIDS, keepOriginalNodeIds);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_NAME, importName);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_PACKAGES, (Serializable)selectedPackages);
			action.setParameterValue(
					ImportActionExecuter.PARAM_IMPORT_START, importStartDate);
			
			StringBuilder actionName = new StringBuilder(importName);
			actionName.append(" (Authorities : ").append(importAuthorities ? "Y" : "N");
			actionName.append(" - Headers : ").append(importHeaders ? "Y" : "N");
			actionName.append(" - Structure : ").append(importStructure ? "Y" : "N");
			actionName.append(" - Keep UUIDs : ").append(keepOriginalNodeIds ? "Y" : "N");
			actionName.append(")<br/>");
			
			actionMap.put(actionName.toString(), action);
	    	actionService.executeAction(action, importNodeRef, false, true);
		}
		
		return "finish";
	}
	
	private String getImportName()
	{
		StringBuilder sb = new StringBuilder();
		Set<Entry<String, Object>> packages = getPackages().entrySet();
		Entry<String, Object> entry;
		for (Iterator iterator = packages.iterator(); iterator.hasNext();)
		{
			entry = (Entry<String, Object>) iterator.next();
			if (selectedPackages.contains((String) entry.getValue()))
			{
				if (sb.length() > 0)
					sb.append(" ");
				sb.append(entry.getKey());
					
			}
		}
		return sb.toString();
	}
	
	/**
	 * Does the final validation before finishing the implementation 
	 * (finishImpl)
	 * 
	 * @see org.alfresco.web.bean.dialog.BaseDialogBean#finish()
	 */
	@Override
	public String finish() {
		
		// Check if the export directory is valid
		if (!(new File(exportDirectory).exists())) {
			Utils.addErrorMessage(translate("migration_export_category_ig_dialog_page_error_directory"));
			return null;
		}
		
		return super.finish();
	}
	
	/**
	 * Called from the JSP's 'Check' button to check if the path from where to 
	 * retrieve the packages to import is right
	 */
	public void check() {
		
		// Check if the export directory is valid
		if (!(new File(exportDirectory)).exists()) {
			Utils.addErrorMessage(translate("migration_export_category_ig_dialog_page_error_directory"));
			packages = new TreeMap<String, Object>();
			selectedPackages = null;
			return;
		}
		
		selectPackages(!SELECT_CATEGORY_NONE.equals(selectedCategory));
	}
	
	/**
	 * Reset the form
	 * 
	 * @param event
	 */
	public void reset(ActionEvent event) {
		init(null);
	}
	
	/**
	 * Get the list of import statuses
	 * 
	 * @return
	 */
	public String getWorkingImportNames() {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		String statuses = "<br/><br/>";
		
		Iterator<Map.Entry<String, Action>> iterator = actionMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			
			Map.Entry<String, Action> actionEntry = iterator.next();
			
			String status = (String) 
							actionEntry.getValue().getParameterValue(
								ImportActionExecuter.PARAM_CURRENT_STATUS);
			
			Date startDate = (Date) actionEntry.getValue().getParameterValue(
					ImportActionExecuter.PARAM_START_DATE);
			
			statuses += actionEntry.getKey() + " [" + 
					((startDate != null) ? simpleDateFormat.format(startDate) : "") + 
						"] -> " + status;			
			
			String executionTime = (String) actionEntry.getValue().getParameterValue(
					ImportActionExecuter.PARAM_EXE_TIME);
			if (executionTime!=null)
			{
				statuses += " (Done in " + executionTime + ".)<br/>";
			}
			
			if (iterator.hasNext()) {
				statuses += "<br/>";
			}
		}
		
		if ("<br/><br/>".equals(statuses)) {
			statuses = "-";
		}
		
		return statuses;
	}
	
	/**
	 * Sets the value of the actionService
	 * 
	 * @param actionService the actionService to set.
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
	
	/**
	 * Gets the value of the categories
	 * 
	 * @return the categories
	 */
	public Map<String, Object> getCategories() {
		return categories;
	}
	
	/**
	 * Sets the value of the categories
	 * 
	 * @param categories the categories to set.
	 */
	public void setCategories(Map<String, Object> categories) {
		this.categories = categories;
	}
	
	/**
	 * Gets the value of the selectedCategory
	 * 
	 * @return the selectedCategory
	 */
	public String getSelectedCategory() {
		return selectedCategory;
	}
	
	/**
	 * Sets the value of the selectedCategory
	 * 
	 * @param selectedCategory the selectedCategory to set.
	 */
	public void setSelectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
	
	/**
	 * Gets the value of the exportDirectory
	 * 
	 * @return the exportDirectory
	 */
	public String getExportDirectory() {
		return exportDirectory;
	}
	
	/**
	 * Sets the value of the exportDirectory
	 * 
	 * @param exportDirectory the exportDirectory to set.
	 */
	public void setExportDirectory(String exportDirectory) {
		this.exportDirectory = exportDirectory;
	}
	
	/**
	 * Gets the value of the packages
	 * 
	 * @return the packages
	 */
	public Map<String, Object> getPackages() {
		return packages;
	}
	
	/**
	 * Sets the value of the packages
	 * 
	 * @param packages the packages to set.
	 */
	public void setPackages(Map<String, Object> packages) {
		this.packages = packages;
	}
	
	/**
	 * Gets the value of the selectedPackages
	 * 
	 * @return the selectedPackages
	 */
	public List<String> getSelectedPackages() {
		return selectedPackages;
	}
	
	/**
	 * Sets the value of the selectedPackages
	 * 
	 * @param selectedPackages the selectedPackages to set.
	 */
	public void setSelectedPackages(List<String> selectedPackages) {
		this.selectedPackages = selectedPackages;
	}
	
	/**
	 * Gets the value of the keepOriginalNodeIds
	 * 
	 * @return the keepOriginalNodeIds
	 */
	public boolean isKeepOriginalNodeIds() {
		return keepOriginalNodeIds;
	}
	
	/**
	 * Sets the value of the keepOriginalNodeIds
	 * 
	 * @param keepOriginalNodeIds the keepOriginalNodeIds to set.
	 */
	public void setKeepOriginalNodeIds(boolean keepOriginalNodeIds) {
		this.keepOriginalNodeIds = keepOriginalNodeIds;
	}
	
	/**
	 * Gets the value of the importHeaders
	 * 
	 * @return the importHeaders
	 */
	public boolean isImportHeaders() {
		return importHeaders;
	}
	
	/**
	 * Sets the value of the importHeaders
	 * 
	 * @param importHeaders the importHeaders to set.
	 */
	public void setImportHeaders(boolean importHeaders) {
		this.importHeaders = importHeaders;
	}
	
	/**
	 * Gets the value of the importStructure
	 * 
	 * @return the importStructure
	 */
	public boolean isImportStructure() {
		return importStructure;
	}
	
	/**
	 * Sets the value of the importStructure
	 * 
	 * @param importStructure the importStructure to set.
	 */
	public void setImportStructure(boolean importStructure) {
		this.importStructure = importStructure;
	}
	
	/**
	 * Gets the value of the importAuthorities
	 * 
	 * @return the importAuthorities
	 */
	public boolean isImportAuthorities() {
		return importAuthorities;
	}
	
	/**
	 * Sets the value of the importAuthorities
	 * 
	 * @param importAuthorities the importAuthorities to set.
	 */
	public void setImportAuthorities(boolean importAuthorities) {
		this.importAuthorities = importAuthorities;
	}
	
	/**
	 * Gets the value of the importStartDate
	 * 
	 * @return the importStartDate
	 */
	public Date getImportStartDate() {
		return importStartDate;
	}
	
	/**
	 * Sets the value of the importStartDate
	 * 
	 * @param importStartDate the importStartDate to set.
	 */
	public void setImportStartDate(Date importStartDate) {
		this.importStartDate = importStartDate;
	}
}
