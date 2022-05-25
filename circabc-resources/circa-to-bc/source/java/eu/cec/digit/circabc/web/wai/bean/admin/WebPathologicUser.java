/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUser;

/**
 * Web client side Pathologic user wrapper for jsp needs
 *
 * @author Yanick Pignot
 */
public class WebPathologicUser extends PathologicUser
{
	private static final String NULL_SELECTED_VALUE = "__NULL___";

	private String selectedUser;
	private final int tableIndex;

	/** */
	private static final long serialVersionUID = -8318379002452811059L;

	/*protected*/ WebPathologicUser(final PathologicUser pathologicUser, final int tableIndex)
	{
		super(pathologicUser.getPerson(), pathologicUser.getMessage(), pathologicUser.getPropositions(), pathologicUser.getPersonWasNull());
		this.tableIndex = tableIndex;
		if(getPropositions().size() == 1)
		{
			selectedUser = getPropositions().keySet().iterator().next();
		}
	}


	public List<SelectItem> getPropositionItems()
	{
		final Map<String, User> propositions = getPropositions();
		if(propositions == null)
		{
			return Collections.<SelectItem>emptyList();
		}
		else
		{
			final List<SelectItem> items = new ArrayList<SelectItem>(propositions.size());
			StringBuilder label;
			User user;
			String userId;
			for(final Map.Entry<String, User> entry: propositions.entrySet())
			{
				userId = entry.getKey();
				user = entry.getValue();

				label = new StringBuilder();
				label
					.append(userId)
					.append(" (")
					.append(user.getMoniker())
					.append("): ")
					.append(user.getFirstname())
					.append(" ")
					.append(user.getLastname())
					.append(", ")
					.append(user.getEmail());

				items.add(new SelectItem(userId, label.toString(), label.toString()));
			}

			return items;
		}

	}

	public List<String> getPropositionsKeys()
	{
		final Map<String, User> propositions = getPropositions();
		if(propositions == null)
		{
			return Collections.<String>emptyList();
		}
		else
		{
			final List<String> keys = new ArrayList<String>(propositions.size());
			keys.addAll(propositions.keySet());
			return keys;
		}
	}


	/**
	 * @return the selectedUser
	 */
	public final String getSelectedUser()
	{
		return selectedUser;
	}


	/**
	 * @return the selectedUser
	 */
	public final User getSelectedUserData()
	{
		if(selectedUser == null || getPropositions().containsKey(selectedUser) == false)
		{
			return null;
		}
		else
		{
			return getPropositions().get(selectedUser);
		}
	}



	/**
	 * @param selectedUser the selectedUser to set
	 */
	public final void setSelectedUser(final String selectedUser)
	{
		if(selectedUser != null && selectedUser.equals(NULL_SELECTED_VALUE))
		{
			this.selectedUser = null;
		}
		else if(selectedUser != null)
		{
			this.selectedUser = selectedUser;
		}
	}

	public boolean isDisabled()
	{
		final Map<String, User> propositions = getPropositions();
		return propositions == null || propositions.size() < 1;
	}


	/**
	 * @return the tableIndex
	 */
	public final int getTableIndex()
	{
		return tableIndex;
	}

}
