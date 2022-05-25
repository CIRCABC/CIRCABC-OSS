/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;

/**
 * Walker that create the users
 *
 * @author Yanick Pignot
 */
public class MigrateUsers extends MigrateProcessorBase
{
    @Override
    public void visit(final Persons persons) throws Exception
    {
    	final List<Person> personList = persons.getPersons();
    	for(final Person person : personList)
    	{
    		apply(new MigrateUserCallback(getJournal(), person));
    	}

    	super.visit(persons);
    }

    @Override
    public void visit(final Circabc circabc) throws Exception
    {
    	// don't loose more time ... nothing to use under circabc
    }

    @Override
    public void visit(final LogFile circabc) throws Exception
    {
    	// don't loose more time ... nothing to use under logFiles
    }

    class MigrateUserCallback extends JournalizedTransactionCallback
    {
        private final Person person;
        private MigrateUserCallback(final MigrationTracer journal, final Person persons)
        {
        	super(journal);
            this.person = persons;
        }

        public String execute() throws Throwable
        {
        	NodeRef personNodeRef;
        	String userName = null;

        	try
			{
        		userName = (String) person.getUserId().getValue();

				if(ElementsHelper.isNodeCreated(person) == false)
	            {
	                 getUserService().createLdapUser(userName,false);

	                 getJournal().journalize(JournalLine.createUser(Status.SUCCESS, ElementsHelper.getXPath(person), userName));
	                 getReport().appendSection("User " + userName + " successfully created. ");

	                 personNodeRef = getUserService().getPerson(userName);
			         ElementsHelper.setNodeRef(person, personNodeRef);
	            }
				else
				{
					getReport().appendSection("User " + userName + " already created. ");
				}
			}
			catch(Throwable t)
			{
				getReport().appendSection("Impossible to create user " + userName + ". " + t.getMessage());

				if(isFailOnError())
				{
					throw t;
				}
				else
				{
					getJournal().journalize(JournalLine.createUser(Status.FAIL, ElementsHelper.getXPath(person), userName));
				}
			}

			return null;
		}
    };

 }
