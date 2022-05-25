/**
 * 
 */
package eu.cec.digit.circabc.migration.post.cleaning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.statistics.global.GlobalStatisticsService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;

/**
 * @author beaurpi
 *
 */
public class MissingUserReferrenceAction extends ActionExecuterAbstractBase {
	
	private static final Log logger = LogFactory.getLog(MissingUserReferrenceAction.class);

	public final static String ACTION_NAME = "scan-missing-user-referrences";
	
	private NodeService nodeService;
	private PermissionService permissionService;
	private NotificationSubscriptionService notificationSubscriptionService;
	private GlobalStatisticsService gStatService;
	private LdapUserService ldapUserService;
	private UserService userService;
	private ManagementService managementService;
	private FileFolderService fileFolderService;
	private ProfileManagerServiceFactory profileManagerServiceFactory;
	private TransactionService transactionService;
	private ContentService contentService;
	
	private static String scanningDictionaryFolderName = "missingUserScanner";
	
	private Set<String> processedUser;
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

		List<NodeRef> lIg = gStatService.getListOfCircabcInterestGroups();
		List<ScanResult> results = new ArrayList<ScanResult>();
		processedUser = new HashSet<String>();
		Double igIndex = 1.0, nbIg = (double) lIg.size();
		
		for(NodeRef igRef: lIg)
		{
			logger.info("Starting scanner with= "+igRef);
			browseNodes(igRef, results,0);
			logger.info("Scanning status: "+(igIndex * 100 / nbIg)+"%");
			igIndex++;
		}
		
		saveReport(results);
	}

	private void saveReport(final List<ScanResult> results) {
		
		final NodeRef targetFolder =  getReportSaveFolder();
		
		final Workbook wb = toXls(results);
		
		Calendar cFile = new GregorianCalendar();
		cFile.setTime(new Date());
		final String fileName = "MissingUserScanner_Report"+ cFile.get(Calendar.DAY_OF_MONTH)+"-"+(cFile.get(Calendar.MONTH)+1)+"-"+cFile.get(Calendar.YEAR)+"-"+cFile.get(Calendar.HOUR_OF_DAY)+"-"+cFile.get(Calendar.MINUTE)+"-"+cFile.get(Calendar.MILLISECOND)+".xls";
		
		if(nodeService.getType(targetFolder).equals(ContentModel.TYPE_FOLDER))
		{
			 
			 RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
			 helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
		        {
		            public NodeRef execute() throws Throwable
		            {
		            	return writeFile(targetFolder, fileName, wb); 
		            }
		        }, false, false);
	                
			 
		}
		else
		{
		  throw new CircabcRuntimeException("destination noderef:"+targetFolder.toString()+" is not a valid folder to save the MissingUserScanner_Report");
		}
	}
	
	private Workbook toXls(List<ScanResult> results) {
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("reportScannedUsers");
		
		Integer iRow = 0;
		
		HSSFRow row = sheet.createRow(iRow);
		row.createCell(0).setCellValue("userName");
		row.createCell(1).setCellValue("targetRef");
		row.createCell(2).setCellValue("targetName");
		row.createCell(3).setCellValue("actionInformation");
		
		iRow++;
		
		for(ScanResult sr: results)
		{
			HSSFRow rowTmp = sheet.createRow(iRow);
			rowTmp.createCell(0).setCellValue(sr.getUserName());
			rowTmp.createCell(1).setCellValue(sr.getTargetRef().toString());
			rowTmp.createCell(2).setCellValue(sr.getTargetName());
			rowTmp.createCell(3).setCellValue(sr.getActionInformation());
			iRow++;
		}
		
		return wb;
	}

	private NodeRef writeFile(NodeRef reportSaveFolder, String fileName, Workbook wb) throws IOException
	{
		NodeRef node = getNodeService().getChildByName(reportSaveFolder, ContentModel.ASSOC_CONTAINS, fileName);
   	 
   	 if(node == null)
        {
   		 final FileInfo fileInfo = getFileFolderService().create(reportSaveFolder, FilenameUtils.getName(fileName), ContentModel.TYPE_CONTENT);
            final NodeRef createdNodeRef = fileInfo.getNodeRef();
            
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
	
	public void prepareFolderRecipient() {

		NodeRef circabcFolderNodeRef = managementService.getCircabcDictionaryNodeRef(); 
		
		FileInfo scanningFolder =null;

		if(nodeService.getChildByName(circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, scanningDictionaryFolderName) == null)
		{
			scanningFolder = fileFolderService.create(circabcFolderNodeRef, scanningDictionaryFolderName, ContentModel.TYPE_FOLDER);

		}

	}
	
	public NodeRef getReportSaveFolder()
	{
		prepareFolderRecipient();
		
		NodeRef circabcFolderNodeRef =  managementService.getCircabcDictionaryNodeRef(); 
		
		NodeRef statFolderNodeRef = nodeService.getChildByName(circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, scanningDictionaryFolderName);
		
		return statFolderNodeRef;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Does one recursive pass to parse content or folders.
	 * 
	 * @param nodeRef
	 * @param place
	 */
	private void browseNodes(NodeRef nodeRef, List<ScanResult> results, Integer index) {
		
		if(results == null)
		{
			results = new ArrayList<ScanResult>();
		}
		
		if (nodeRef == null || !nodeService.exists(nodeRef)) {
			return;
		}
		
		QName type = nodeService.getType(nodeRef);
		
		if (ContentModel.TYPE_CONTENT.equals(type) || ForumModel.TYPE_TOPIC.equals(type))
		{
			
			scanItem(nodeRef, results, index);
			
		}
		else if (ContentModel.TYPE_FOLDER.equals(type) || ForumModel.TYPE_FORUM.equals(type)) {
			
			scanItem(nodeRef, results, index);
			
			List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
			
			for (ChildAssociationRef childRef : children) {
				NodeRef childNodeRef = childRef.getChildRef();
				if (childNodeRef != null && nodeService.exists(nodeRef)) {
					browseNodes(childRef.getChildRef(), results, index+1);
				}
			}
			
		}


		
	}

	/**
	 * @param nodeRef
	 * @param results
	 */
	private void scanItem(NodeRef nodeRef, List<ScanResult> results, Integer index) {
				
		String out = "Visiting "+(nodeService.getType(nodeRef).getLocalName().equals("folder") ? nodeService.getType(nodeRef).getLocalName()+" ": nodeService.getType(nodeRef).getLocalName())+" : ";
		for(int i=0; i<index; i++)
		{
			out+="_";
		}
		out+=nodeRef;
		logger.info(out);
		
		Set<AuthorityNotification> notifReport = notificationSubscriptionService.getNotifications(nodeRef);
		for(AuthorityNotification authorityNotification: notifReport)
		{
			if(authorityNotification.getAuthorityType() == AuthorityType.USER)
			{
				String username = authorityNotification.getAuthority();
				if(!processedUser.contains(username))
				{
					if(!userService.isUserExists(username))
					{
						CircabcUserDataBean cUser = ldapUserService.getLDAPUserDataByUid(username);
						
						ScanResult sRp = new ScanResult();
						sRp.setUserName(username);
						sRp.setTargetRef(nodeRef);
						sRp.setTargetName(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
						
						if(cUser != null)
						{
							logger.info("FROM Notification ==> user found and will be created:"+ username);
							
							userService.createLdapUser(username,false);
							sRp.setActionInformation("FROM Notification = User found in LDAP, creating alfresco account");
							
						}
						else
						{
							logger.info("FROM Notification ==> user not found and will not be created:"+ username);
							
							sRp.setActionInformation("FROM Notification = User not found in LDAP");
						}
						
						results.add(sRp);
					}
					processedUser.add(username);
				}
			}
		}
		
		Set<AccessPermission> perms = permissionService.getAllSetPermissions(nodeRef);
		for(AccessPermission ap: perms)
		{
			if(ap.getAuthorityType() ==  AuthorityType.USER)
			{
				String username = ap.getAuthority();
				if(!processedUser.contains(username))
				{
					if(!userService.isUserExists(username))
					{
						CircabcUserDataBean cUser = ldapUserService.getLDAPUserDataByUid(username);
						
						ScanResult sRp = new ScanResult();
						sRp.setUserName(username);
						sRp.setTargetRef(nodeRef);
						sRp.setTargetName(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
						
						if(cUser != null)
						{
							logger.info("FROM Permission ==> user found and will be created:"+ username);
							
							userService.createLdapUser(username,false);
							sRp.setActionInformation("FROM Permission = User found in LDAP, creating alfresco account");
							
						}
						else
						{
							logger.info("FROM Permission ==> user not found and will not be created:"+ username);
							
							sRp.setActionInformation("FROM Permission = User not found in LDAP");
						}
						
						results.add(sRp);
					}
					processedUser.add(username);
				}
			}
		}
	}

	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public GlobalStatisticsService getgStatService() {
		return gStatService;
	}

	public void setgStatService(GlobalStatisticsService gStatService) {
		this.gStatService = gStatService;
	}

	/**
	 * @return the permissionService
	 */
	public PermissionService getPermissionService() {
		return permissionService;
	}

	/**
	 * @param permissionService the permissionService to set
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * @return the notificationSubscriptionService
	 */
	public NotificationSubscriptionService getNotificationSubscriptionService() {
		return notificationSubscriptionService;
	}

	/**
	 * @param notificationSubscriptionService the notificationSubscriptionService to set
	 */
	public void setNotificationSubscriptionService(
			NotificationSubscriptionService notificationSubscriptionService) {
		this.notificationSubscriptionService = notificationSubscriptionService;
	}

	/**
	 * @return the ldapUserServiceImpl
	 */
	public LdapUserService getLdapUserService() {
		return ldapUserService;
	}

	/**
	 * @param ldapUserServiceImpl the ldapUserServiceImpl to set
	 */
	public void setLdapUserService(LdapUserService ldapUserService) {
		this.ldapUserService = ldapUserService;
	}

	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * @return the managementService
	 */
	public ManagementService getManagementService() {
		return managementService;
	}

	/**
	 * @param managementService the managementService to set
	 */
	public void setManagementService(ManagementService managementService) {
		this.managementService = managementService;
	}

	/**
	 * @return the fileFolderService
	 */
	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	/**
	 * @param fileFolderService the fileFolderService to set
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * @return the profileManagerServiceFactory
	 */
	public ProfileManagerServiceFactory getProfileManagerServiceFactory() {
		return profileManagerServiceFactory;
	}

	/**
	 * @param profileManagerServiceFactory the profileManagerServiceFactory to set
	 */
	public void setProfileManagerServiceFactory(
			ProfileManagerServiceFactory profileManagerServiceFactory) {
		this.profileManagerServiceFactory = profileManagerServiceFactory;
	}

	/**
	 * @return the transactionService
	 */
	public TransactionService getTransactionService() {
		return transactionService;
	}

	/**
	 * @param transactionService the transactionService to set
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
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
