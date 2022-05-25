/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

import eu.cec.digit.circabc.migration.entities.ElementsConverter;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Logo;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.JournalLine.UpdateOperation;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.impl.MigrateContents.MigrateContentCallback;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.service.keyword.KeywordsService;

/**
 * Import the properties on any kind of nodes using the tree walking
 *
 * @author Yanick Pignot
 */
public class MigrateProperties extends MigrateProcessorBase
{
	public MigrateProperties()
	{
		super();
	}

	public MigrateProperties(final ImportRoot importRoot, final MigrationTracer _journal, final CircabcServiceRegistry registry)
	{
		super(importRoot, _journal, registry);
	}

	/* TODO Should be configurable */
	private static final List<QName> FORBIDEN_PROPERTIES = new ArrayList<QName>(4);
	static {
		FORBIDEN_PROPERTIES.add(Version2Model.PROP_QNAME_VERSION_DESCRIPTION);
		FORBIDEN_PROPERTIES.add(Version2Model.PROP_QNAME_VERSION_LABEL);
		FORBIDEN_PROPERTIES.add(ContentModel.PROP_NAME);
		FORBIDEN_PROPERTIES.add(ContentModel.PROP_USERNAME);
	}

	/* TODO Should be configurable or dynmaic (ie read the model) */
	private static final List<QName> ML_TEXT_PROPERTIES = new ArrayList<QName>(3);
	static {
		ML_TEXT_PROPERTIES.add(ContentModel.PROP_TITLE);
		ML_TEXT_PROPERTIES.add(ContentModel.PROP_DESCRIPTION);
		ML_TEXT_PROPERTIES.add(CircabcModel.PROP_CONTACT_INFORMATION);
	}


	@Override
    public void visit(final InterestGroup interestGroup, final DynamicPropertyDefinitions dynamicPropertyDefinitions) throws Exception
	{
		apply(new MigrateDynamicPropertyCallback(getJournal(), interestGroup, dynamicPropertyDefinitions));
	}

	@Override
	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception
	{
		apply(new MigrateLogosCallback(getMigrateContents(), getJournal(), interestGroup, logoDefinitions));
	}

	@Override
    public void visit(final InterestGroup interestGroup, final KeywordDefinitions keywordDefinitions) throws Exception
	{
		apply(new MigrateKeywordCallback(getJournal(), interestGroup, keywordDefinitions));
	}

	@Override
    public void visit(final Node node, final KeywordReferences keywords) throws Exception
	{
		if(keywords != null && keywords.getIds() != null && keywords.getIds().size() > 0)
		{
			apply(new MigrateKeywordReferenceCallback(getJournal(), node, keywords));
		}
	}

	@Override
    public void visit(final Node node, final ExtendedProperty property) throws Exception
	{
		if(property != null)
		{
			
			if (isFirstImport())
			{
				visit(node, property.getQname(), property.getValue(), false, true);
			}
			else
			{
				visit(node, property.getQname(), property.getValue(), false, false);
			}
		}
	}

	@Override
    public void visit(final Node node, final TypedProperty property) throws Exception
	{
		if(property != null)
		{

			if (isFirstImport())
			{
				visit(node, property.getIdentifier(), property.getValue(), false, true);
			}
			else
			{
				visit(node, property.getIdentifier(), property.getValue(), false, false);
			}
		}
	}

	@Override
    public void visit(final Node node, final TypedPreference preference) throws Exception
	{
		if(preference != null)
		{
			
			if (isFirstImport())
			{
				visit(node, preference.getIdentifier(), preference.getValue(), true, true);
			}
			else
			{
				visit(node, preference.getIdentifier(), preference.getValue(), true, false);
			}
		
		}
	}


    /*package*/ void visit(final Node node, final QName identifier, final Serializable value, final boolean isPreference, final boolean requireNewTransaction) throws Exception
	{
		apply(new MigratePropertyCallback(getJournal(), node, identifier, value, isPreference), requireNewTransaction);
	}


    /*package*/ void visit(final Node node, final PropertyMap properties, final PropertyMap preferences, final boolean requireNewTransaction) throws Exception
	{
    	apply(new MigratePropertiesCallback(getJournal(), node, properties), requireNewTransaction);
    	// preferences can only setted one by one
    	if(preferences != null)
    	{
    		for(Map.Entry<QName, Serializable> pref: preferences.entrySet())
    		{
    			apply(new MigratePropertyCallback(getJournal(), node, pref.getKey(), pref.getValue(), true));
    		}
    	}
	}


	class MigratePropertyCallback extends JournalizedTransactionCallback
    {
		private final Node node;
		private final QName identifier;
		private final Serializable value;
		private final boolean isPreference;

        MigratePropertyCallback(final MigrationTracer journal, final Node node, final QName identifier, final Serializable value, final boolean isPreference)
        {
        	super(journal);
        	this.node = node;
        	this.identifier = identifier;
        	this.value = value;
        	this.isPreference = isPreference;
        }

        public String execute() throws Throwable
        {
        	final NodeRef nodeRef = ElementsHelper.getNodeRef(node);

        	if(FORBIDEN_PROPERTIES.contains(identifier))
        	{
        		// theses properties should be setted in an other class
        	}
        	else if(nodeRef == null)
        	{
        		// the node creation probably fail
        	}
        	else
        	{
        		final UpdateOperation operation = (isPreference) ? UpdateOperation.SET_PREFERENCE : UpdateOperation.SET_PROPERTY;
            	boolean wasAware = MLPropertyInterceptor.setMLAware(true);
            	getPolicyBehaviourFilter().disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

    			try
    			{
    				Serializable oldValue;
    				if(isPreference)
    				{
    					oldValue = getUserService().getPreference(nodeRef, identifier);
    				}
    				else
    				{
    					oldValue = getNodeService().getProperty(nodeRef, identifier);
    				}

    				if(isMLTextProperty(identifier))
    				{
    					checkForMLTextValues(oldValue, value);
    				}

    				if(oldValue == null || !(oldValue.equals(value)))
    				{
    					if(isPreference)
    					{
    						getUserService().setPreference(nodeRef, identifier, value);
    					}
    					else
    					{
    						getNodeService().setProperty(nodeRef, identifier, value);
    					}

    					final Map<JournalLine.Parameter, Serializable> parameters = new HashMap<JournalLine.Parameter, Serializable>(2);
    					parameters.put(JournalLine.Parameter.QNAME, identifier);
    					parameters.put(JournalLine.Parameter.OLD_VALUE, oldValue == null ? "" : oldValue);
    					parameters.put(JournalLine.Parameter.NEW_VALUE, value == null ? "" : value);

    					getJournal().journalize(JournalLine.updateNode(
    							Status.SUCCESS,
    							ElementsHelper.getQualifiedPath(node),
    							nodeRef.toString(),
    							operation,
    							parameters
    						));

    				}
    				else
    				{
    					getReport().appendSection(ElementsHelper.getQualifiedPath(node)
    							+ (isPreference ?  " preference " : " property ")
    							+ identifier + " already setted as " + value);
    				}
    			}
    			catch(Throwable t)
    			{
    				getReport().appendSection("Impossible to set " + (isPreference ?  " preference " : " property ")
    						+ identifier + ":" + value + " to " + ElementsHelper.getQualifiedPath(node)
    						+ ". " + t.getMessage());

    				if(isFailOnError())
    				{
    					throw t;
    				}
    				else
    				{
    					getJournal().journalize(JournalLine.updateNode(
    											Status.FAIL,
    											ElementsHelper.getQualifiedPath(node),
    											operation,
    		    								JournalLine.Parameter.NEW_VALUE,
    		    								identifier + ":" + value
    						));
    				}
    			}
    			finally
    			{
    				MLPropertyInterceptor.setMLAware(wasAware);
    				getPolicyBehaviourFilter().enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    			}
        	}



			return null;
		}

    }

	class MigratePropertiesCallback extends JournalizedTransactionCallback
    {
		private final Node node;
		final PropertyMap properties;

		MigratePropertiesCallback(final MigrationTracer journal, final Node node, final PropertyMap properties)
        {
        	super(journal);
        	this.node = node;
        	this.properties = properties;
        }

        public String execute() throws Throwable
        {
        	final NodeRef nodeRef = ElementsHelper.getNodeRef(node);

        	if(nodeRef == null)
        	{
        		// the node creation probably fail
        		return null;
        	}
        	else
        	{
        		boolean wasAware = MLPropertyInterceptor.setMLAware(true);
        		getPolicyBehaviourFilter().disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        		try
    			{
        			final Map<QName, Serializable> oldProperties = getNodeService().getProperties(nodeRef);
            		final Set<QName> keysToRemove = new HashSet<QName>();
            		for(final QName key: this.properties.keySet())
            		{
            			if(FORBIDEN_PROPERTIES.contains(key))
                    	{
                    		// theses properties should be setted in an other class
            				keysToRemove.add(key);
                    	}
                    	else
                    	{	final Serializable newValue = this.properties.get(key);
    						final Serializable oldValue = oldProperties.get(key);

    						if(newValue == null)
    						{
    							keysToRemove.add(key);
    						}
    						else
    						{
    							if(isMLTextProperty(key))
    							{
    								checkForMLTextValues(oldValue, newValue);
    							}

    							if(oldValue != null && oldValue.equals(newValue))
    							{
    								keysToRemove.add(key);
    							}
    						}
                    	}
            		}

            		for(final QName key: keysToRemove)
            		{
            			this.properties.remove(key);
            		}

    				if(properties.size() > 0)
    				{
    					getNodeService().addProperties(nodeRef, this.properties);

    					for(final Map.Entry<QName, Serializable> entry: this.properties.entrySet())
    					{
    						final Map<JournalLine.Parameter, Serializable> parameters = new HashMap<JournalLine.Parameter, Serializable>(2);
        					parameters.put(JournalLine.Parameter.QNAME, entry.getKey());
        					parameters.put(JournalLine.Parameter.OLD_VALUE, oldProperties.get(entry.getKey()));
        					parameters.put(JournalLine.Parameter.NEW_VALUE, entry.getValue());

        					getJournal().journalize(JournalLine.updateNode(
        							Status.SUCCESS,
        							ElementsHelper.getQualifiedPath(node),
        							nodeRef.toString(),
        							UpdateOperation.SET_PROPERTY,
        							parameters
        						));
    					}
    				}
    			}
    			catch(Throwable t)
    			{
    				getReport().appendSection("Impossible to set properties " + this.properties
    						 + " to " + ElementsHelper.getQualifiedPath(node) + ". " + t.getMessage());

    				if(isFailOnError())
    				{
    					throw t;
    				}
    				else
    				{
    					getJournal().journalize(JournalLine.updateNode(
    											Status.FAIL,
    											ElementsHelper.getQualifiedPath(node),
    											UpdateOperation.SET_PROPERTIES,
    		    								JournalLine.Parameter.NEW_VALUE,
    		    								this.properties
    						));
    				}
    			}
    			finally
    			{
    				MLPropertyInterceptor.setMLAware(wasAware);
    				getPolicyBehaviourFilter().enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    			}
        	}

			return null;
		}
    }

	class MigrateDynamicPropertyCallback extends JournalizedTransactionCallback
    {
        protected final InterestGroup interestGroup;
        protected final DynamicPropertyDefinitions dynamicPropertyDefinitions;

        private final DynamicPropertyService dynamicPropertyService;

        public MigrateDynamicPropertyCallback(final MigrationTracer journal, final InterestGroup interestGroup, final DynamicPropertyDefinitions dynamicPropertyDefinitions)
        {
        	super(journal);
        	this.interestGroup = interestGroup;
        	this.dynamicPropertyDefinitions = dynamicPropertyDefinitions;
        	this.dynamicPropertyService = getRegistry().getDynamicPropertieService();
        }

        public String execute() throws Throwable
        {
			final NodeRef igNodeRef = ElementsHelper.getNodeRef(interestGroup);
			final List<DynamicProperty> existingProperties = dynamicPropertyService.getDynamicProperties(igNodeRef);

			final List<DynamicPropertyDefinition> importedDynamicProperties = dynamicPropertyDefinitions.getDefinitions();

			for(final DynamicPropertyDefinition definition : importedDynamicProperties)
			{
				if(indexAlreadyCreated(definition.getId(), existingProperties))
				{
					getReport().appendSection(ElementsHelper.getXPath(definition) + " already created. A dynamic property with the index " + definition.getId() + " is defined in the Interest Group.");
				}
				else
				{
					try
					{
						final DynamicProperty property = ElementsConverter.convertDynamicProperty(definition);
						final DynamicProperty createdProperty = dynamicPropertyService.addDynamicProperty(igNodeRef, property);

						getJournal().journalize(JournalLine.updateNode(
		    								Status.SUCCESS,
		    								ElementsHelper.getQualifiedPath(interestGroup),
		    								igNodeRef.toString(),
		    								JournalLine.UpdateOperation.ADD_DYNAMIC_PROPERTY_DEFINITION,
		    								Collections.singletonMap(JournalLine.Parameter.DEFINITION, (Serializable) createdProperty)
		    						));

						getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated with dynamic property " + ElementsHelper.getXPath(definition));
					}
					catch(Throwable t)
					{
						getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(interestGroup) + "with dynamic property " + ElementsHelper.getXPath(definition)+ ". " + t.getMessage());

						if(isFailOnError())
						{
							throw t;
						}
						else
						{
							getJournal().journalize(JournalLine.updateNode(
													Status.FAIL,
													ElementsHelper.getQualifiedPath(interestGroup),
													JournalLine.UpdateOperation.ADD_DYNAMIC_PROPERTY_DEFINITION,
				    								JournalLine.Parameter.XPATH,
				    								ElementsHelper.getXPath(definition)
								));
						}
					}
				}
			}

			return null;
		}

      	private boolean indexAlreadyCreated(int index, final List<DynamicProperty> properties)
        {
        	boolean found = false;

        	for (final DynamicProperty property : properties)
			{
				if(property.getIndex() == index)
				{
					found = true;
					break;
				}
			}

        	return found;
        }
    }

	class MigrateKeywordCallback extends JournalizedTransactionCallback
    {
        protected final InterestGroup interestGroup;
        protected final KeywordDefinitions keywordDefinitions;
        private final KeywordsService keywordsService;

        public MigrateKeywordCallback(final MigrationTracer journal, final InterestGroup interestGroup, final KeywordDefinitions keywordDefinitions)
        {
        	super(journal);
        	this.interestGroup = interestGroup;
        	this.keywordDefinitions = keywordDefinitions;
        	this.keywordsService = getRegistry().getKeywordsService();
        }

        public String execute() throws Throwable
        {
			final NodeRef igNodeRef = ElementsHelper.getNodeRef(interestGroup);
			final List<KeywordDefinition> importedKeywords = keywordDefinitions.getDefinitions();

			for(final KeywordDefinition definition : importedKeywords)
			{
				try
				{
					final Keyword keyword = ElementsConverter.convertKeywords(definition);

					if(getKeywordWithValues(keywordsService, igNodeRef, keyword.getMLValues()) != null)
					{
						getReport().appendSection(ElementsHelper.getXPath(definition) + " already created. A keyword definition with values " +  keyword.getMLValues() + " is defined in the Interest Group.");
					}
					else
					{
						final Keyword createdKeyword = keywordsService.createKeyword(igNodeRef, keyword);

						getJournal().journalize(JournalLine.updateNode(
		    								Status.SUCCESS,
		    								ElementsHelper.getQualifiedPath(interestGroup),
		    								igNodeRef.toString(),
		    								JournalLine.UpdateOperation.ADD_KEYWORD_DEFINITION,
		    								Collections.singletonMap(JournalLine.Parameter.DEFINITION, (Serializable) createdKeyword)
		    						));

						getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated with keyword " + ElementsHelper.getXPath(definition));
					}
				}
				catch(Throwable t)
				{
					getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(interestGroup) + " with keyword definition " + ElementsHelper.getXPath(definition)+ ". " + t.getMessage());

					if(isFailOnError())
					{
						throw t;
					}
					else
					{
						getJournal().journalize(JournalLine.updateNode(
												Status.FAIL,
												ElementsHelper.getQualifiedPath(interestGroup),
												JournalLine.UpdateOperation.ADD_KEYWORD_DEFINITION,
			    								JournalLine.Parameter.XPATH,
			    								ElementsHelper.getXPath(definition)
							));
					}
				}
			}

			return null;
		}
    }

	class MigrateKeywordReferenceCallback extends JournalizedTransactionCallback
    {
		private final Node node;
		private final KeywordReferences keywords;
		private final KeywordsService keywordsService;

        public MigrateKeywordReferenceCallback(final MigrationTracer journal, final Node node, final KeywordReferences keywords)
        {
        	super(journal);
        	this.node = node;
        	this.keywords = keywords;
        	this.keywordsService = getRegistry().getKeywordsService();
        }

        public String execute() throws Throwable
        {
        	try
			{
				final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(node);
	        	final NodeRef igNodeRef = ElementsHelper.getNodeRef(interestGroup);
	        	final NodeRef nodeRef = ElementsHelper.getNodeRef(node);
	        	final List<KeywordDefinition> keywordDefinition = interestGroup.getKeywordDefinitions().getDefinitions();
	        	final List<NodeRef> settedKeywords = (List<NodeRef>) getNodeService().getProperty(nodeRef, DocumentModel.PROP_KEYWORD);
	        	final List<NodeRef> newKeywords = new ArrayList<NodeRef>();

	        	if(settedKeywords != null)
	        	{
	        		newKeywords.addAll(settedKeywords);
	        	}

	        	for(final Integer keywordId : keywords.getIds())
	        	{
	        		final KeywordDefinition definitionById = getDefinitionById(keywordDefinition, keywordId);
	        		final NodeRef keywordRef = getKeywordRef(igNodeRef, definitionById);

	        		if(newKeywords.contains(keywordRef))
	        		{
	        			getReport().appendSection(ElementsHelper.getXPath(definitionById) + " already setted to node " + ElementsHelper.getQualifiedName(node));
	        		}
	        		else
	        		{
	        			newKeywords.add(keywordRef);
	        		}
	        	}

	        	if(!newKeywords.equals(settedKeywords))
	        	{
	        		getNodeService().setProperty(nodeRef, DocumentModel.PROP_KEYWORD, (Serializable) newKeywords);

	        		final Map<JournalLine.Parameter, Serializable> parameters = new HashMap<JournalLine.Parameter, Serializable>(2);
					parameters.put(JournalLine.Parameter.OLD_VALUE, (Serializable) settedKeywords);
					parameters.put(JournalLine.Parameter.NEW_VALUE, (Serializable) newKeywords);

					getJournal().journalize(JournalLine.updateNode(
							Status.SUCCESS,
							ElementsHelper.getQualifiedPath(node),
							nodeRef.toString(),
							JournalLine.UpdateOperation.SET_PROPERTY,
							parameters
						));

					getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " successfully updated with keywords " + newKeywords);
	        	}
	        	else
	        	{
	        		getReport().appendSection("No keyword to add to " + ElementsHelper.getQualifiedName(node));
	        	}

			}
        	catch(Throwable t)
			{
				getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(node) + " keyword references. " + t.getMessage());

				if(isFailOnError())
				{
					throw t;
				}
				else
				{
					getJournal().journalize(JournalLine.updateNode(
											Status.FAIL,
											ElementsHelper.getQualifiedPath(node),
											JournalLine.UpdateOperation.SET_PROPERTY,
		    								JournalLine.Parameter.XPATH,
		    								ElementsHelper.getXPath(keywords)
						));
				}
			}

			return null;
		}

        private NodeRef getKeywordRef(NodeRef igNodeRef, KeywordDefinition keywordDef)
        {
    		final Keyword convertedKeyword = ElementsConverter.convertKeywords(keywordDef);
    		final Keyword targetExistingKeyword = getKeywordWithValues(this.keywordsService, igNodeRef, convertedKeyword.getMLValues());
    		return targetExistingKeyword.getId();
        }

        private KeywordDefinition getDefinitionById(final List<KeywordDefinition> definitions, final Integer id)
        {
        	KeywordDefinition foundDefinition = null;

        	for(KeywordDefinition keywordDefinition : definitions)
        	{
        		if(keywordDefinition.getId().equals(id))
        		{
        			foundDefinition = keywordDefinition;
        			break;
        		}
        	}

        	return foundDefinition;
        }
    }

	private Keyword getKeywordWithValues(final KeywordsService keywordsService, final NodeRef igNoderef, final MLText values)
	{
		Keyword foundKeyword = null;
		final List<Keyword> keywords = keywordsService.getKeywords(igNoderef);

		for(Keyword keyword : keywords)
		{
			if(values.equals(keyword.getMLValues()))
			{
				foundKeyword = keyword;
				break;
			}
		}

		return foundKeyword;
	}

	 private boolean isMLTextProperty(final QName identifier)
     {
     	return ML_TEXT_PROPERTIES.contains(identifier);
     }

     private Locale defaultLocale = new Locale(".default");

     private void checkForMLTextValues(final Serializable oldValue, final Serializable newValue)
     {
     	if(newValue != null && oldValue != null && oldValue.toString().length() > 0)
			{
     		if(oldValue instanceof MLText && newValue instanceof MLText)
     		{
     			final MLText newMLValue = (MLText) newValue;
     			final MLText oldMLValue = (MLText) oldValue;

         		for(final Map.Entry<Locale, String> entry : oldMLValue.entrySet())
 				{
 					if(!defaultLocale.equals(entry.getKey()) && !newMLValue.containsKey(entry.getKey()) && entry.getValue() != null && entry.getValue().trim().length() > 0)
 					{
 						newMLValue.addValue(entry.getKey(), entry.getValue());
 					}
 				}
     		}
			}
     }

     class MigrateLogosCallback extends MigrateContentCallback
     {
         protected final InterestGroup interestGroup;
         protected final LogoDefinitions logoDefinitions;
         private final LogoPreferencesService logoPreferencesService;
         private final Set<String> circaDefaultsLogos = new HashSet<String>(Arrays.asList("ig_logo.jpg", "ig_logo.gif", "index-logo.gif"));
         
          

         public MigrateLogosCallback(final MigrateContents migrateContents, final MigrationTracer journal, final InterestGroup interestGroup, final LogoDefinitions logoDefinitions)
         {
        	 migrateContents.super(journal, interestGroup, null);
         	 this.interestGroup = interestGroup;
         	 this.logoDefinitions = logoDefinitions;
         	 this.logoPreferencesService = getRegistry().getLogoPreferencesService();
         }

         @Override
         public String execute() throws Throwable
         {
 			final NodeRef igNodeRef = ElementsHelper.getNodeRef(interestGroup);
 			final List<Logo> logos = logoDefinitions.getLogos();
 			String defaultLogoName = logoDefinitions.getDefaultLogo();

 			for(final Logo logo: logos)
 			{
 				try
 				{
 					if (circaDefaultsLogos.contains(logo.getName()))
 					{
 						continue;
 					}
 					logoPreferencesService.addLogo(igNodeRef, (String) logo.getName(), getInpuStreamFromResource(logo.getUri()));

					getJournal().journalize(JournalLine.updateNode(
	    								Status.SUCCESS,
	    								ElementsHelper.getQualifiedPath(interestGroup),
	    								igNodeRef.toString(),
	    								JournalLine.UpdateOperation.ADD_LOGO,
	    								Collections.singletonMap(JournalLine.Parameter.TARGET, (Serializable) logo.getUri())
	    						));

					getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated with logo " + ElementsHelper.getXPath(logo));
 				}
 				catch(Throwable t)
 				{
 					getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(interestGroup) + " with logo definition " + ElementsHelper.getXPath(logo)+ ". " + t.getMessage());

 					if(isFailOnError())
 					{
 						throw t;
 					}
 					else
 					{
 						getJournal().journalize(JournalLine.updateNode(
 												Status.FAIL,
 												ElementsHelper.getQualifiedPath(interestGroup),
 												JournalLine.UpdateOperation.ADD_LOGO,
 			    								JournalLine.Parameter.XPATH,
 			    								ElementsHelper.getXPath(logo)
 							));
 					}
 				}
 			}

 			if(defaultLogoName != null && defaultLogoName.length() > 0)
 			{
 				try
 				{
 					if (circaDefaultsLogos.contains(defaultLogoName))
 					{
 						defaultLogoName = "circabc_logo.gif";
 					}
 					final List<LogoDefinition> repoLogos = logoPreferencesService.getAllLogos(igNodeRef);
 					LogoDefinition defaultLogo = null;

 					for(final LogoDefinition def: repoLogos)
 					{
 						if(def.getName().equals(defaultLogoName))
 						{
 							defaultLogo = def;
 							break;
 						}
 					}

 					if(defaultLogo != null)
 					{
 						logoPreferencesService.setDefault(igNodeRef, defaultLogo.getReference());

 						final DefaultLogoConfiguration rootConfig = logoPreferencesService.getDefault(igNodeRef);

 						// display it by applying the default config
 						logoPreferencesService.setMainPageLogoConfig(igNodeRef, true,
 								rootConfig.getMainPageLogoHeight(),
 								rootConfig.getMainPageLogoWidth(),
 								rootConfig.isMainPageSizeForced(),
 								rootConfig.isMainPageLogoAtLeft());

 						logoPreferencesService.setOtherPagesLogoConfig(igNodeRef, true,
 								rootConfig.getOtherPagesLogoHeight(),
 								rootConfig.getOtherPagesLogoWidth(),
 								rootConfig.isOtherPagesSizeForced());

 						getJournal().journalize(JournalLine.updateNode(
								Status.SUCCESS,
								ElementsHelper.getQualifiedPath(interestGroup),
								igNodeRef.toString(),
								JournalLine.UpdateOperation.ADD_LOGO,
								Collections.singletonMap(JournalLine.Parameter.TARGET, (Serializable) defaultLogo.getReference())
						));

 						getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated with logo " + ElementsHelper.getXPath(logoDefinitions));
 					}
 					else
 					{
 						reportError(defaultLogoName, "Logo must be created first", null);
 					}

 				}
 				catch(Throwable t)
 				{
 					reportError(defaultLogoName, t.getMessage(), t);
 				}
 			}

 			return null;
 		}

		private void reportError(final String defaultLogoName, final String message, final Throwable t) throws Throwable
		{
			getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(interestGroup) + " with default logo definition " + defaultLogoName + ". " + message);

			if(isFailOnError())
			{
				if(t == null)
				{
					throw new CustomizationException(message);
				}
				else
				{
					throw t;
				}
			}
			else
			{
				getJournal().journalize(JournalLine.updateNode(
										Status.FAIL,
										ElementsHelper.getQualifiedPath(interestGroup),
										JournalLine.UpdateOperation.SET_DEFAULT_LOGO,
										JournalLine.Parameter.XPATH,
										ElementsHelper.getXPath(logoDefinitions)
					));
			}
		}
     }

    private MigrateContents getMigrateContents()
 	{
 		final MigrateContents migrateContents = new MigrateContents(importRoot, getJournal(), getRegistry());
 		return migrateContents;
 	}
}
