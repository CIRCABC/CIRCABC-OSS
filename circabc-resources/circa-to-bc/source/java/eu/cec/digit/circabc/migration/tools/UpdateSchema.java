/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/


package eu.cec.digit.circabc.migration.tools;

import java.net.URL;

/**
 * Tool to generates new pojos if the file importSchema.xsd has changed
 *
 * @author Yanick Pignot
 */
class UpdateSchema
{
	private static final String DEFAULT_SCHEMA_FILE = "/alfresco/extension/migration/importSchema.xsd";
	private static final String DEFAULT_BINDING_FILE = "/alfresco/extension/migration/importSchema.xjb";

	//private static final String DEFAULT_SCHEMA_FILE = "/alfresco/extension/migration/aida/users.xsd";
	//private static final String DEFAULT_BINDING_FILE = "/alfresco/extension/migration/aida/aida.xjb";

	public static void main(String[] args) throws Throwable
	{
		URL xsdRessource    = null;
		URL xjbRessource	= null;
		String outputFolder = null;

		if(args != null && args.length == 3)
		{
			xsdRessource    = UpdateSchema.class.getResource(args[0]);
			xjbRessource    = UpdateSchema.class.getResource(args[1]);
			outputFolder = args[2];
		}
		else
		{
			xsdRessource    = UpdateSchema.class.getResource(DEFAULT_SCHEMA_FILE);
			xjbRessource    = UpdateSchema.class.getResource(DEFAULT_BINDING_FILE);
			final URL rootRessource =  UpdateSchema.class.getResource("/");
			outputFolder = rootRessource.getFile() + "../../source/java/";

			//outputFolder = "C:\\temp\\ENT";
		}

		com.sun.tools.xjc.XJCFacade.main(
				new String []
				{
						"-extension",
						xsdRessource.getFile(),
						//"-mark-generated",
						"-b",
						xjbRessource.getFile(),
						"-d",
						outputFolder,
						"-Xfluent-api",
						//"-Xcopyable",
						//"-Xcommons-lang",
						"-Xvalue-constructor"

				}
		);
	}

}
