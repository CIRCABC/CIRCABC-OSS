/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Helper class managing encodings, especially UTF-8
 *
 * @author Yanick Pignot
 */
public abstract class EncodingUtils
{
	private static final String UTF_8 = "UTF-8";

	private EncodingUtils(){}

	public static final String changeToUTF8Encoding(final byte[] source, final String sourceCharsetName) throws UnsupportedEncodingException
	{
		return changeToUTF8Encoding(source, 0, source.length, sourceCharsetName);
	}

	public static final String changeToUTF8Encoding(final String source, final String sourceCharsetName) throws UnsupportedEncodingException
	{
		return changeToUTF8Encoding(source.getBytes(sourceCharsetName), sourceCharsetName);
	}

	public static final String changeToUTF8Encoding(final byte[] source, int offset, int length, final String sourceCharsetName) throws UnsupportedEncodingException
	{
		return changeEncoding(source, offset, length, sourceCharsetName, UTF_8);
	}

	public static final String changeEncoding(final String source, final String sourceCharsetName, final String targetCharsetName) throws UnsupportedEncodingException
	{
		return changeEncoding(source.getBytes(sourceCharsetName), sourceCharsetName, targetCharsetName);
	}

	public static final String changeEncoding(final byte[] source, final String sourceCharsetName, final String targetCharsetName) throws UnsupportedEncodingException
	{
		return changeEncoding(source, 0, source.length, sourceCharsetName, targetCharsetName);
	}

	public static final String changeEncoding(final byte[] source, int offset, int length, final String sourceCharsetName, final String targetCharsetName) throws UnsupportedEncodingException
	{
		if(sourceCharsetName.equals(targetCharsetName))
		{
			return new String (source, offset, length, sourceCharsetName);
		}
		else
		{
			final Charset sourceCharset = Charset.forName(sourceCharsetName);
			final Charset targetCharset = Charset.forName(targetCharsetName);

			final String sourceString = new String(source, offset, length, sourceCharset.name());

			byte[] targetBytes = sourceString.getBytes(targetCharset.name());
			final String targetString = new String (targetBytes, targetCharset.name());

			return targetString;
		}
	}


	/**
	 * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @author http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
     * @bugfix DIGIT-CIRCABC-1137
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(final String in)
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
                out.append(current);
        }
        return out.toString();
    }

}
