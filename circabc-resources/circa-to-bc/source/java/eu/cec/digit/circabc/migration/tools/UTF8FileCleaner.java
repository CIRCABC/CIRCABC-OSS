/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tool is usefull to validate that the XSD (or any file) is UTF-8 ell formed.
 *
 * It copy the given file by remplacing all bad characters by another ('!' by default ).
 *
 * <b><u>The correction will be hand made !!!</u></b>
 *
 * @see http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
 * @see http://www.javapractices.com/topic/TopicAction.do?Id=42
 *
 * @author Yanick Pignot
 */

class UTF8FileCleaner
{
	private static final Log logger = LogFactory.getLog(UTF8FileCleaner.class);

	private static final String DEFAULT_SCHEMA_FILE = "/alfresco/extension/migration/importSchema.xsd";
	private static final char DEFAULT_REPLACEMENT = '!';

	private static boolean errorFound;


	public static void main(final String[] args) throws Throwable
	{
		String inputFile  = null;
		String outputFile = null;

		errorFound = false;

		if(args != null && args.length > 1)
		{
			inputFile = UpdateSchema.class.getResource(args[0]).getFile();
		}
		else
		{
			inputFile = UpdateSchema.class.getResource(DEFAULT_SCHEMA_FILE).getFile();
		}

		String targetNameRC = inputFile + ".utf8Clean";

		for(int x = 0; outputFile == null; ++x)
		{
			if(new File(targetNameRC).exists())
			{
				if(x == 0)
				{
					targetNameRC += ".try0";
				}
				else
				{
					int last = targetNameRC.lastIndexOf(".try");
					targetNameRC = targetNameRC.substring(0, last + 4) + String.valueOf(x);
				}
			}
			else
			{
				outputFile = targetNameRC;
			}
		}

		copyfile(inputFile, outputFile, DEFAULT_REPLACEMENT);

		if(errorFound)
		{
			if(logger.isErrorEnabled()) {
				logger.error("The file " + inputFile + " contained UTF-8 errors. ");
				logger.error("The errors are replaced by the charactere '" + DEFAULT_REPLACEMENT + "' in the file " + outputFile);
			}
		}
		else
		{
			if(logger.isInfoEnabled()) {
				logger.info("No error found. The file " + outputFile + " will be deleted.");
			}

			new File(outputFile).delete();
		}

	}

	private static void copyfile(final String srFile, final String dtFile, final char replacement)
	{
		try
		{
			final File f1 = new File(srFile);
			final File f2 = new File(dtFile);

			if(f2.exists() == false)
			{
				f2.createNewFile();
			}

			final String fileContent = getContents(f1);
			final String xmlValidfileContent = replaceNonValidXMLCharacters(fileContent,DEFAULT_REPLACEMENT);
			setContents(f2, xmlValidfileContent);

		}

		catch (final FileNotFoundException ex)
		{
			if(logger.isErrorEnabled()) {
				logger.error(ex.getMessage() + " in the specified directory.", ex);
			}
		}
		catch (final IOException e)
		{
			if(logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	private static String replaceNonValidXMLCharacters(final String in, final char default_replacement)
	{
		final StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
            {
                out.append(current);
            }
            else
            {
            	 out.append(default_replacement);
            	 errorFound = true;
            }

        }
        return out.toString();
	}



	/**
	  * Fetch the entire contents of a text file, and return it in a String.
	  * This style of implementation does not throw Exceptions to the caller.
	  *
	  * @param aFile is a file which already exists and can be read.
	  */
	  static public String getContents(final File aFile) {
	    //...checks on aFile are elided
	    final StringBuilder contents = new StringBuilder();

	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      final BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (final IOException ex){
	    	if(logger.isErrorEnabled()) {
				logger.error("Exception in getContents", ex);
    		}
	    }

	    return contents.toString();
	  }

	  /**
	   * Change the contents of text file in its entirety, overwriting any
	   * existing text.
	   *
	   * This style of implementation throws all exceptions to the caller.
	   *
	   * @param aFile is an existing file which can be written to.
	   * @throws IllegalArgumentException if param does not comply.
	   * @throws FileNotFoundException if the file does not exist.
	   * @throws IOException if problem encountered during write.
	   */
	   static public void setContents(final File aFile, final String aContents)
	                                  throws FileNotFoundException, IOException {
	     if (aFile == null) {
	       throw new IllegalArgumentException("File should not be null.");
	     }
	     if (!aFile.exists()) {
	       throw new FileNotFoundException ("File does not exist: " + aFile);
	     }
	     if (!aFile.isFile()) {
	       throw new IllegalArgumentException("Should not be a directory: " + aFile);
	     }
	     if (!aFile.canWrite()) {
	       throw new IllegalArgumentException("File cannot be written: " + aFile);
	     }

	     //use buffering
	     final Writer output = new BufferedWriter(new FileWriter(aFile));
	     try {
	       //FileWriter always assumes default encoding is OK!
	       output.write( aContents );
	     }
	     finally {
	       output.close();
	     }
	   }

}