/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.Comparator;

import org.alfresco.util.VersionNumber;

import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;


/**
 *  implement singleton pattern as described in 
 *  http://en.wikipedia.org/wiki/Singleton_pattern#The_solution_of_Bill_Pugh
 *  @author Slobodan Filipovic
 */

final class ContentNodeVersionComparator implements Comparator<ContentNode> , Serializable
{
	
	
	private static final long serialVersionUID = 4990276970216812503L;
	private ContentNodeVersionComparator()
	{
	}
	
	private static class ContentNodeVersionComparatorHolder
	{
		private static final ContentNodeVersionComparator INSTANCE = new ContentNodeVersionComparator();
	}
	public int compare(final ContentNode o1, final ContentNode o2)
	{
		final String versionLabel1 = (String)o1.getVersionLabel().getValue();
		final String versionLabel2 = (String)o2.getVersionLabel().getValue();
		final VersionNumber versionNumber1 = new VersionNumber(versionLabel1) ;
		final VersionNumber versionNumber2 = new VersionNumber(versionLabel2) ;
		return versionNumber1.compareTo(versionNumber2);
	}
	static Comparator<ContentNode> getInstance()
	{
		return ContentNodeVersionComparatorHolder.INSTANCE;
	}
}
