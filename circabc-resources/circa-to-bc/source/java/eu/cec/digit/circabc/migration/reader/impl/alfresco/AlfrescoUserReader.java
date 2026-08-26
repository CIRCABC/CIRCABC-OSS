package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.service.migration.ExportationException;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.user.UserService;

/**
 * UserReader implementation that reads users from the CIRCABC Alfresco repository.
 */
public class AlfrescoUserReader implements UserReader {

    private static final Log logger = LogFactory.getLog(AlfrescoUserReader.class);

    private NodeService nodeService;
    private PersonService personService;
    private ProfileManagerServiceFactory profileManagerServiceFactory;
    private UserService circabcUserService;

    @Override
    public List<Person> getInvitedPersons(final InterestGroup interestGroup) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(interestGroup);
        if (path == null) {
            return Collections.emptyList();
        }
        try {
            final NodeRef igRef = new NodeRef(path);
            final IGRootProfileManagerService profService = profileManagerServiceFactory.getIGRootProfileManagerService();
            final Set<String> invitedUsers = profService.getInvitedUsers(igRef);

            final List<Person> persons = new ArrayList<>(invitedUsers.size());
            for (final String userId : invitedUsers) {
                final Person person = buildPerson(userId);
                if (person != null) {
                    persons.add(person);
                }
            }
            return persons;
        } catch (Exception e) {
            throw new ExportationException("Error reading invited persons for IG " + path, e);
        }
    }

    @Override
    public List<Person> getInvitedPersons(final Category category) throws ExportationException {
        // Category-level members are typically category admins
        return Collections.emptyList();
    }

    @Override
    public Person getPerson(final String uid) throws ExportationException {
        return buildPerson(uid);
    }

    @Override
    public String getPersonidWithCommonName(final String cn) throws ExportationException {
        // Not directly supported in Alfresco - return cn as-is
        return cn;
    }

    @Override
    public boolean isPersonExists(final String userId) throws ExportationException {
        return personService.personExists(userId);
    }

    @Override
    public List<Person> getPersonsWithEmails(final List<String> emails, final boolean conjunction, final boolean negation) throws ExportationException {
        // Complex query - not needed for basic export
        return Collections.emptyList();
    }

    private Person buildPerson(final String userId) {
        if (!personService.personExists(userId)) {
            logger.warn("Person not found: " + userId);
            return null;
        }
        try {
            final NodeRef personRef = personService.getPerson(userId);
            final Map<QName, Serializable> props = nodeService.getProperties(personRef);

            final Person person = new Person();
            person.setUserId(new TypedProperty.UserIdProperty(userId));

            final String email = (String) props.get(ContentModel.PROP_EMAIL);
            if (email != null) {
                person.setEmail(new TypedProperty.EmailProperty(email));
            }

            final String firstName = (String) props.get(ContentModel.PROP_FIRSTNAME);
            if (firstName != null) {
                person.setFirstName(new TypedProperty.FirstNameProperty(firstName));
            }

            final String lastName = (String) props.get(ContentModel.PROP_LASTNAME);
            if (lastName != null) {
                person.setLastName(new TypedProperty.LastNameProperty(lastName));
            }

            return person;
        } catch (Exception e) {
            logger.warn("Error building person: " + userId, e);
            return null;
        }
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setProfileManagerServiceFactory(ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public void setCircabcUserService(UserService circabcUserService) {
        this.circabcUserService = circabcUserService;
    }
}
