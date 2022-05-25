package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.sun.star.auth.InvalidArgumentException;

import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

public class CopyUserBean extends BaseWaiDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4942879585650008363L;
	
	private static final Log logger = LogFactory.getLog(CopyUserBean.class);
	
	private boolean deleteOldUser;
	private boolean takeOwnership;
	private boolean copyMembership;
	private String newUserName;
	private String oldUserName;
	
	private UploadedFile submittedFile;
	
	private ContentService contentService;
	
	public void init(final Map<String, String> parameters)
    {
      
		super.init(parameters);
		deleteOldUser = false;
		takeOwnership = true;
		copyMembership = true ;
		newUserName = "";
		oldUserName = "";
		submittedFile = null;
		
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
		if(oldUserName.length() > 0 && newUserName.length() > 0)
		{
			getUserService().copyUser(oldUserName, newUserName, deleteOldUser,  takeOwnership,copyMembership);
		}
		
		if(submittedFile != null)
		{
			Runnable r = new ParseAndCopyUsersRunnable(AuthenticationUtil.getFullyAuthenticatedUser());
			getAsyncThreadPoolExecutor().execute(r);
			
			Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate("action_background"));
			
		}
		
		return "finish";
	}
	
	private class ParseAndCopyUsersRunnable implements Runnable {
		
		public ParseAndCopyUsersRunnable(String userName) {
			this.userName = userName;
		}
		
		protected String userName;
		
	    public void run(){
	    	
	    	AuthenticationUtil.runAs( new AuthenticationUtil.RunAsWork<String>() {
				public String doWork()
         		{
					parseAndCopyUsers(submittedFile , deleteOldUser,  takeOwnership,copyMembership);
					return null;
					
			}
			},userName);
	    	
	    }
	  }

	private void parseAndCopyUsers(final UploadedFile submittedFile2, boolean deleteOldUser2, boolean takeOwnership2, boolean copyMembership2) 
	{
		if(submittedFile2 == null)
		{
			if(logger.isErrorEnabled())
			{
				logger.error("The excel file cannot be null the copy user job will not run",  new InvalidArgumentException("Submitted file empty in copy user"));
			}
		}
		else
		{
			try {
				
				final Workbook wb = new HSSFWorkbook(submittedFile2.getInputStream());
				
				for(Integer iSheet=0; iSheet < wb.getNumberOfSheets(); iSheet++)
				{
					Sheet sheet = wb.getSheetAt(iSheet);
					
					processRow(deleteOldUser2, takeOwnership2, copyMembership2,
							sheet);
				}
				
				
				
				RetryingTransactionHelper helper = getTransactionService().getRetryingTransactionHelper();
			     helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
		        {
		            public NodeRef execute() throws Throwable
		            {
		            	String filename = submittedFile2.getName().split("\\.")[0]+".processed."+new Date().getTime()+".xls";
		            	
		            	return writeFile(getManagementService().getCircabcDictionaryNodeRef(), filename, submittedFile2+".processed", wb); 
		            }
		        }, false, true);
				
			} catch (IOException e) {
				
				if(logger.isErrorEnabled())
				{
					logger.error("impossible to read the file from the copy user job",  e);
				}
			}
			
		}
		
	}
	/**
	 * @param deleteOldUser2
	 * @param takeOwnership2
	 * @param copyMembership2
	 * @param sheet
	 */
	private void processRow(boolean deleteOldUser2, boolean takeOwnership2,
			boolean copyMembership2, Sheet sheet) {
		Iterator<Row> rowIterator = sheet.iterator();
		 while(rowIterator.hasNext()) 
		 {
		    Row row = rowIterator.next();
		    
		    if(row.getRowNum()>0)
		    {
		    	if(row != null)
				{
					if(row.getCell(0) != null && row.getCell(1) != null)
					{
						if(!row.getCell(0).getStringCellValue().equals("") && !row.getCell(1).getStringCellValue().equals(""))
						{
							try
							{
								getUserService().copyUser(row.getCell(0).getStringCellValue(), 
								row.getCell(1).getStringCellValue(), deleteOldUser2, takeOwnership2, copyMembership2);
							
								row.createCell(3).setCellValue("done");
							}
							catch(Exception e)
							{
								if(logger.isErrorEnabled())
								{
									logger.error("Problem during the copy of user from"+row.getCell(0).getStringCellValue()+ " to " +
											row.getCell(1).getStringCellValue(), e);
								}
								
								row.createCell(3).setCellValue("failed");
							}
						}
					}
				}
		    }
		 }
	}
	
	private NodeRef writeFile(NodeRef reportSaveFolder, String fileName, String title, Workbook wb) throws IOException
	{
		NodeRef node = getNodeService().getChildByName(reportSaveFolder, ContentModel.ASSOC_CONTAINS, fileName);
   	 
   	 if(node == null)
        {
   		 final FileInfo fileInfo = getFileFolderService().create(reportSaveFolder, FilenameUtils.getName(fileName), ContentModel.TYPE_CONTENT);
            final NodeRef createdNodeRef = fileInfo.getNodeRef();
            getNodeService().setProperty(createdNodeRef, ContentModel.PROP_TITLE, title);
            File tempFile = TempFileProvider.createTempFile(FilenameUtils.getName(fileName), "tmp");
            FileOutputStream fileWriter =null;
            try 
            {
           	 fileWriter = new FileOutputStream(tempFile) ;
           	 wb.write(fileWriter);
            }
            finally
            {
           	 if (fileWriter != null)
           	 {
           		 fileWriter.close();
           	 }
            }
    		 
            // get a writer for the content and put the file
            final ContentWriter writer = getContentService().getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);

            writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
         
            writer.putContent(tempFile);
            
            boolean isDeleted = tempFile.delete();
            if (!isDeleted && logger.isWarnEnabled())
            {
         	   try {
					logger.warn ("Unable to delete file : " + tempFile.getCanonicalPath() ) ;
				} catch (IOException e) {
					logger.warn ("Unable to get getCanonicalPath for ." + tempFile.getPath() ,e ) ;
				}
            }

            node=createdNodeRef;
        }
   	 
   	 	return node;
	}
	
	public void setOldUserName(String oldUserName)
	{
		this.oldUserName = oldUserName;
	}

	public String getOldUserName()
	{
		return oldUserName;
	}

	public void setNewUserName(String newUserName)
	{
		this.newUserName = newUserName;
	}

	public String getNewUserName()
	{
		return newUserName;
	}

	public void setTakeOwnership(boolean takeOwnership)
	{
		this.takeOwnership = takeOwnership;
	}

	public boolean isTakeOwnership()
	{
		return takeOwnership;
	}

	public void setDeleteOldUser(boolean deleteOldUser)
	{
		this.deleteOldUser = deleteOldUser;
	}

	public boolean isDeleteOldUser()
	{
		return deleteOldUser;
	}

	public void setCopyMembership(boolean copyMembership)
	{
		this.copyMembership = copyMembership;
	}

	public boolean isCopyMembership()
	{
		return copyMembership;
	}
	public UploadedFile getSubmittedFile() {
		return submittedFile;
	}
	public void setSubmittedFile(UploadedFile submittedFile) {
		this.submittedFile = submittedFile;
	}
	/**
	 * @return the contentService
	 */
	public ContentService getContentService() {
		return contentService;
	}
	/**
	 * @param contentService the contentService to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

}
