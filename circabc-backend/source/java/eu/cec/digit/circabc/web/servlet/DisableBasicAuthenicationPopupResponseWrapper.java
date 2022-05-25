package eu.cec.digit.circabc.web.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class DisableBasicAuthenicationPopupResponseWrapper
        extends HttpServletResponseWrapper {

    public DisableBasicAuthenicationPopupResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setHeader(String name, String value) {
        if (!name.equalsIgnoreCase("WWW-Authenticate")) {
            super.setHeader(name, value);
        }
    }

}
