/**
 * 
 */
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.DocumentDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Document;
import eu.cec.digit.circabc.migration.reader.impl.circa.ibatis.DocumentDaoImpl;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

/**
 * @author beaurpi
 *
 */
public class UpdateLinksBean extends BaseWaiDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<SelectItem> categories;
	private List<SelectItem> igs;
	
	private String selectedCategory;
	private String selectedInterestGroup;
	
	private String rootServerUrl;
	
	private List<NodeRef> igLinks;
	private String[][] data;
	private String circaCateg;
	private String circaIg;
	
	private String[] encodingNumber = {"_x0030_","_x0031_","_x0032_","_x0033_","_x0034_","_x0035_","_x0036_","_x0037_","_x0038_","_x0039_"};
	
	private static final Log logger = LogFactory.getLog(UpdateLinksBean.class);
	
	@Override
	public void init(Map<String, String> parameters) 
	{
		
		super.init(parameters);
		
		buildCategories();
		buildIgs(new NodeRef((String) categories.get(0).getValue()));
	}


	private void buildIgs(NodeRef nodeRef) 
	{
		igs = new ArrayList<SelectItem>();

		for(NodeRef ig: getManagementService().getInterestGroups(nodeRef))
		{
			igs.add(new SelectItem(ig.toString(), getNodeService().getProperty(ig, ContentModel.PROP_NAME).toString()));
		}
		
	}


	private void buildCategories()
	{

			categories = new ArrayList<SelectItem>();

		
		for(NodeRef categ: getManagementService().getCategories())
		{
			categories.add(new SelectItem(categ.toString(), getNodeService().getProperty(categ, ContentModel.PROP_NAME).toString()));
		}
		
	}
	
	
	public void updateIgs()
	{
		buildIgs(new NodeRef(this.selectedCategory));
		
		circaCateg = getNodeService().getProperty(new NodeRef(selectedCategory), ContentModel.PROP_NAME).toString();
	}
	
	public void prepareUpdate()
	{
		if(this.rootServerUrl.length()>0)
		{
		
			DocumentDao documentsDao = new DocumentDaoImpl();
			
			String circabcCategory = getNodeService().getProperty(new NodeRef(selectedCategory), ContentModel.PROP_NAME).toString();
			String circabcIg = getNodeService().getProperty(new NodeRef(selectedInterestGroup), ContentModel.PROP_NAME).toString();
			
			String encoded9075CircabcGroup = ISO9075.encode(circabcIg);
			String encoded9075CircabcCategory = ISO9075.encode(circabcCategory);
			
			for(Integer i=0; i< data.length ;i++)
			{
	
				// check to ignore external websites | links from other IG | anything else that is not inside library
				if(data[i][2].contains("/"+circaIg+"/") && data[i][2].matches(".*circa.europa.eu.*") && !data[i][2].contains("n=europa"))
				{
					data[i][5]= "true";
					data[i][4] = "Ok";
					
					String urlSource = data[i][2];
					String identifier = urlSource.replace("/library?l=", "/LIB/DOC"); 
					identifier = identifier.substring(identifier.lastIndexOf("/irc")+4);
					if(identifier.contains("&"))
					{
						identifier = identifier.substring(0, identifier.indexOf("&"));
					
					}
					identifier = "/europa" + identifier;
					
					StringBuilder cbcPath = new StringBuilder(1024);
					cbcPath.append("PATH:\"/app:company_home/cm:CircaBC/cm:"+encoded9075CircabcCategory+"/cm:"+encoded9075CircabcGroup+"/cm:Library");
					
					if(identifier.contains(".")) // it is a file
					{
						try {
							
							Document tmp = documentsDao.getDocumentByIdentifier(circaCateg, circaIg, identifier);
							
							if(tmp != null){
								String fileName = tmp.getAlternative();
								String path = identifier.substring(identifier.indexOf("LIB/DOC/")+8);
								String[] pathElements = path.split("/");
								
								for(Integer j=0; j<pathElements.length-2; j++)
								{
									cbcPath.append("/cm:");
									
									cbcPath.append(ISO9075.encode(pathElements[j]));
									
								}
								
								cbcPath.append("/*\" AND @name:\"");
								cbcPath.append(fileName);
								cbcPath.append("\"");
								
								SearchParameters sp = new SearchParameters();
						        sp.addStore(new NodeRef(selectedInterestGroup).getStoreRef());
						        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
						        sp.setQuery(cbcPath.toString());
						        
						        ResultSet results = getSearchService().query(sp);
						        
						        if(results.length() == 1)
						        {
						        	data[i][3] = rootServerUrl+"/w/browse/"+results.getNodeRef(0).getId();
						        	data[i][6] = getNodeService().getProperty(results.getNodeRef(0), ContentModel.PROP_NAME).toString();
						        }
						        else if(results.length() == 0)
						        {
						        	data[i][5] = "false";
						        	data[i][4] = "no matching node in CIRCABC";
						        	data[i][6] = "circa name= "+fileName;
						        	
						        }
						        else
						        {
						        	data[i][5] = "false";
						        	data[i][4] = "more than 1 matching node in CIRCABC";
						        	
						        	for(int k = 0; k < results.length(); k++)
						        	{
						        		data[i][6] = getNodeService().getProperty(results.getNodeRef(k), ContentModel.PROP_NAME).toString();
						        	}
						        }
	   
							}
							
						} catch (SQLException e) {
							if(logger.isErrorEnabled())
							{
								logger.error("Error during querying CIRCA DB, impossible to get: "+identifier,e);
							}
						} catch (IOException e) {
							if(logger.isErrorEnabled())
							{
								logger.error("Error during querying CIRCA DB, IO error",e);
							}
						} 
					}
					else
					{
						String path = identifier.substring(identifier.indexOf("LIB/DOC/")+8);
						String[] pathElements = path.split("/");
						
						for(Integer j=0; j<pathElements.length; j++)
						{
							cbcPath.append("/cm:");
							cbcPath.append(ISO9075.encode(pathElements[j]));
						}
						
						cbcPath.append("\"");
						
						SearchParameters sp = new SearchParameters();
				        sp.addStore(new NodeRef(selectedInterestGroup).getStoreRef());
				        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				        sp.setQuery(cbcPath.toString());
				        
				        ResultSet results = getSearchService().query(sp);
				        
				        if(results.length() == 1)
				        {
				        	data[i][3] = rootServerUrl+"/w/browse/"+results.getNodeRef(0).getId();
				        	data[i][6] = getNodeService().getProperty(results.getNodeRef(0), ContentModel.PROP_NAME).toString();
				        }
				        else if(results.length() == 0)
				        {
				        	data[i][5] = "false";
				        	data[i][4] = "no matching node in CIRCABC";
				        	data[i][6] = "folder name= "+cbcPath.substring(cbcPath.lastIndexOf(":")+1, cbcPath.length()-1);
				        }
				        else
				        {
				        	data[i][5] = "false";
				        	data[i][4] = "more than 1 matching node in CIRCABC";
				        	data[i][6] = "folder name= "+cbcPath.substring(cbcPath.lastIndexOf(":")+1, cbcPath.length()-1);
				        }
	
					}
				}
				else
				{
					data[i][5]= "false";
					data[i][4] = "will be ignored";
				}
			}
		}
		else
		{
			Utils.addErrorMessage("New Root url cannot be null");
		}
	}
	
	public void select()
	{
		circaIg = getNodeService().getProperty(new NodeRef(selectedInterestGroup), ContentModel.PROP_NAME).toString();
		circaCateg = getNodeService().getProperty(new NodeRef(selectedCategory), ContentModel.PROP_NAME).toString();
		
		String query = "PATH:\"/app:company_home/cm:CircaBC/cm:"+
						getNodeService().getProperty(new NodeRef(selectedCategory), ContentModel.PROP_NAME).toString()+
						"/cm:"+
						getNodeService().getProperty(new NodeRef(selectedInterestGroup), ContentModel.PROP_NAME).toString()+
						"/cm:Library//*\" AND ASPECT:\"{http://www.cc.cec/circabc/model/document/1.0}urlable\"";
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(new NodeRef(selectedInterestGroup).getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        
        ResultSet results = null;
        
        try
        {
            results = getSearchService().query(sp);
            igLinks = results.getNodeRefs();
            
            data = new String[results.length()][7];
            
            Integer i=0;
            for(NodeRef ref: igLinks)
            {
            	data[i][0] = ref.toString();
            	data[i][1] = getNodeService().getProperty(ref, ContentModel.PROP_NAME).toString();
            	data[i][2] = getNodeService().getProperty(ref, DocumentModel.PROP_URL).toString();
            	data[i][3] = "";
            	
            	i++;
            	
            }
            
        }
        finally
        {
            if(results != null)
            {
                results.close();
            }
        } 
		
	}

	/**
	 * @return the categories
	 */
	public List<SelectItem> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(List<SelectItem> categories) {
		this.categories = categories;
	}

	/**
	 * @return the igs
	 */
	public List<SelectItem> getIgs() {
		return igs;
	}

	/**
	 * @param igs the igs to set
	 */
	public void setIgs(List<SelectItem> igs) {
		this.igs = igs;
	}

	/**
	 * @return the selectedCategory
	 */
	public String getSelectedCategory() {
		return selectedCategory;
	}

	/**
	 * @param selectedCategory the selectedCategory to set
	 */
	public void setSelectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	/**
	 * @return the selectedInterestGroup
	 */
	public String getSelectedInterestGroup() {
		return selectedInterestGroup;
	}

	/**
	 * @param selectedInterestGroup the selectedInterestGroup to set
	 */
	public void setSelectedInterestGroup(String selectedInterestGroup) {
		this.selectedInterestGroup = selectedInterestGroup;
	}

	public void reset(ActionEvent event)
	 {
		 this.init(null);
	 }

	@Override
	public String getPageIconAltText() {
		
		return null;
	}

	@Override
	public String getBrowserTitle() {
		
		return "Update Link dialog";
	}

	/**
	 * @return the igLinks
	 */
	public List<NodeRef> getIgLinks() {
		return igLinks;
	}


	/**
	 * @param igLinks the igLinks to set
	 */
	public void setIgLinks(List<NodeRef> igLinks) {
		this.igLinks = igLinks;
	}


	/**
	 * @return the circaCateg
	 */
	public String getCircaCateg() {
		return circaCateg;
	}


	/**
	 * @param circaCateg the circaCateg to set
	 */
	public void setCircaCateg(String circaCateg) {
		this.circaCateg = circaCateg;
	}


	/**
	 * @return the circaIg
	 */
	public String getCircaIg() {
		return circaIg;
	}


	/**
	 * @param circaIg the circaIg to set
	 */
	public void setCircaIg(String circaIg) {
		this.circaIg = circaIg;
	}


	/**
	 * @return the data
	 */
	public String[][] getData() {
		return data;
	}


	/**
	 * @param data the data to set
	 */
	public void setData(String[][] data) {
		this.data = data;
	}


	@Override
	protected String finishImpl(FacesContext context, String outcome)
			throws Throwable {
		
		for(int i=0; i<data.length; i++)
		{
			if(Boolean.parseBoolean(data[i][5]))
			{
				NodeRef tmpRef = new NodeRef(data[i][0]);
				getNodeService().setProperty(tmpRef, DocumentModel.PROP_URL, data[i][3]);
			}
		}
		
		
		return "finish";
	}


	/**
	 * @return the rootServerUrl
	 */
	public String getRootServerUrl() {
		return rootServerUrl;
	}


	/**
	 * @param rootServerUrl the rootServerUrl to set
	 */
	public void setRootServerUrl(String rootServerUrl) {
		this.rootServerUrl = rootServerUrl;
	}

}
