/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.logs;

import java.io.Serializable;

import org.alfresco.util.ParameterCheck;

/**
 * @author Yanick Pignot
 *
 */
public class LogEntryConfiguration implements Serializable
{
	/** */
	private static final long serialVersionUID = 2824754285427274820L;
	private final String key;
    private final CircaPathParser pathParser;
    private final CircaInfoParser infoParser;
    private final String service;
    private final String activity;
    private final Boolean success;

    LogEntryConfiguration(final String key, final CircaPathParser pathParser, final CircaInfoParser infoParser, final String service, final String activity, final Boolean success)
	{
    	super();

    	ParameterCheck.mandatoryString("key", key);
    	ParameterCheck.mandatoryString("service", service);
    	ParameterCheck.mandatoryString("activity", activity);
    	ParameterCheck.mandatory("pathParser", pathParser);
    	ParameterCheck.mandatory("infoParser", infoParser);
    	ParameterCheck.mandatory("success", success);

		this.key = key;
		this.pathParser = pathParser;
		this.infoParser = infoParser;
		this.service = service;
		this.activity = activity;
		this.success = success;
	}
	/**
	 * @return the activity
	 */
	public final String getActivity()
	{
		return activity;
	}
	/**
	 * @return the infoParser
	 */
	public final CircaInfoParser getInfoParser()
	{
		return infoParser;
	}
	/**
	 * @return the key
	 */
	public final String getKey()
	{
		return key;
	}
	/**
	 * @return the pathParser
	 */
	public final CircaPathParser getPathParser()
	{
		return pathParser;
	}
	/**
	 * @return the service
	 */
	public final String getService()
	{
		return service;
	}
	/**
	 * @return the success
	 */
	public final Boolean getSuccess()
	{
		return success;
	}

}
