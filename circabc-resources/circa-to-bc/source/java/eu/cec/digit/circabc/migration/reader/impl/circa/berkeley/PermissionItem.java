/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.io.Serializable;

/**
 * Wrap a permission entry in the berkely db
 *
 * @author Yanick Pignot
 */
public class PermissionItem implements Serializable
{
	/** */
	private static final long serialVersionUID = -6436498948789127792L;

	private static final String PROFILE_REGEX = "\\_{2}.+";
	private static final String INTEREST_GROUP_REGEX = "\\={2}.+";

	final String authority;
	final String permissionCode;
	final boolean user;

	/**
	 * @param authority
	 * @param permissionCode
	 */
	PermissionItem(final String authority, final String permissionCode)
	{
		super();
		this.authority = authority;
		this.permissionCode = permissionCode;
		this.user = authority.matches(PROFILE_REGEX) == false && authority.matches(INTEREST_GROUP_REGEX) == false;
	}

	/**
	 * @return the authority
	 */
	public final String getAuthority()
	{
		if(user)
		{
			return authority;
		}
		else
		{
			return authority.substring(2);
		}
	}
	/**
	 * @return the permissionCode
	 */
	public final String getPermissionCode()
	{
		return permissionCode;
	}

	/**
	 * @return the profile
	 */
	public final boolean isProfile()
	{
		return user == false && authority.matches(PROFILE_REGEX);
	}

	/**
	 * @return the profile
	 */
	public final boolean isInterestGroup()
	{
		return user == false && authority.matches(INTEREST_GROUP_REGEX);
	}
}