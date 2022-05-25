/**
 *
 */
package eu.cec.digit.circabc.web.servlet;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * @author beaurpi
 */
public class Range {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String MULTIPART_BYTERANGES_BOUNDRY = "<ALF4558907921887235966L>";
    private static final String MULTIPART_BYTERANGES_BOUNDRY_SEP =
            "--" + MULTIPART_BYTERANGES_BOUNDRY;

    private long start;
    private long end;
    private long entityLength;
    private String contentType;
    private String contentRange;

    /**
     * Constructor
     *
     * @param contentType Mimetype of the range content
     * @param start Start position in the parent entity
     * @param end End position in the parent entity
     * @param entityLength Length of the parent entity
     */
    Range(String contentType, long start, long end, long entityLength) {
        this.contentType = HEADER_CONTENT_TYPE + ": " + contentType;
        this.setStart(start);
        this.setEnd(end);
        this.entityLength = entityLength;
    }

    /**
     * Factory method to construct a byte range from a range header value.
     *
     * @param range Range header value
     * @param contentType Mimetype of the range
     * @param entityLength Length of the parent entity
     * @return Range
     * @throws IllegalArgumentException for an invalid range
     */
    static Range constructRange(String range, String contentType, long entityLength) {
        if (range == null) {
            throw new IllegalArgumentException("Range argument is mandatory");
        }

        // strip total if present - it does not give us anything useful
        if (range.indexOf('/') != -1) {
            range = range.substring(0, range.indexOf('/'));
        }

        // find the separator
        int separator = range.indexOf('-');
        if (separator == -1) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }

        try {
            // split range and parse values
            long start = 0L;
            if (separator != 0) {
                start = Long.parseLong(range.substring(0, separator));
            }
            long end = entityLength - 1L;
            if (separator != range.length() - 1) {
                end = Long.parseLong(range.substring(separator + 1));
            }

            // return object to represent the byte-range
            return new Range(contentType, start, end, entityLength);
        } catch (NumberFormatException err) {
            throw new IllegalArgumentException("Unable to parse range value: " + range);
        }
    }

    /**
     * Output the header bytes for a multi-part byte range header
     */
    void outputHeader(ServletOutputStream os) throws IOException {
        // output multi-part boundry separator
        os.println(MULTIPART_BYTERANGES_BOUNDRY_SEP);
        // output content type and range size sub-header for this part
        os.println(this.contentType);
        os.println(getContentRange());
        os.println();
    }

    /**
     * @return the length in bytes of the byte range content including the header bytes
     */
    int getLength() {
        // length in bytes of range plus it's header plus section marker and
        // line feed bytes
        return MULTIPART_BYTERANGES_BOUNDRY_SEP.length() + 2 + this.contentType.length() + 2
                + getContentRange().length() + 4 + (int) (this.getEnd() - this.getStart() + 1L) + 2;
    }

    /**
     * @return the Content-Range header string value for this byte range
     */
    String getContentRange() {
        if (this.contentRange == null) {
            this.contentRange = "Content-Range: bytes " + Long.toString(this.getStart()) + "-" + Long
                    .toString(this.getEnd())
                    + "/" + Long.toString(this.entityLength);
        }
        return this.contentRange;
    }

    @Override
    public String toString() {
        return this.getStart() + "-" + this.getEnd();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Range o) {
        return this.getStart() < o.getStart() ? 1 : -1;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
    }
}
