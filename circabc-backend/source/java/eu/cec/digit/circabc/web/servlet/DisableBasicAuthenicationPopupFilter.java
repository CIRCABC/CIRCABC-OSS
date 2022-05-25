package eu.cec.digit.circabc.web.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DisableBasicAuthenicationPopupFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse newResponse =
                    new DisableBasicAuthenicationPopupResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, newResponse);
        }
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
