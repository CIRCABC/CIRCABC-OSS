/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Logo;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynPropertyType;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaIGConfigReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.properties.SetCircaMetadataVisitor;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.migration.walker.XMLNodesVisitor;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ExportationException;
/**
 * Concrete metadata reader for circa. Can read either metadata in
 * a database, either the target circa installation system.
 *
 * @author Yanick Pignot
 */
public class CircaMetadataReaderImpl implements MetadataReader
{
	private static final Log logger = LogFactory.getLog(CircaMetadataReaderImpl.class);

    String circaDomainPrefix;
    private FileClient fileClient;
    private CircaIGConfigReader configReader;
    private CircabcServiceRegistry circabcServiceRegistry;

    private static final String BACKUP_EXTENSION = ".backup";
    private static final String KEYWORDS_FOLDER = "/.keywords";
    private static final String DYN_ATTRIBUTES_FILE = "/lib_attributes.cfg";
    private static final String DELIM = "\r\n";

    private static final String COMMENT_LINE_START = "#";
    private static final String DYN_ATTR_COL_REGEX = "attr[1-5]";
    private static final String ATTRIBUTE_COLUMN = "ATTRIBUTE";
    private static final String TYPE_COLUMN = "TYPE";
    private static final String DATA_COLUMN = "DATA";

    private static final String[] CIRCA_SELECTION_CASES = {"textfield", "textarea", "datefield", "selection"};

    public void setDynamicPropertyDefinition(final InterestGroup interestGroup)throws ExportationException
    {
    	DynamicPropertyDefinitions definitions = interestGroup.getDynamicPropertyDefinitions();
    	if(definitions == null)
    	{
    		interestGroup.withDynamicPropertyDefinitions((definitions = new DynamicPropertyDefinitions()));

    		if(logger.isDebugEnabled())
    		{
    			logger.debug("  ---  No dynamic attribute defined");
    		}
    	}

    	final String igPath = ElementsHelper.getExportationPath(interestGroup);
    	final StringTokenizer tokens = new StringTokenizer(fileClient.downloadAsString(igPath + DYN_ATTRIBUTES_FILE), DELIM, false);

    	String line;
    	List<Locale> langs = null;
    	List<String> words;

    	final List<Integer> settedAttr = new ArrayList<Integer>(5);

		while(tokens.hasMoreTokens())
		{
			line = tokens.nextToken();

			if(!line.startsWith(COMMENT_LINE_START) && line.length() > 0)
			{
				words = getWords(line);
				final int wordCount = words.size();

				if(langs == null)
				{
					// must be first line
					if(words.get(0).equals(ATTRIBUTE_COLUMN)
							&& words.get(1).equals(TYPE_COLUMN)
							&& words.get(wordCount - 1).equals(DATA_COLUMN))
					{
						langs = new ArrayList<Locale>(wordCount - 3);
						for(int x = 2; x < wordCount - 1; ++x)
						{
							langs.add(I18NUtil.parseLocale(words.get(x)));
						}
					}
					else
					{
						throw new ExportationException("Impossible to read the library attribute file of the Interest Group. The first line must be [ATTRIBUTE, TYPE, (lang ISO Code,)* DATA]" );
					}
				}
				else if(words.get(0).matches(DYN_ATTR_COL_REGEX))
				{
					final String name = words.get(0);
					final String type = words.get(1);
					final String values = words.get(wordCount - 1);

					final DynamicPropertyDefinition definition = new DynamicPropertyDefinition();

					int idx = Integer.valueOf(name.substring(name.length() - 1));

					// don't set two dynamic property with the same index
					if(settedAttr.contains(idx) == false)
					{
						settedAttr.add(idx);

						definition.withId(idx);

						if(type.equalsIgnoreCase(CIRCA_SELECTION_CASES[1]))
						{
							definition.withType(DynPropertyType.TEXT_AREA);
						}
						else if(type.equalsIgnoreCase(CIRCA_SELECTION_CASES[2]))
						{
							definition.withType(DynPropertyType.DATE_FIELD);
						}
						else if(type.equalsIgnoreCase(CIRCA_SELECTION_CASES[3]))
						{
							definition.withType(DynPropertyType.SELECTION);

							final StringTokenizer valueTokens = new StringTokenizer(values, ",", false);
							while(valueTokens.hasMoreTokens())
							{
								definition.withSelectionCases(valueTokens.nextToken().trim());
							}
						}
						else
						{
							definition.withType(DynPropertyType.TEXT_FIELD);
						}

						for(int x = 0; x < langs.size(); ++x)
						{
							if(x < wordCount - 2)
							{
								definition.withI18NValues(new I18NProperty(langs.get(x), words.get(x + 2)));
							}
						}

						if(logger.isDebugEnabled())
						{
							logger.debug("  ---  With dynamic attribute:");
							logger.debug("  -----  Index: " + definition.getId());
							logger.debug("  -----  Type:  " + definition.getType().value());
							logger.debug("  -----  Name:  " + definition.getI18NValues());
						}

						definitions.withDefinitions(definition);
					}
				}
			}
		}
    }

    public void setKeywordDefinition(final InterestGroup interestGroup)throws ExportationException
    {
    	KeywordDefinitions definitions = interestGroup.getKeywordDefinitions();
    	if(definitions == null)
    	{
    		interestGroup.withKeywordDefinitions((definitions = new KeywordDefinitions()));

    		if(logger.isDebugEnabled())
    		{
    			logger.debug("  ---  No keyword attribute defined");
    		}
    	}
    	String fileName;
    	StringTokenizer tokens;
    	int counter = 0;
        for(final String path : fileClient.list(ElementsHelper.getExportationPath(interestGroup) + KEYWORDS_FOLDER, false, true, false))
        {
        	if((fileName = FilePathUtils.retreiveFileName(path)).endsWith(BACKUP_EXTENSION) == false)
        	{
        		tokens = new StringTokenizer(fileClient.downloadAsString(path), DELIM, false);
        		while(tokens.hasMoreTokens())
        		{
        			final KeywordDefinition keywordDef = new KeywordDefinition(singleI18NProperty(fileName, tokens.nextToken()), null, counter);

        			definitions.withDefinitions(keywordDef);

        			if(logger.isDebugEnabled())
					{
						logger.debug("  ---  With Keyword attribute:" );
						logger.debug("  -----  Index: " + keywordDef.getId());
						logger.debug("  -----  Name:  " + keywordDef.getI18NValues());
					}

        			++counter;
        		}
        	}
        }
    }

    public void setProperties(final XMLNode node) throws ExportationException
    {
    	final CircaClientsRegistry registry = CircaClientsRegistry.getInstance(circabcServiceRegistry);
        final XMLNodesVisitor visitor = new SetCircaMetadataVisitor(circaDomainPrefix, registry);

        try
        {
        	node.accept(visitor);
        }
        catch(final Exception e)
        {
            throw new ExportationException(e);
        }

    }

   	public void setIconsDefinition(final InterestGroup interestGroup) throws ExportationException
	{
   		final String interestGroupPath = ElementsHelper.getExportationPath(interestGroup);
   		final List<String> icons = configReader.getInterestgroupIconsPath(interestGroupPath);
   		final String defaultIcon = configReader.getInterestgroupDefaultIconPath(interestGroupPath);

   		if(icons.size() > 0 || defaultIcon != null)
   		{
   			final LogoDefinitions logoDef = new LogoDefinitions();
   			interestGroup.setLogoDefinitions(logoDef);

   			if(defaultIcon != null && icons.contains(defaultIcon))
   			{
				final String iconName = computeIconName(defaultIcon);
				logoDef.setDefaultLogo(iconName);

   				if(logger.isDebugEnabled())
   	    		{
   	    			logger.debug("  ---  Icon displayed by default: " + iconName);
   	    		}
   			}
   			else
   			{
   				if(logger.isDebugEnabled())
   	    		{
   	    			logger.debug("  ---  No default icon to displayed. ");
   	    		}
   			}

   			for(final String path: icons)
   			{
   				logoDef.withLogos(new Logo(computeIconName(path), fileClient.generateResouceString(path)));

   				if(logger.isDebugEnabled())
   	    		{
   	    			logger.debug("  ---  Logo added with path: " + path);
   	    		}
   			}
   		}
   		else
   		{
   			if(logger.isDebugEnabled())
    		{
    			logger.debug("  ---  No icon defined. ");
    		}
   		}
	}

   	private String computeIconName(final String iconPath)
   	{
   		return FilePathUtils.retreiveFileName(iconPath);
   	}

    /**
     * @param fileClient the fileClient to set
     */
    public final void setFileClient(FileClient fileClient)
    {
        this.fileClient = fileClient;
    }

    /**
     * @param circaInstallationDomainPrefix the circaInstallationDomainPrefix to set
     */
    public final void setCircaInstallationDomainPrefix(String circaInstallationDomainPrefix)
    {
        this.circaDomainPrefix = circaInstallationDomainPrefix;
    }


    /**
     * @param circaDomainPrefix the circaDomainPrefix to set
     */
    public final void setCircaDomainPrefix(String circaDomainPrefix)
    {
        this.circaDomainPrefix = circaDomainPrefix;
    }


    private final List<I18NProperty> singleI18NProperty(String lang, String value)
    {
    	return Collections.singletonList(new I18NProperty(I18NUtil.parseLocale(lang), value));
    }


    private static final char SPACE = ' ';
    private static final char TAB = '\t';
    private static final char QUOTE = '"';

    private List<String> getWords(String line)
	{
		final List<String> words = new ArrayList<String>();
		StringBuilder builder = null;
		final int lineLen = line.length();
    	final char[] chars = new char[lineLen];
    	line.getChars(0, line.length(), chars, 0);

    	boolean startToFill = false;
    	boolean spacesAllowed = false;
    	for(int count = 0; count < lineLen; ++count)
    	{
    		char c = chars[count];
    		if(startToFill == false && c != SPACE && c != TAB)
    		{
    			builder = new StringBuilder();
    			startToFill = true;
    			spacesAllowed = c == QUOTE;

    			if(spacesAllowed == false)
    			{
    				builder.append(c);
    			}
    		}
    		else if(startToFill)
    		{
    			if(!(spacesAllowed == false && (c == SPACE || c == TAB)) && c != QUOTE)
    			{
    				builder.append(c);

    				// last iteration, add the word
    				if(count == lineLen - 1)
    				{
    					words.add(builder.toString());
    				}
    			}
    			else
    			{
    				words.add(builder.toString());
    				builder = null;
    				startToFill = false;
    			}
    		}
    	}

    	return words;
	}

	/**
	 * @param circabcServiceRegistry the circabcServiceRegistry to set
	 */
	public final void setCircabcServiceRegistry(CircabcServiceRegistry circabcServiceRegistry)
	{
		this.circabcServiceRegistry = circabcServiceRegistry;
	}

	/**
	 * @param configReader the configReader to set
	 */
	public final void setConfigReader(CircaIGConfigReader configReader)
	{
		this.configReader = configReader;
	}

}