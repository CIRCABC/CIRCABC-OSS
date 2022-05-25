package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.processor.Processor;
import eu.cec.digit.circabc.migration.walker.TreeWalkerVisitorBase;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;

/**
 * Base class for importation processors.
 *
 * @author Yanick Pignot
 */
public abstract class MigrateProcessorBase extends TreeWalkerVisitorBase implements Processor
{
	private static final Log logger = LogFactory.getLog(MigrateProcessorBase.class);
	
	private MigrationTracer _journal;
	private CircabcServiceRegistry registry;

	public MigrateProcessorBase()
	{
		super();
	}

	public MigrateProcessorBase(final ImportRoot importRoot, final MigrationTracer _journal, final CircabcServiceRegistry registry)
	{
		super(importRoot);
		this._journal = _journal;
		this.registry = registry;
	}

	public Report process(final CircabcServiceRegistry registry, final ImportRoot importRoot, final MigrationTracer importationJournal) throws ImportationException
	{
		ParameterCheck.mandatory("The service registry", registry);
		ParameterCheck.mandatory("The root element", importRoot);
        ParameterCheck.mandatory("An instance of the importation journal", importationJournal);
        ParameterCheck.mandatory("An instance of the report", importationJournal.getProcessReport());
        ParameterCheck.mandatory("An instance of the report report", importationJournal.getJournal());

        this._journal = importationJournal;
        this.registry = registry;

        try
     	{
        	walk(importRoot);
     	}
     	catch(ImportationException ex)
     	{
     		throw ex;
     	}
     	catch(Throwable t)
     	{
     		throw new ImportationException("Unexpected error when walking the import root element", t);
     	}

        final Report processReport = _journal.getProcessReport();
        onFinish();

		return processReport;
	}
	
	protected boolean isFirstImport()
	{
		return (this._journal.getIteration().getTransformationDates().size() == 1);
	}

	@SuppressWarnings("unchecked")
	protected Object apply(final RetryingTransactionCallback callback) throws ImportationException
	{

		if (isFirstImport())
		{
			return apply(callback, true);
		}
		else
		{
			return apply(callback, false);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected Object apply(final RetryingTransactionCallback callback, boolean requireNew) throws ImportationException
	{
		final RetryingTransactionHelper txnHelper = getTransactionService().getRetryingTransactionHelper();

		getPolicyBehaviourFilter().disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);

		try
		{
			return txnHelper.doInTransaction(callback, false, requireNew);
		}
		catch(Throwable t)
		{
			if(isFailOnError())
			{
				throw new ImportationException("Error occurs when creating spaces.", t);
			}
			else
			{
				return null;
			}
		}
	}

	abstract class JournalizedTransactionCallback implements RetryingTransactionCallback
	{
		private MigrationTracer journal;

		/**
		 * @param journal
		 */
		public JournalizedTransactionCallback(final MigrationTracer journal)
		{
			super();

			ParameterCheck.mandatory("An instance of the importation journal", journal);
	        ParameterCheck.mandatory("An instance of the report", journal.getProcessReport());

			this.journal = journal;
		}

		/**
		 * @return the journal
		 */
		protected final MigrationTracer getJournal()
		{
			return journal;
		}

		/**
		 * @return the journal
		 */
		protected final Report getReport()
		{
			return journal.getProcessReport();
		}
	}

	abstract class MigrateNodesCallback extends JournalizedTransactionCallback
    {
        protected final Node node;
        public MigrateNodesCallback(final MigrationTracer journal, final Node node)
        {
        	super(journal);
        	this.node = node;
        }

        public String execute() throws Throwable
        {
    		try
			{
				if(ElementsHelper.isNodeCreated(node) == false)
	            {
	                 final NodeRef nodeRef = executeImpl(node);
	                 ElementsHelper.setNodeRef(node, nodeRef);
	                 applyUIFacets(nodeRef, getIcon());

					 final JournalLine journalLineNode = JournalLine.createNode(Status.SUCCESS, ElementsHelper.getQualifiedPath(node), nodeRef.toString());
					 super.getJournal().journalize(journalLineNode);
					 getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " successfully created. ");

					 final LogRecord record = new LogRecord();
					 record.setService("Migration");
					 record.setActivity("Import");
					 record.setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
					 record.setOK(Boolean.TRUE);
					 record.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
					 if(node instanceof Circabc)
					 {
						 record.setIgID(record.getDocumentID());
						 record.setIgName("CircaBC");
						 record.setPath("/CircaBC");
					 }
					 else if(node instanceof Category)
					 {
						record.setIgID(record.getDocumentID());
						record.setIgName((String) ((NamedNode) node).getName().getValue());
						record.setPath("/CircaBC/" + record.getIgName());
					 }
					 else if(node instanceof CategoryHeader)
					 {
						record.setIgID(record.getDocumentID());
						record.setIgName((String) ((NamedNode) node).getName().getValue());
						record.setPath("/CircaBC/" + record.getIgName());
					 }
					 else
					 {
						 final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(node);
						 record.setIgName((String) interestGroup.getName().getValue());
						 record.setPath(getManagementService().getNodePath(nodeRef).toString());

						 final NodeRef igRef = ElementsHelper.getNodeRef(interestGroup);
						 record.setIgID((Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID));
					 }

					 getLogService().log(record);
	            }
				else
				{
					getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " already created. ");
				}
			}
			catch(final Throwable t)
			{
				if(logger.isErrorEnabled()) {
					logger.error("An error occur", t);
				}
				reportError(t);
			}
			return null;
		}

		/**
		 * @param t
		 * @throws Throwable
		 */
		protected void reportError(Throwable t) throws Throwable
		{
			getReport().appendSection("Impossible to create node " + ElementsHelper.getQualifiedPath(node) + ". " + t.getMessage());

			if(isFailOnError())
			{
				throw t;
			}
			else
			{
				final JournalLine journalLineNode = JournalLine.createNode(Status.FAIL, ElementsHelper.getQualifiedPath(node), null);
				super.getJournal().journalize(journalLineNode);
			}
		}

        protected void applyUIFacets(final NodeRef ref, final String icon)
        {
        	 final Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);

             uiFacetsProps.put(ContentModel.PROP_TITLE, "" );
             uiFacetsProps.put(ContentModel.PROP_DESCRIPTION,"");

             if(icon != null)
        	 {
        		 uiFacetsProps.put(ApplicationModel.PROP_ICON, icon);
        		 getNodeService().addAspect(ref, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
        	 }
             else
             {
            	 getNodeService().addAspect(ref, ContentModel.ASPECT_TITLED, uiFacetsProps);
             }
        }

        protected String getName()
        {
        	return (String) ElementsHelper.getProperties(node).get(ContentModel.PROP_NAME);
        }

        protected NodeRef executeImplGeneric(final Node node, final QName typeQName)throws Throwable
    	{
        	XMLElement parent = ElementsHelper.getParent(node);

        	if(node instanceof LibraryContentVersion || node instanceof LibraryTranslationVersion || node instanceof InformationContentVersion || node instanceof InformationTranslationVersion)
        	{
        		// Version container
        		parent = ElementsHelper.getParent(parent);
        		// Current version
        		parent = ElementsHelper.getParent(parent);
        	}

        	if(parent instanceof MlContent || parent instanceof InfMLContent)
        	{
        		parent = ElementsHelper.getParent(parent);
        	}
        	else if(parent instanceof Message)
        	{
        		parent = ElementsHelper.getElementTopic(parent);
        	}

        	final NodeRef parentRef = ElementsHelper.getNodeRef((Node)parent);
			final FileInfo fileInfo = getFileFolderService().create(parentRef, getName(), typeQName);
    		return fileInfo.getNodeRef();
    	}

        public String getIcon() throws Throwable
        {
        	return null;
        }

        abstract public NodeRef executeImpl(Node node)throws Throwable;

    };

    public void onFinish()
	{
    	_journal = null;
    	importRoot = null;
		circabcContext = null;
		personsContext = null;
	}

	/**
	 * @return the failOnError
	 */
	protected final boolean isFailOnError()
	{
		return _journal.isFailOnError();
	}

	/**
	 * @return the journal
	 */
	protected final MigrationTracer getJournal()
	{
		return _journal;
	}

	
	/**
	 * set the journal
	 */
	protected final void setJournal(MigrationTracer journal)
	{
		_journal = journal;
	}

	
	/**
	 * @return the transactionService
	 */
	protected final TransactionService getTransactionService()
	{
		return registry.getAlfrescoServiceRegistry().getTransactionService();
	}

	/**
	 * @return the nodeService
	 */
	protected final NodeService getNodeService()
	{
		return registry.getAlfrescoServiceRegistry().getNodeService();
	}

	/**
	 * @return the fileFolderService
	 */
	protected final FileFolderService getFileFolderService()
	{
		return registry.getAlfrescoServiceRegistry().getFileFolderService();
	}

	/**
	 * @return the userService
	 */
	protected final UserService getUserService()
	{
		return registry.getUserService();
	}


	/**
	 * @return the policyBehaviourFilter
	 */
	protected final BehaviourFilter getPolicyBehaviourFilter()
	{
		return registry.getPolicyBehaviourFilter();
	}

	/**
	 * @return the logService
	 */
	protected final LogService getLogService()
	{
		return registry.getLogService();
	}

	/**
	 * @return the managementService
	 */
	protected final ManagementService getManagementService()
	{
		return registry.getManagementService();
	}

	/**
	 * @return the registry
	 */
	protected final CircabcServiceRegistry getRegistry()
	{
		return registry;
	}
}
