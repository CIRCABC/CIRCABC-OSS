/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.ImportedProfile;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileException;

/**
 * Validate the custom profiles name
 *
 * @author yanick pignot
 */
public class ImportedProfilesValidator extends JXPathValidator
{
	private static final String IMPORTED_PROFILES = ".//importedProfiles";

	@SuppressWarnings("unchecked")
	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		for(final ImportedProfile profile: (List<ImportedProfile>) context.selectNodes(IMPORTED_PROFILES))
		{
			// check if the target profile exists (in the repository and in the document)

			final InterestGroup profileIg = ElementsHelper.getElementInterestGroup(profile);
			final String targetInterestgroup = profile.getTargetInterestgroup();

			if(profileIg.getName().getValue().toString().equals(targetInterestgroup) == false)
			{
				final Category category = ElementsHelper.getElementCategory(profileIg);

				boolean profileFound = false;
				boolean profileShared = false;
				for(final InterestGroup interestGroup: category.getInterestGroups())
				{
					if(interestGroup.getName().getValue().equals(targetInterestgroup))
					{
						for(final AccessProfile targetProfile : interestGroup.getDirectory().getAccessProfiles())
						{
							if(targetProfile.getName().equals(profile.getTargetProfile()))
							{
								profileFound = true;
								profileShared = targetProfile.isExported();
								break;
							}
						}
						break;
					}
				}

				if(!profileFound)
				{
					final NodeRef targetNodeRef = ElementsHelper.getTargetRef(getRegistry().getManagementService(), (Circabc) context.getContextBean(), ElementsHelper.getQualifiedPath(category) + "/" + targetInterestgroup);

					if(targetNodeRef != null)
					{
						try
						{
							final Profile prof  = getRegistry().getProfileManagerServiceFactory().getProfileManagerService(targetNodeRef).getProfile(targetNodeRef, profile.getTargetProfile());
							profileFound = true;
							profileShared = prof.isExported();
						}
						catch (final ProfileException ignore) {
							// not found
						}
					}
				}

				if(profileFound == false)
				{
					error(journal, "The imported profile " + profile.toString() + " can't be used because the target " + targetInterestgroup + ":" + profile.getTargetProfile() + "  doesn't exists in the category " + category.getName().getValue());
				}
				else
				{
					debug(journal, "The imported profile target successfully found " + targetInterestgroup + ":" + profile.getTargetProfile());

					if(profileShared == false)
					{
						error(journal, "The imported profile " + profile.toString() + " can't be used because the target " + targetInterestgroup + ":" + profile.getTargetProfile() + " is not exported.");
					}
					else
					{
						debug(journal, "The imported profile target successfully setted being exported " + targetInterestgroup + ":" + profile.getTargetProfile());
					}
				}
			}
			else
			{
				error(journal, "The imported profile " + profile.toString() + " can't be used because the target " + targetInterestgroup + ":" + profile.getTargetProfile() + " is not in the same interest group.");
			}
		}

	}

}
