/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.logs;


import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;

import eu.cec.digit.circabc.repo.migration.ReflexionUtils;


/**
 * Class in charge to synchronize a folder with a war / ear resource list.
 *
 * @author Yanick Pignot
 *
 */
public class CircaLogConfig
{
    private static final String CONFIG_AREA = "circa-logfiles-configuration";
    private static final String CONFIG_CONDITION = "Parse definition";

    private static final String ELEM_PATH_PARSERS = "pathParsers";
    private static final String ELEM_INFO_PARSERS = "infoParsers";
    private static final String ELEM_PARSER  = "parser";

    private static final String ELEM_LOG_ENTRIES = "logEntriesConfiguration";
    private static final String ELEM_ENTRY = "entry";
    private static final String ELEM_KEY = "key";
    private static final String ELEM_PATH_PARSER = "pathParser";
    private static final String ELEM_INFO_PARSER = "infoParser";
    private static final String ELEM_SERVICE = "service";
    private static final String ELEM_ACTIVITY = "activity";
    private static final String ELEM_SUCCESS = "success";

    private static final String ATTR_NAME = "name";

    private Map<String, CircaPathParser> pathParsers = new HashMap<String, CircaPathParser>();
    private Map<String, CircaInfoParser> infoParsers = new HashMap<String, CircaInfoParser>();
    private Map<String, LogEntryConfiguration> entryConfiguration = new HashMap<String, LogEntryConfiguration>();

    private ConfigService configService;

    public void init()
    {
       final ConfigLookupContext clContext = new ConfigLookupContext(CONFIG_AREA);
       final Config configConditions = configService.getConfig(CONFIG_CONDITION, clContext);

       final ConfigElement pathParsersElements = configConditions.getConfigElement(ELEM_PATH_PARSERS);
       final ConfigElement infoParsersElements = configConditions.getConfigElement(ELEM_INFO_PARSERS);
       final ConfigElement entriesParsersElements = configConditions.getConfigElement(ELEM_LOG_ENTRIES);

       setParsers(pathParsersElements, pathParsers);
       setParsers(infoParsersElements, infoParsers);
       setEntries(entriesParsersElements);
    }

    public LogEntryConfiguration getLogEntryConfig(final String configKey)
    {
    	ParameterCheck.mandatoryString("Configuration key", configKey);
    	return entryConfiguration.get(configKey);
    }

	@SuppressWarnings("unchecked")
	private <T> void setParsers(final ConfigElement parsersElements, final Map<String, T> parsers)
    {
		String name;
		T parser;

    	for(final ConfigElement config: parsersElements.getChildren(ELEM_PARSER))
    	{
    		name = config.getAttribute(ATTR_NAME);

    		if(parsers.containsKey(name))
    		{
    			throw new ConfigException("Duplicate parser name not allowed: " + name);
    		}
    		else
    		{
    			try
    			{
    				parser = ReflexionUtils.<T>buildValidator(config.getValue());
    				parsers.put(name, parser);
    			}
    			catch(Exception e)
    			{
    				throw new ConfigException(e.getMessage());
    			}
    		}
    	}
    }

	private void setEntries(ConfigElement entriesParsersElements)
	{
		String key;
		String pathParser;
		String infoParser;
		String service;
		String activity;
		String success;

		for(final ConfigElement config: entriesParsersElements.getChildren(ELEM_ENTRY))
    	{
			key = config.getChildValue(ELEM_KEY);
			pathParser = config.getChildValue(ELEM_PATH_PARSER);
			infoParser = config.getChildValue(ELEM_INFO_PARSER);
			service = config.getChildValue(ELEM_SERVICE);
			activity = config.getChildValue(ELEM_ACTIVITY);
			success = config.getChildValue(ELEM_SUCCESS);

			checkEmptyString(key, ELEM_KEY);
			checkEmptyString(pathParser, ELEM_PATH_PARSER);
			checkEmptyString(infoParser, ELEM_INFO_PARSER);
			checkEmptyString(service, ELEM_SERVICE);
			checkEmptyString(activity, ELEM_ACTIVITY);
			checkEmptyString(success, ELEM_SUCCESS);

			if(pathParsers.containsKey(pathParser) == false)
    		{
    			throw new ConfigException("Path parser not defined: " + pathParser);
    		}
			if(infoParsers.containsKey(infoParser) == false)
    		{
    			throw new ConfigException("Info parser not defined: " + infoParser);
    		}

			entryConfiguration.put(key, new LogEntryConfiguration(
					key,
					pathParsers.get(pathParser),
					infoParsers.get(infoParser),
					service,
					activity,
					Boolean.valueOf(success))
			);
    	}
	}


	private void checkEmptyString(final String str, final String message)
	{
		if(str == null || str.trim().length() < 1)
		{
			throw new ConfigException("The " + message + " element definition is mandatory.");
		}
	}

    public final void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }
}
