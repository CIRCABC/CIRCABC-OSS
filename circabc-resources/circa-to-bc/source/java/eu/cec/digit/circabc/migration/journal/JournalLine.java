/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Use for strong type process journal. It can be used to log exaclty what action performed.
 *
 * The structure can be read to be interpreted to perform rollbacks for example when an error occurs.
 *
 * @author Yanick Pignot
 */
public class JournalLine implements Serializable
{
    /** */
    private static final long serialVersionUID = -4541186430373778878L;

    private static final String SPACES = "  ";

    private static final String UPDATE_SEPARATOR = " - OPERATION: ";
    private static final String ID_SEPARATOR = " - ID: ";
    private static final String PARAM_SEPARATOR = " - PARAM: ";

    /**
	 *
     */
    public enum Operation {
        CREATE, UPDATE
    }

    /**
	 *
     */
    public enum ObjectType {
        USER, NODE
    }

    /**
	 *
     */
    public enum Status {
		SUCCESS, FAIL
	}

    /**
	 *
     */
    public enum Parameter {
    	PROFILE,
    	PERMISSION,
    	PERMISSION_MAP,
    	DEFINITION,
    	OLD_VALUE,
    	NEW_VALUE,
    	USER_NAME,
    	XPATH,
    	QNAME,
    	TARGET,
    	VERSION_LABEL,
    	IS_MODERATED,
    	MODERATION_STATUS_PENDING,
    	MODERATION_STATUS_ACCEPTED,
    	MODERATION_STATUS_REJECTED

    }

    /**
	 *
     */
    public enum UpdateOperation{
		SET_PERMISSION,
		SET_NOTIFICATION,
		SET_PROPERTY,
		SET_PROPERTIES,
		SET_PREFERENCE,
		SET_PROFILE,
		SET_TRANSLATION,
		MAKE_MULTILINGUAL,
		ADD_DYNAMIC_PROPERTY_DEFINITION,
		ADD_KEYWORD_DEFINITION,
		ADD_LOGO,
		SET_DEFAULT_LOGO,
		ADD_PROFILE,
		ADD_VERSION,
		ADD_APPLICATION,
		SHARE_SPACE,
		UPDATE_PROFILE,
		DELETE_PROFILE,
		ACCEPT_MEETING,
		REJECT_MEETING,
		APPLY_MODERATION,
		POST_REPLY_OF,
		ADD_LOG_ENTRY

	}

    private final Status status;
    private final Operation operation;
    private final ObjectType objectType;
    private final String id;
    private final String path;
    private final Date date;
    private final UpdateOperation updateOperation;
    private final Map<Parameter, Serializable> parameters;

    private JournalLine(final Status status, final Operation operation, final ObjectType objectType, final String path, final String id, final UpdateOperation updateOperation, final Map<Parameter, Serializable> parameters)
    {
    	super();
        this.status = status;
        this.operation = operation;
        this.objectType = objectType;
        this.id = id;
        this.updateOperation = updateOperation;
        this.parameters = parameters;
        this.path = path;
        this.date = new Date();
    }

    private JournalLine(final Status status, final Operation operation, final ObjectType objectType, final String path, final String id)
	{
		this(status, operation, objectType, path, id, null, null);
	}


	/**
	 * Instanciate a create node action
	 *
	 * @param status
	 * @param id
	 * @return
	 */
	public static final JournalLine createNode(final Status status, final String path, final String id)
    {
    	return new JournalLine(status, Operation.CREATE, ObjectType.NODE, path, id);
    }
    /**
     * Instanciate a create user action
     *
     * @param status
     * @param id
     * @return
     */
    public static final JournalLine createUser(final Status status, final String path, final String id)
    {
    	return new JournalLine(status, Operation.CREATE, ObjectType.USER, path, id);
    }


    /**
     * Instanciate a update node action with a single parameter
     *
     * @param status
     * @param id
     * @param updateOperation
     * @param key
     * @param value
     * @return
     */
    public static final JournalLine updateNode(final Status status, final String path, final UpdateOperation updateOperation, final Parameter key, final Serializable value)
    {
    	return new JournalLine(status, Operation.UPDATE, ObjectType.NODE, path, null, updateOperation, Collections.singletonMap(key, value));
    }
    /**
     * Instanciate a update user action with a single parameter
     *
     * @param status
     * @param id
     * @param updateOperation
     * @param key
     * @param value
     * @return
     */
    public static final JournalLine updateUser(final Status status, final String path, final UpdateOperation updateOperation, final Parameter key, final Serializable value)
    {
    	return new JournalLine(status, Operation.UPDATE, ObjectType.USER, path, null, updateOperation, Collections.singletonMap(key, value));
    }


    /**
     * Instanciate a update node action with multiple parameters
     *
     * @param status
     * @param id
     * @param updateOperation
     * @param parameters
     * @return
     */
    public static final JournalLine updateNode(final Status status,  final String path, final String id, final UpdateOperation updateOperation, final Map<Parameter, Serializable> parameters)
    {
    	return new JournalLine(status, Operation.UPDATE, ObjectType.NODE, path, id, updateOperation, parameters);
    }

    /**
     * Read a toString journalLine and decode ii
     *
     * @param journalLine
     * @return
     */
    public JournalLine decodeLine(String journalLine)
    {
    	// TODO
        return null;
    }

    public String toString(final DateFormat dateFormat)
    {
    	final StringBuffer buff = new StringBuffer(dateFormat.format(date));

    	buff
    		.append(SPACES)
    		.append(this.status)
    		.append(SPACES)
    		.append(this.operation)
    		.append(SPACES)
    		.append(this.objectType)
    		.append(SPACES)
    		.append(this.path);

    	if(id != null)
    	{
    		buff
    			.append(ID_SEPARATOR)
    			.append(this.id);
    	}

    	if(operation.equals(Operation.UPDATE))
    	{
    		buff
    			.append(UPDATE_SEPARATOR)
    			.append(updateOperation);
    	}
    	if(parameters != null && parameters.size() > 0)
    	{
    		buff
    			.append(PARAM_SEPARATOR)
				.append(parameters);
    	}

    	return buff.toString();
    }

    @Override
	public String toString()
	{
		return toString(new SimpleDateFormat());
	}


    /**
     * @return the id
     */
    public final String getId()
    {
        return id;
    }


    /**
     * @return the objectType
     */
    public final ObjectType getObjectType()
    {
        return objectType;
    }


    /**
     * @return the operation
     */
    public final Operation getOperation()
    {
        return operation;
    }


    /**
     * @return the status
     */
    public final Status getStatus()
    {
        return status;
    }

	/**
	 * @return the date
	 */
	public final Date getDate()
	{
		return date;
	}

	/**
	 * @return the parameters
	 */
	public final Map<Parameter, Serializable> getParameters()
	{
		return parameters;
	}

	/**
	 * @return the updateOperation
	 */
	public final UpdateOperation getUpdateOperation()
	{
		return updateOperation;
	}

	/**
	 * @return the path
	 */
	public final String getPath()
	{
		return path;
	}
}
