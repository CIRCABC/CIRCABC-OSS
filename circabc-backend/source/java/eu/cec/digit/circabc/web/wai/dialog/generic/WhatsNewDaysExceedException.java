/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.generic;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author beaurpi
 */
public class WhatsNewDaysExceedException extends AlfrescoRuntimeException {

    public final static Integer MAX_DAYS = 31;
    public final static Integer MIN_DAYS = 1;

    /**
     *
     */
    private static final long serialVersionUID = -3527942824713417764L;


    /**
     * @param arg0
     */
    public WhatsNewDaysExceedException(String arg0) {
        super(arg0);

    }


}
