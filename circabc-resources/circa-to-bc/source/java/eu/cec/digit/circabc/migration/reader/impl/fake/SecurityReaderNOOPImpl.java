/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import java.util.Collections;
import java.util.Set;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support SecurityReader
 *
 * @author Yanick Pignot
 */
public class SecurityReaderNOOPImpl implements SecurityReader
{

	public Set<CategoryInterestGroupPair> getAllImportedProfileTarget(CategoryInterestGroupPair pair) throws ExportationException
	{
		return Collections.<CategoryInterestGroupPair>emptySet();
	}

	public Set<CategoryInterestGroupPair> getAllSharedLinkTarget(CategoryInterestGroupPair pair) throws ExportationException
	{
		return Collections.<CategoryInterestGroupPair>emptySet();
	}

	public void setApplicants(ImportRoot root, InterestGroup igRoot) throws ExportationException
	{

	}

	public void setNotification(XMLNode node) throws ExportationException
	{

	}

	public void setPermission(XMLNode node) throws ExportationException
	{

	}

	public void setProfileDefinition(InterestGroup interestGroup) throws ExportationException
	{

	}

	public void setSharedDefinition(Space space) throws ExportationException
	{

	}




}